# Memorandum 项目开发与设计规范

## 1. 视觉与原型设计习惯
- **设计基调**：固定沿用 V1.0 / V1.1 确立的「白纸 + 发丝线」纸面极简体系，后续版本（V1.2 等）原型均保持同一色调，不再切换为暗色或高饱和撞色风格。
- **核心色彩令牌**：
  - 页面底色：`#FFFFFF`（展示板底色 `#F1F2F5`）
  - 次级底色 / 输入框：`#F7F7FA` / `#F2F2F6`
  - 1px 发丝线：`#E6E7EC`
  - 正文墨色：`#1D1D1F`（次要 `#4C4F57` / `#6E727B` / `#8A8F9B`）
  - 唯一主动作色（品牌蓝）：`#3478F6`
  - 关系色板与词条分类色点：保持低饱和浅底深字（集中定义在 `core/designsystem/theme/Tints.kt`）
- **字体气质**：人名使用衬线体（Android `FontFamily.Serif` / 原型 `Noto Serif SC`），其余正文、标签与控件使用无衬线体（`Noto Sans SC` / 系统 SansSerif）。
- **原型产出规范**：
  - 各版本 PRD 放在 `docs/PRDs/vX.Y.Z.md`。
  - 交互原型放在 `docs/prototype/vX.Y.Z.html`（单文件自包含 HTML，双击即可打开交互）。
  - 对应预览图放在 `docs/images/vX.Y.Z.png`。

## 2. Android 工程约定
- **技术栈**：Kotlin + Jetpack Compose Material3 + Hilt (KSP) + Navigation Compose + Room (KSP)，单 Activity + MVI（`UiState` / `Event` / `Effect`）。
- **数据边界**：纯本地 Room 持久化，无账号、无网络、无后台静默抓取；悬浮助手剪贴板读取必须由用户主动触发，对话保存前必须明确关联人物。
- **构建与测试**：在项目根目录执行 `./gradlew :app:testDebugUnitTest :app:assembleDebug` 验证单元测试与 Debug 包构建。
