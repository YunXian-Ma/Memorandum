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
- **构建与测试**：在项目根目录执行 `./gradlew test :app:assembleDebug` 验证全部模块单元测试与 Debug 包构建。

## 3. 模块结构与依赖规则
```
app                 装配层：入口、导航图（navigation/AppNavHost）、Application
core:common         纯 JVM 工具（日期格式等）
core:navigation     纯 JVM 路由表 Routes
core:designsystem   Compose 主题 / 组件 / 色板令牌
core:floatingtheme  悬浮窗主题注册表 + 各主题绘制（themes/）
domain              纯 JVM：model / repository 接口 / usecase
data                Room（entity/dao/relation）、仓库实现、偏好、种子数据、Hilt 模块
feature:*           home / person / entry / relationship / category，各自 Screen+ViewModel+UiState
service:floating    悬浮助手服务（WindowManager + ComposeView）
build-logic         约定插件：memorandum.android.{application,library,compose,hilt,room,feature} / memorandum.jvm.library
```
- 依赖方向：`app -> feature/service -> domain/core`；`data -> domain`。**feature 之间禁止互相依赖**，跨页面跳转只通过 `core:navigation` 的 Routes 由 `app` 连线。
- `domain` 不得出现 Android 依赖；feature 不得依赖 `data`（通过 `domain` 里的接口，如 `PersonRepository`、`FloatingSettings`）。
- 新增 feature：建 `feature/<name>/build.gradle.kts` 写一行 `plugins { alias(libs.plugins.memorandum.android.feature) }`，在 `settings.gradle.kts` include，并在 `app` 里依赖与注册路由。
- namespace 由模块路径自动推导（`:feature:home` -> `com.jfeng2048.memorandum.feature.home`），包名需与之一致。

## 4. 悬浮窗主题扩展（资源可扩展）
- 一个主题 = `FloatingTheme(id, copy 文案, palette 配色, drawAvatar 绘制)`，文件放在 `core/floatingtheme/.../themes/`。
- 新增主题：新建 `XxxTheme.kt`（可复制 `CatTheme.kt`），再把它加进 `FloatingThemes.all`。首页选择器、悬浮球、小窝、文案都按注册表渲染，**界面与服务代码不需要任何改动**。
- 主题 `id` 是持久化键，发布后不可更改；未知 id 自动回落到 `FloatingThemes.default`。
- 界面层禁止硬编码「猫窝」等主题词汇，一律读 `theme.copy.*`。

## 5. 数据库命名规范
- 表名使用**单数**（`person` / `entry` / `category` / `relationship` / `conversation`，关联表 `person_relationship`）。
- 主键列名为 `表名_id`（`person_id`、`entry_id`…）；外键列与被引用的主键同名；其余列使用 snake_case（`created_at`、`sort_order`）。
- Kotlin 实体属性仍用 `id`、`createdAt`，通过 `@ColumnInfo(name = ...)` 映射；每个实体一个文件，放在 `data/local/entity/`。
- 改表结构需递增 `AppDatabase.version` 与 `AppPreferences.KEY_SEEDED`（当前 v6 / `seed_done_v6`）。Demo 阶段使用破坏性迁移。
