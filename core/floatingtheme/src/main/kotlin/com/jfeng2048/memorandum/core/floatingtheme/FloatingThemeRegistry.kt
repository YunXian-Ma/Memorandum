package com.jfeng2048.memorandum.core.floatingtheme

import android.content.Context
import android.graphics.Color as AColor
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.io.File

/** 主题注册表：界面按这个列表渲染选择器，未知 id 回落到默认主题 */
interface FloatingThemeRegistry {
    val all: List<FloatingTheme>
    val default: FloatingTheme
    fun fromId(id: String?): FloatingTheme = all.firstOrNull { it.id == id } ?: default
}

/**
 * 从资源目录装载主题：
 * - 内置：`assets/floating_themes/<id>/theme.json`
 * - 外部主题包（可下载）：`filesDir/floating_themes/<id>/theme.json`，同 id 覆盖内置
 */
class FloatingThemeRepository private constructor(context: Context) : FloatingThemeRegistry {

    override val all: List<FloatingTheme> = load(context.applicationContext)
    override val default: FloatingTheme = all.firstOrNull { it.id == DEFAULT_ID } ?: all.first()

    private fun load(context: Context): List<FloatingTheme> {
        val result = linkedMapOf<String, FloatingTheme>()
        context.assets.list(ROOT).orEmpty().sorted().forEach { dir ->
            runCatching {
                val json = context.assets.open("$ROOT/$dir/theme.json").bufferedReader().use { it.readText() }
                parse(json) { ThemeImageRef(assetPath = "$ROOT/$dir/$it") }
            }.getOrNull()?.let { result[it.id] = it }
        }
        File(context.filesDir, ROOT).listFiles()?.sortedBy { it.name }?.forEach { dir ->
            runCatching {
                parse(File(dir, "theme.json").readText()) { ThemeImageRef(filePath = File(dir, it).path) }
            }.getOrNull()?.let { result[it.id] = it }
        }
        return result.values.toList()
    }

    private fun parse(json: String, ref: (String) -> ThemeImageRef): FloatingTheme {
        val o = JSONObject(json)
        val id = o.getString("id")
        val displayName = o.getString("displayName")
        val homeName = o.getString("homeName")
        fun text(key: String) = o.getString(key).replace("{displayName}", displayName).replace("{homeName}", homeName)
        val p = o.getJSONObject("palette")
        fun color(key: String) = Color(AColor.parseColor("#" + p.getString(key)))
        val img = o.getJSONObject("images")
        return FloatingTheme(
            id = id,
            copy = FloatingThemeCopy(
                displayName = displayName,
                homeName = homeName,
                homeSubtitle = text("homeSubtitle"),
                wentHomeToast = text("wentHomeToast"),
                idleHint = text("idleHint"),
                companionTip = text("companionTip"),
            ),
            palette = FloatingPalette(color("primary"), color("accent"), color("softBg"), color("roof")),
            images = FloatingThemeImages(
                avatar = ref(img.getString("avatar")),
                avatarHappy = img.optString("avatarHappy").takeIf { it.isNotEmpty() }?.let(ref),
                home = ref(img.getString("home")),
                homeHover = img.optString("homeHover").takeIf { it.isNotEmpty() }?.let(ref),
            ),
            animations = parseAnimations(o.optJSONObject("animations"), ref),
        )
    }

    private fun parseAnimations(o: JSONObject?, ref: (String) -> ThemeImageRef): Map<PetState, SpriteAnimation> {
        if (o == null) return emptyMap()
        return PetState.values().mapNotNull { state ->
            val a = o.optJSONObject(state.key) ?: return@mapNotNull null
            val frames = a.optInt("frames", 1)
            if (frames < 2) return@mapNotNull null
            state to SpriteAnimation(ref(a.getString("sheet")), frames, a.optInt("fps", 10).coerceIn(1, 30))
        }.toMap()
    }

    companion object {
        private const val ROOT = "floating_themes"
        private const val DEFAULT_ID = "cat"

        @Volatile private var instance: FloatingThemeRepository? = null

        fun get(context: Context): FloatingThemeRepository =
            instance ?: synchronized(this) {
                instance ?: FloatingThemeRepository(context.applicationContext).also { instance = it }
            }
    }
}
