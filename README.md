# 人物备忘录（Memorandum）

一款纯本地的人物档案备忘录：记下你认识的人，再为每个人补充一条条可检索的「词条」（爱好、饮食、性格、重要日期……）。

- 无账号、无网络、无埋点，数据只存在手机本地（Room）。
- 单 Activity + Jetpack Compose Material3，界面按 `docs/prototype/v1.0.0.html` 还原。
- 首次启动自动写入 6 个默认关系，通讯录从空开始，不预置任何人物与词条。

## 功能一览

| 模块 | 能力 |
| --- | --- |
| 首页 | 人物列表、按姓名/简介/词条全文搜索、按关系筛选（关系可自定义）、最近更新 / 按姓名排序、随便翻一个（随机进入）、关系管理入口、右下角新建 FAB、空态 |
| 人物详情 | 头像与关系标签、简介、统计卡（词条数 / 分类数 / 建档日期）、按分类筛选词条、词条浮层卡片、添加词条、编辑人物、删除人物（二次确认） |
| 人物表单 | 新建 / 编辑复用一套表单，姓名必填，关系从用户维护的关系表里选择，性别三选一，简介选填 |
| 关系管理 | 新增、重命名、删除关系；被人物引用的关系与最后一个关系不允许删除 |
| 词条表单 | 新建 / 编辑复用一套表单，名称必填，9 个分类，详情选填；保存后回详情页 |
| 数据 | Room 持久化，人物删除时词条级联删除；词条变更自动刷新人物「最后更新时间」 |

## 技术栈

| 项 | 选型 |
| --- | --- |
| 语言 / 构建 | Kotlin 2.2.10、AGP 9.5.2、Gradle 9.6、JDK 17 |
| UI | Jetpack Compose + Material3（Compose BOM 2026.02.01） |
| 架构 | 单 Activity + MVI（`UiState` / `Event` / `Effect`），`StateFlow` + `Channel` 单向数据流 |
| 依赖注入 | Hilt 2.60.1（KSP，不用 kapt） |
| 路由 | Navigation Compose 2.10.2（字符串路由 + `Long` 参数） |
| 本地存储 | Room 2.8.5（KSP） |
| SDK | minSdk 24、targetSdk / compileSdk 37 |
| 包名 / 应用名 | `com.jfeng2048.memorandum` / 人物备忘录 |

## 快速开始

环境要求：JDK 17、Android Studio（支持 AGP 9.x）、Android SDK Platform 37。

```bash
# 1. 确认 SDK 路径（local.properties 已存在则可跳过）
#    sdk.dir=/Users/<you>/Library/Android/sdk

# 2. 编译 Debug 包
./gradlew :app:assembleDebug

# 3. 安装到已连接的设备 / 模拟器
./gradlew :app:installDebug

# 4. 跑单元测试
./gradlew :app:testDebugUnitTest
```

也可以直接用 Android Studio 打开工程，等待 Gradle Sync 完成后点 Run。

产物位置：`app/build/outputs/apk/debug/app-debug.apk`。

## 首次启动数据

