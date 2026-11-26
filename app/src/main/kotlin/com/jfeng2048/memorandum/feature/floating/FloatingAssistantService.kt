package com.jfeng2048.memorandum.feature.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jfeng2048.memorandum.MainActivity
import com.jfeng2048.memorandum.R
import com.jfeng2048.memorandum.core.designsystem.theme.MemorandumTheme
import com.jfeng2048.memorandum.data.local.prefs.AppPreferences
import com.jfeng2048.memorandum.domain.model.ConversationSource
import com.jfeng2048.memorandum.domain.model.EntryCategory
import com.jfeng2048.memorandum.domain.model.Person
import com.jfeng2048.memorandum.domain.repository.PersonRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 全局悬浮助手：悬浮球 + 展开面板，均为 WindowManager 上的 ComposeView。
 * 剪贴板只在用户点击「读取剪贴板」时读取，绝不监听。
 */
@AndroidEntryPoint
class FloatingAssistantService : Service() {

    @Inject lateinit var repository: PersonRepository
    @Inject lateinit var preferences: AppPreferences

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val owner = ServiceViewOwner()
    private lateinit var windowManager: WindowManager

    private var ballView: ComposeView? = null
    private var panelView: ComposeView? = null
    private var homeDockView: ComposeView? = null
    private lateinit var ballParams: WindowManager.LayoutParams

