# Android 悬浮窗实现笔记（基于 Memorandum 项目）

> 代码位置：`app/src/main/kotlin/com/jfeng2048/memorandum/feature/floating/`
> 组成：`FloatingAssistantService`（窗口与生命周期）、`FloatingUi`（Compose 界面）、`AppForeground`（前后台感知）。

## 1. 整体方案

**前台 Service + WindowManager + ComposeView**

- 悬浮窗必须由 Service 持有，Activity 退到后台后窗口才不会随之消失。
- 用 `WindowManager.addView()` 直接挂一个 `ComposeView`，界面与主 App 共用 `MemorandumTheme`。
- 两个独立窗口：悬浮球（小窗，不抢焦点）和展开面板（全屏半透明遮罩，可获焦点）。
- 数据通过 Hilt 注入 `PersonRepository` 和 `AppPreferences`，与主 App 共用同一个 Room 数据库。

```
开关(HomeScreen) -> startForegroundService -> FloatingAssistantService
                                                |- 悬浮球窗口 (NOT_FOCUSABLE)
                                                '- 面板窗口  (可获焦点, 遮罩)
```

## 2. 权限与 Manifest

| 项 | 说明 |
| --- | --- |
| `SYSTEM_ALERT_WINDOW` | 悬浮窗权限，需用户去系统设置页手动授权，运行时弹窗申请不了 |
| `FOREGROUND_SERVICE` | 前台服务基础权限 |
| `FOREGROUND_SERVICE_SPECIAL_USE` | targetSdk 34+ 必须；悬浮窗不属于任何预设类型，用 `specialUse` |
| `POST_NOTIFICATIONS` | Android 13+ 前台服务通知需要，申请被拒也不影响悬浮窗运行 |

```xml
<service
    android:name=".feature.floating.FloatingAssistantService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="..." />
</service>
```

- `specialUse` 必须带 `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` 说明用途，上架时也要在 Play 后台说明。
- `startForeground` 在 API 29+ 要传类型参数 `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`，低版本用两参数重载。

## 3. 权限引导流程（HomeScreen）

1. 用户打开开关。
2. Android 13+ 先申请通知权限。
3. `Settings.canDrawOverlays(context)` 为真则直接启动服务。
4. 否则先保存「已开启」状态，再跳 `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`（带 `package:` URI，直达本应用的授权页）。
5. 用 `LifecycleEventObserver` 监听 `ON_RESUME`：用户授权返回后自动启动服务；仍未授权则把开关复位为关闭。

服务的 `onCreate` 里也会再检查一次 `canDrawOverlays`，没有权限就直接 `stopSelf()`，防止崩溃。

## 4. 在 Service 里跑 Compose（最关键的坑）

`ComposeView` 挂在 WindowManager 上没有 Activity 宿主，不手动提供三个 owner 会在 attach 时崩溃：

```kotlin
setViewTreeLifecycleOwner(owner)
setViewTreeViewModelStoreOwner(owner)
setViewTreeSavedStateRegistryOwner(owner)
```

项目里的 `ServiceViewOwner` 同时实现 `LifecycleOwner`、`ViewModelStoreOwner`、`SavedStateRegistryOwner`：

- `onCreate`：`savedState.performRestore(null)`，再把 `LifecycleRegistry` 设为 `RESUMED`。
- `onDestroy`：设为 `DESTROYED` 并清空 `viewModelStore`。
- 同一个 owner 被悬浮球和面板两个 ComposeView 共用。
- `ViewCompositionStrategy.DisposeOnDetachedFromWindow`：窗口移除时释放组合，避免泄漏。

## 5. 悬浮球：拖动与贴边

**窗口参数**

- `TYPE_APPLICATION_OVERLAY`（API 26+，更低用 `TYPE_PHONE`）。
- `FLAG_NOT_FOCUSABLE`：不抢占底层应用（如微信）的输入和返回键。
- `FLAG_LAYOUT_NO_LIMITS`：允许窗口部分移出屏幕，半球贴边靠它实现。
- `Gravity.TOP or Gravity.START`，用 `x/y` 像素坐标定位。

**手势**

- Compose 的 `detectTapGestures` 处理点击，`detectDragGestures` 处理拖动。
- 拖动中每次增量调用 `windowManager.updateViewLayout()` 更新 `LayoutParams.x/y`。

**吸附与记忆**

- 松手时按球心在屏幕左半还是右半决定停靠侧。
- 贴边后只露出一半：左侧 `x = -size/2`，右侧 `x = screenWidth - size/2`，减少遮挡第三方界面。
- 位置存入 `AppPreferences`：`floatingBallIsLeft`（左右侧）和 `floatingBallYRatio`（纵向比例）。用比例而不是像素，是为了横竖屏和不同分辨率下位置合理。