- 首次启动时 `App` 在 IO 线程调用 `PersonRepository.seedIfNeeded()`，只在 `SharedPreferences` 未标记、且关系表为空时写入 6 个默认关系（朋友 / 同事 / 家人 / 同学 / 客户 / 其他）。
- 不预置人物与词条，首页首次打开是空态，直接用右下角 FAB 新建即可。
- 想恢复默认关系：清除应用数据（设置 → 应用 → 人物备忘录 → 存储 → 清除数据），或卸载重装。
- 关系种子在 [SeedData.kt](file:///Users/kika/code/android_project/demo/Memorandum/app/src/main/kotlin/com/jfeng2048/memorandum/data/seed/SeedData.kt)。

## 目录结构（精简）

```text
app/src/main/kotlin/com/jfeng2048/memorandum/
├── App.kt                     # @HiltAndroidApp，首启种子数据、全局异常日志
├── MainActivity.kt            # 唯一 Activity，边到边 + 主题 + NavHost
├── core/
│   ├── designsystem/theme/    # 颜色 / 字体 / 间距 / 圆角 / 关系与分类色板
│   ├── designsystem/component/# 头像、标签、输入框、按钮、空态、Toast、确认弹窗
│   ├── navigation/            # Routes 路由表 + AppNavHost
│   └── util/DateFormats.kt    # 今天 / 昨天 / 月日 / 完整日期
├── data/
│   ├── local/                 # Room：实体、DAO、关系、数据库、SharedPreferences 标记
│   ├── mapper/                # 实体 → 领域模型
│   ├── seed/                  # 默认关系种子
│   └── repository/            # PersonRepositoryImpl（唯一数据接缝）
├── domain/
│   ├── model/                 # Person、PersonEntry、Relationship、EntryCategory、Gender、HomeSort
│   ├── repository/            # PersonRepository 接口
│   └── usecase/PersonFilters.kt  # 搜索 / 筛选 / 排序（纯 Kotlin，可单测）
├── di/                        # DatabaseModule、RepositoryModule
└── feature/
    ├── home/                  # 首页（列表、搜索、筛选、排序、随机、FAB）
    ├── detail/                # 人物详情（统计、分类筛选、词条浮层、删除）
    ├── personform/            # 新建 / 编辑人物
    ├── relationship/          # 关系管理（新增 / 重命名 / 删除）
    └── entryform/             # 新建 / 编辑词条
```

架构分层、数据库表结构、路由表、打包与调试细节见 [docs/develop.md](file:///Users/kika/code/android_project/demo/Memorandum/docs/develop.md)。

## 设计还原

| 令牌 | 取值 |
| --- | --- |
| 主色 | `#3478F6` |
| 墨色正文 | `#1D1D1F` |
| 发丝线 | `#E6E7EC` |
| 页面底色 | `#FFFFFF` |
| 灰底输入 / 轨道 | `#F7F7FA` / `#F2F2F6` |
| 圆角 | 输入 12dp、卡片 16dp、标签胶囊 |
| 间距 | 8dp 栅格，页面左右留白 20dp |
| 人名 | 系统 Serif，29sp Bold；正文全部 SansSerif |

所有颜色集中在 `core/designsystem/theme/Color.kt` 与 `Tints.kt`，页面里不出现 `Color(0xFF...)`。

## 与原型 / PRD 的差异

- **头像**：原型与界面示例都使用「关系色 + 姓名首字」的生成式头像，第一版不接系统相册（PRD 中头像为可选项）。
- **词条详情**：按原型实现为浮层卡片，而不是独立路由页面。
- **删除人物**：入口放详情页顶栏「⋯」菜单，二次确认后级联删除词条并返回列表。
- **深色主题**：PRD 未要求，第一版固定浅色；`Theme.kt` 已留出接入位置。
- **登录 / 网络**：产品定位为本地单机工具，未引入登录、Retrofit、Coil。

## 验证状态与已知限制

- 本工程的 `assembleDebug` 未在开发机完整执行（按需求跳过真机/构建验证），代码以静态审查为准；首次在 Android Studio Sync 时如遇 AGP 9 内置 Kotlin 与 KSP 的兼容提示，请参考 [docs/develop.md](file:///Users/kika/code/android_project/demo/Memorandum/docs/develop.md) 的 FAQ 处理。
- 数据库版本 1，启用了 `fallbackToDestructiveMigration`：后续升级表结构时数据会被清空，正式发版前应改为手写迁移。
- 未做分页：本地通讯录量级，一次性加载并在内存中搜索即可。

## 文档

- 开发文档（架构、数据库、打包、调试、FAQ）：[docs/develop.md](file:///Users/kika/code/android_project/demo/Memorandum/docs/develop.md)
- 产品需求：[docs/requerment.md](file:///Users/kika/code/android_project/demo/Memorandum/docs/requerment.md)
- 交互原型：[docs/prototype/prototype.html](file:///Users/kika/code/android_project/demo/Memorandum/docs/prototype/prototype.html)