    private var people by mutableStateOf<List<Person>>(emptyList())
    private var categories by mutableStateOf<List<EntryCategory>>(emptyList())
    private var panelState by mutableStateOf(FloatingPanelState())
    private var currentPet by mutableStateOf(FloatingPet.CAT)
    private var isDraggingBall by mutableStateOf(false)
    private var isOverPetHome by mutableStateOf(false)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        owner.start()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        repository.observePeople().onEach { people = it }.launchIn(scope)
        repository.observeCategories().onEach { list ->
            categories = list
            if (panelState.categoryId == null) {
                panelState = panelState.copy(categoryId = null)
            }
        }.launchIn(scope)
        preferences.floatingPetIdFlow
            .onEach { id -> currentPet = FloatingPet.fromId(id) }
            .launchIn(scope)
        initHomeDock()
        showBall()
        // 应用自己在前台时不显示悬浮球；面板打开时由面板接管
        AppForeground.visible.onEach { refreshBallVisibility() }.launchIn(scope)
    }

    private fun refreshBallVisibility() {
        ballView?.visibility =
            if (AppForeground.visible.value || panelView != null) View.GONE else View.VISIBLE
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        hidePanel()
        homeDockView?.let { runCatching { windowManager.removeView(it) } }
        homeDockView = null
        ballView?.let { runCatching { windowManager.removeView(it) } }
        ballView = null
        owner.stop()
        scope.cancel()
        super.onDestroy()
    }

    // ---------- 悬浮球 ----------

    private fun showBall() {
        val dm = resources.displayMetrics
        val size = (BALL_DP * dm.density).toInt()
        ballParams = WindowManager.LayoutParams(
            size,
            size,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = hiddenX(preferences.floatingBallIsLeft, size)
            y = (dm.heightPixels * preferences.floatingBallYRatio).toInt()
        }
        val view = newComposeView {
            FloatingBall(
                pet = currentPet,
                isDragging = isDraggingBall,
                isOverHome = isOverPetHome,
                onDragStart = { startBallDrag() },
                onDrag = { dx, dy -> moveBall(dx, dy) },
                onDragEnd = { finishBallDrag() },
                onTap = { showPanel() },
            )
        }
        ballView = view
        windowManager.addView(view, ballParams)
    }

    private fun startBallDrag() {
        isDraggingBall = true
        isOverPetHome = false
        homeDockView?.visibility = View.VISIBLE
    }

    /** 贴边后只露出一半，减少对第三方界面的遮挡 */
    private fun hiddenX(left: Boolean, size: Int): Int {
        val width = resources.displayMetrics.widthPixels
        return if (left) -size / 2 else width - size / 2
    }

    private fun moveBall(dx: Float, dy: Float) {
        ballParams.x += dx.toInt()
        ballParams.y += dy.toInt()
        ballView?.let { windowManager.updateViewLayout(it, ballParams) }
        isOverPetHome = isBallInsideHomeZone()
    }

    private fun finishBallDrag() {
        val sendHome = isOverPetHome
        isDraggingBall = false
        isOverPetHome = false
        homeDockView?.visibility = View.GONE
        if (sendHome) {
            sendPetHome()
            return
        }
        val dm = resources.displayMetrics
        val size = ballParams.width
        val left = ballParams.x + size / 2 < dm.widthPixels / 2
        ballParams.x = hiddenX(left, size)
        ballParams.y = ballParams.y.coerceIn(0, dm.heightPixels - size * 2)
        ballView?.let { windowManager.updateViewLayout(it, ballParams) }
        preferences.floatingBallIsLeft = left
        preferences.floatingBallYRatio = ballParams.y.toFloat() / dm.heightPixels
    }

    /** 先于悬浮球添加，确保宠物拖拽进小窝时始终位于小窝上方，且不打断当前手势 */
    private fun initHomeDock() {
        val dm = resources.displayMetrics
        val dockHeight = (HOME_DOCK_HEIGHT_DP * dm.density).toInt()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            dockHeight,
            overlayType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        val view = newComposeView {
            PetHomeDock(
                pet = currentPet,
                hovered = isOverPetHome,
            )
        }
        view.visibility = View.GONE
        homeDockView = view
        windowManager.addView(view, params)
    }

    private fun isBallInsideHomeZone(): Boolean {
        val dm = resources.displayMetrics
        val ballCenterX = ballParams.x + ballParams.width / 2f
        val ballCenterY = ballParams.y + ballParams.height / 2f
        val homeCenterX = dm.widthPixels / 2f
        val homeCenterY = dm.heightPixels - (HOME_CENTER_BOTTOM_DP * dm.density)
        val hitRadiusX = HOME_HIT_HALF_WIDTH_DP * dm.density
        val hitRadiusY = HOME_HIT_HALF_HEIGHT_DP * dm.density
        return kotlin.math.abs(ballCenterX - homeCenterX) <= hitRadiusX &&
            kotlin.math.abs(ballCenterY - homeCenterY) <= hitRadiusY
    }

    /** 宠物回家：关闭悬浮窗并同步关闭开关状态 */
    private fun sendPetHome() {
        val message = currentPet.wentHomeToast
        preferences.floatingAssistantEnabled = false
        toast(message)
        stopSelf()
    }

    // ---------- 面板 ----------

    private fun showPanel() {
        if (panelView != null) return
        ballView?.visibility = View.GONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType(),
            // 不带 FLAG_NOT_FOCUSABLE：Android 10+ 只有获得焦点的窗口才能读剪贴板，也才能弹键盘
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT,
        ).apply {
            dimAmount = 0.28f
            gravity = Gravity.CENTER
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        val view = newComposeView {
            FloatingPanel(
                pet = currentPet,
                state = panelState,
                people = people,
                categories = categories,
                onStateChange = { panelState = it },
                onReadClipboard = { readClipboard() },
                onSaveConversation = { saveConversation() },
                onSaveEntry = { saveEntry() },
                onQuickCreate = { name, bio -> quickCreate(name, bio) },
                onCollapse = { hidePanel() },
            )
        }
        panelView = view
        windowManager.addView(view, params)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    private fun hidePanel() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
        refreshBallVisibility()
    }

    /** 只在用户点击按钮时调用；不覆盖已有草稿以外的内容 */
    private fun readClipboard() {
        val view = panelView ?: return
        // 等面板窗口真正拿到焦点后再读
        view.postDelayed({
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val text = cm.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(this)
                ?.toString()
                ?.trim()
            if (text.isNullOrEmpty()) {
                toast("剪贴板里没有文本")
            } else {
                panelState = panelState.copy(draft = text, source = ConversationSource.CLIPBOARD)
            }
        }, 80)
    }

    private fun saveConversation() {
        val state = panelState
        val personId = state.selectedPersonId ?: return
        val content = state.draft.trim()
        if (content.isEmpty()) return
        val name = people.firstOrNull { it.id == personId }?.name.orEmpty()
        scope.launch {
            repository.saveConversation(personId, content, state.source)
            panelState = panelState.copy(draft = "", source = ConversationSource.MANUAL)
            toast("已保存到「$name」")
        }
    }

    private fun saveEntry() {
        val state = panelState
        val personId = state.selectedPersonId ?: return
        val categoryId = state.categoryId ?: return
        val title = state.entryTitle.trim()
        if (title.isEmpty()) return
        val name = people.firstOrNull { it.id == personId }?.name.orEmpty()
        scope.launch {
            repository.createEntry(personId, title, categoryId, state.entryContent.trim())
            panelState = panelState.copy(entryTitle = "", entryContent = "")
            toast("已记下「$title」给「$name」")
        }
    }

    private fun quickCreate(name: String, bio: String) {
        scope.launch {
            val id = repository.quickCreatePerson(name.trim(), bio.trim())
            // 新建后自动选中，并回到收集模式，草稿保持不变
            panelState = panelState.copy(selectedPersonId = id, mode = FloatingMode.COLLECT)
            toast("已创建「${name.trim()}」")
        }
    }

    private fun toast(message: String) =
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

    // ---------- 基础设施 ----------

    private fun newComposeView(content: @androidx.compose.runtime.Composable () -> Unit) =
        ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent { MemorandumTheme { content() } }
        }

    private fun overlayType() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    private fun startAsForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "悬浮助手", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("悬浮助手已开启")
            .setContentText("点击悬浮球，把对话收进人物档案")
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /** WindowManager 上没有 Activity，需要手动提供 Compose 依赖的三个 owner */
    private class ServiceViewOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
        private val registry = LifecycleRegistry(this)
        private val savedState = SavedStateRegistryController.create(this)
        override val viewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle get() = registry
        override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

        fun start() {
            savedState.performRestore(null)
            registry.currentState = Lifecycle.State.RESUMED
        }

        fun stop() {
            registry.currentState = Lifecycle.State.DESTROYED
            viewModelStore.clear()
        }
    }

    companion object {
        private const val CHANNEL_ID = "floating_assistant"
        private const val NOTIFICATION_ID = 1101
        private const val BALL_DP = 54
        private const val HOME_DOCK_HEIGHT_DP = 156
        private const val HOME_CENTER_BOTTOM_DP = 82
        private const val HOME_HIT_HALF_WIDTH_DP = 104
        private const val HOME_HIT_HALF_HEIGHT_DP = 76

        fun start(context: Context) {
            val intent = Intent(context, FloatingAssistantService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingAssistantService::class.java))
        }
    }
}
