package com.jfeng2048.memorandum.data.local.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 极轻量的本地标记。
 * 只用来记录「演示数据是否已经写入过」，不涉及账号或隐私信息，
 * 因此用 SharedPreferences 而不是 DataStore。
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences =
        context.getSharedPreferences("memorandum_prefs", Context.MODE_PRIVATE)

    var isSeeded: Boolean
        get() = preferences.getBoolean(KEY_SEEDED, false)
        set(value) = preferences.edit().putBoolean(KEY_SEEDED, value).apply()

    var floatingAssistantEnabled: Boolean
        get() = preferences.getBoolean(KEY_FLOATING, false)
        set(value) = preferences.edit().putBoolean(KEY_FLOATING, value).apply()

    var floatingBallIsLeft: Boolean
        get() = preferences.getBoolean(KEY_BALL_LEFT, false)
        set(value) = preferences.edit().putBoolean(KEY_BALL_LEFT, value).apply()

    /** 悬浮球纵向位置占屏幕高度的比例，适配横竖屏 */
    var floatingBallYRatio: Float
        get() = preferences.getFloat(KEY_BALL_Y, 0.4f)
        set(value) = preferences.edit().putFloat(KEY_BALL_Y, value).apply()

    private companion object {
        const val KEY_FLOATING = "floating_enabled"
        const val KEY_BALL_LEFT = "floating_ball_left"
        const val KEY_BALL_Y = "floating_ball_y_ratio"

        /**
         * 带版本号：数据库结构升级（fallbackToDestructiveMigration 会清库）后，
         * 更换 key 等同于把标记重置为 false，保证默认关系与演示数据重新写入。
         */
        const val KEY_SEEDED = "seed_done_v5"
    }
}