## 6. 展开面板：焦点、键盘、剪贴板

**窗口参数**

- 铺满屏幕的遮罩窗，`FLAG_DIM_BEHIND` + `dimAmount = 0.28f`。
- **不带 `FLAG_NOT_FOCUSABLE`**，这样才能获得输入焦点，键盘才能弹出。
- `softInputMode = SOFT_INPUT_ADJUST_RESIZE`，Compose 内用 `imePadding()` 让面板避开键盘。
- 点击遮罩收起；面板自身加一个空 `clickable` 拦截点击，防止点面板内部也被当成遮罩点击。

**Android 10+ 剪贴板限制**

- 只有拥有输入焦点的窗口才能读取剪贴板，后台 Service 或不可聚焦窗口读到的是空。
- 做法：面板窗口可聚焦，添加后调用 `requestFocus()`；用户点「读取剪贴板」后 `postDelayed(80ms)` 再读，等窗口真正拿到焦点。
- 读取用 `primaryClip?.getItemAt(0)?.coerceToText(context)`；为空时只弹 Toast，不覆盖用户已有草稿。

**球与面板的互斥**

- 打开面板时把悬浮球设为 `GONE`，收起时恢复。
- 面板状态（人物选择、草稿、模式）放在 Service 的 `mutableStateOf` 里，收起再展开内容不丢。Compose 状态可以由 Service 持有，不必依赖 ViewModel。

## 7. 应用在前台时隐藏悬浮球

`AppForeground` 在 `Application` 里注册 `ActivityLifecycleCallbacks`，用 `started` 计数：

- `onActivityStarted` 加一，`onActivityStopped` 减一，计数大于 0 即本应用在前台。
- 状态用 `StateFlow<Boolean>` 暴露，Service 订阅后：`应用在前台 或 面板已打开` 时隐藏球，其余时候显示。
- 用 started/stopped 而不是 resumed/paused，是为了避免 Activity 切换瞬间球闪现。

## 8. 隐私与边界（产品层面）

- 不监听剪贴板（不注册 `OnPrimaryClipChangedListener`），只在用户点击按钮时读取一次。
- 不使用无障碍服务，不自动识别聊天对象，必须由用户明确选人或快速建人后才能保存。
- 读取的内容先展示在输入框中预览，用户确认保存后才写入 Room。

## 9. 踩坑清单

| 现象 | 原因与处理 |
| --- | --- |
| 添加 ComposeView 即崩溃 | 缺少 ViewTree 三个 owner，见第 4 节 |
| 剪贴板读出为空 | 窗口没有焦点（带了 `NOT_FOCUSABLE`）或读得太早，见第 6 节 |
| 软键盘不弹 | 面板窗口带了 `NOT_FOCUSABLE`，或没设 `softInputMode` |
| 前台服务启动崩溃 | 缺 `specialUse` 类型声明或权限，targetSdk 34+ 强制 |
| 开关打开但没有球 | 未授予悬浮窗权限；服务 `onCreate` 检查后自行停止 |
| 厂商 ROM（荣耀等）后台被杀 | 引导用户在系统设置里允许自启动、关闭电池优化；`START_STICKY` 只是尽力恢复 |
| 增量安装后启动崩溃，报 `Cannot find 'XxxScreen(...)'` | 新旧 dex 混用导致 Compose 方法签名缺失，Clean Project 后重新安装 |
| 横竖屏切换后球位置错乱 | 位置要存比例而不是像素，并在使用时按当前屏幕宽高换算 |

## 10. 后续可优化项

- 屏幕旋转时重新计算并刷新球的位置（目前只在创建和松手时计算）。
- 球在贴边一段时间后降低透明度，进一步减少打扰。
- 针对厂商 ROM 增加「后台运行/自启动」设置引导页。
- 为 `PersonFilters`、悬浮面板状态等纯逻辑补充单元测试；悬浮窗本身依赖系统，只能在真机验证。

## 11. 真机验证清单

1. 开启开关，授权悬浮窗，回到应用后球自动出现（切到其他应用时）。
2. 回到本应用，球隐藏。
3. 拖动球，松手后半球贴边；杀进程重开，位置与左右侧恢复。
4. 在微信复制一段文字，点球，点「读取剪贴板」，确认文本填入。
5. 未选人物时保存按钮为灰色；选人后可保存，回到本应用详情页能看到记录。
6. 点输入框，键盘弹出且面板不被遮挡。
7. 在面板内点「＋ 新朋友」，创建后自动选中并回到收集模式，草稿保留。
