# 开发文档

本文档面向接手本工程的开发者，覆盖：环境与版本、工程结构、架构设计、数据库设计、路由表、设计系统、依赖注入、打包、调试、测试与常见问题。

---

## 1. 环境与版本

| 项 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | Gradle 与 AGP 9.x 的硬性要求 |
| Gradle | 9.6 | 由 `gradle/wrapper/gradle-wrapper.properties` 决定 |
| AGP | 9.5.2 | 内置 Kotlin 支持，工程不再单独 apply `kotlin-android` |
| Kotlin | 2.2.10 | `org.jetbrains.kotlin.plugin.compose` 版本 |
| KSP | 2.2.10-2.0.2 | 与 Kotlin 版本严格对应，升级必须成对 |
| Compose BOM | 2026.02.01 | Compose / Material3 统一由此管理版本 |
| Navigation Compose | 2.10.2 | 字符串路由 |
| Hilt | 2.60.1 | `hilt-android` + `hilt-android-compiler`（KSP） |
| Room | 2.8.5 | `room-runtime` / `room-ktx` / `room-compiler`（KSP） |
| minSdk / targetSdk / compileSdk | 24 / 37 / 37 | |

版本统一在 [gradle/libs.versions.toml](file:///Users/kika/code/android_project/demo/Memorandum/gradle/libs.versions.toml) 维护，不要在模块脚本里写死版本号。

常用命令：

```bash
./gradlew :app:assembleDebug        # 编译 Debug APK
./gradlew :app:installDebug         # 安装到已连接设备
./gradlew :app:testDebugUnitTest    # JVM 单元测试
./gradlew :app:lintDebug            # Lint
./gradlew :app:bundleRelease        # 打 AAB
```

---

## 2. 工程结构

```text
Memorandum/
├── app/
│   ├── build.gradle.kts            # namespace、SDK、构建类型、依赖
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # 只声明一个 Activity
│       │   ├── res/                # app_name、主题、启动图标
│       │   └── kotlin/com/jfeng2048/memorandum/
│       │       ├── App.kt
│       │       ├── MainActivity.kt
│       │       ├── core/
│       │       │   ├── designsystem/
│       │       │   │   ├── theme/       # Color / Tints / Type / Dimens / Theme
│       │       │   │   └── component/   # 可复用 Compose 组件
│       │       │   ├── navigation/      # Routes / AppNavHost
│       │       │   └── util/            # DateFormats
│       │       ├── data/
│       │       │   ├── local/           # entity / dao / relation / AppDatabase / prefs
│       │       │   ├── mapper/          # Entity → Domain
│       │       │   ├── seed/            # 默认关系种子
│       │       │   └── repository/      # PersonRepositoryImpl
│       │       ├── domain/
│       │       │   ├── model/           # Person、PersonEntry、枚举
│       │       │   ├── repository/      # PersonRepository 接口
│       │       │   └── usecase/         # PersonFilters（纯 Kotlin）
│       │       ├── di/                  # DatabaseModule、RepositoryModule
│       │       └── feature/
│       │           ├── home/            # HomeScreen / HomeViewModel / HomeUiState + components
│       │           ├── detail/          # DetailScreen / DetailViewModel / DetailUiState + components
│       │           ├── personform/      # 新建 / 编辑人物
│       │           ├── relationship/    # 关系管理（新增 / 重命名 / 删除）
│       │           └── entryform/       # 新建 / 编辑词条
│       ├── test/                        # JVM 单测
│       └── androidTest/                 # 仪器测试（模板保留）
├── docs/
│   ├── develop.md                       # 本文件
│   ├── requerment.md                    # PRD
│   └── prototype/                       # prototype.html + preview.png
├── gradle/libs.versions.toml
├── README.md
└── settings.gradle.kts
```

依赖方向（单向，禁止反向引用）：

```text
feature (Screen / ViewModel)
   ↓
domain (model / repository 接口 / usecase)
   ↑
data (Room / mapper / repository 实现)
   ↑
di (Hilt 装配)
core (designsystem / navigation / util)  ← 各层都可依赖
```

四条硬规则：

1. `*Screen` 只读 `UiState`、只发 `Event`，不写业务、不碰数据库。
2. `*ViewModel` 只调 `PersonRepository`，不 import Compose，不持有 `Context`。
3. `data/repository` 是唯一数据接缝，换数据源只改这里 + `di/RepositoryModule`。
4. `data/local` 只被 Repository 使用，页面禁止直接 import DAO / Entity。

---

## 3. 架构设计

### 3.1 单 Activity + MVI

`MainActivity` 是唯一 Activity，只做三件事：`enableEdgeToEdge()`、挂 `MemorandumTheme`、挂 `AppNavHost`。所有页面都是 Compose，页面间切换由 Navigation Compose 完成，不新建 Activity、不共享 ViewModel。

每个功能页固定三件套：

| 文件 | 职责 |
| --- | --- |
| `XxxUiState.kt` | 一个 `data class` 描述整页状态；同时声明 `XxxEvent`（sealed interface）与 `XxxEffect`（一次性副作用） |
| `XxxViewModel.kt` | 持有 `MutableStateFlow<XxxUiState>`；`onEvent()` 处理事件；`Channel<XxxEffect>` 发出导航 / Toast |
| `XxxScreen.kt` | `collectAsStateWithLifecycle()` 订阅状态；`LaunchedEffect` 收集 effect；内部 `private fun XxxContent(state, callbacks)` 纯展示 |

### 3.2 一次交互的数据流

以「首页输入关键词」为例：

```text
用户输入
  → HomeScreen 调用 viewModel.onEvent(HomeEvent.QueryChanged(text))
  → ViewModel 更新内部 query: MutableStateFlow
  → combine(query, relationship, sort, repository.observePeople())
  → PersonFilters.filter / sort
  → 生成新的 HomeUiState
  → StateFlow 发射
  → HomeScreen 重组，列表刷新
```

要点：

- **状态只从 ViewModel 流向 UI**，Screen 不保存业务状态（浮层是否打开这类纯 UI 状态除外）。
- **一次性事件用 `Channel.receiveAsFlow()`**，避免旋转屏幕后 Toast / 导航被重放。
- **数据库是唯一事实来源**：增删改后不手动刷新列表，Room 的 `Flow` 会自动推送新数据。

### 3.3 关键取舍

| 取舍 | 原因 |
| --- | --- |
| 搜索 / 筛选 / 排序都在内存完成 | 本地通讯录量级（几百人），一次查询全量数据最简单、响应最快 |
| 无分页 / 无下拉刷新 | 同上；`LazyColumn` 足够 |
| 日期用 `SimpleDateFormat` + `Calendar` | minSdk 24 上使用 `java.time` 需要开启脱糖，多一份构建复杂度 |
| 中文姓名排序用 `Collator.getInstance(Locale.CHINA)` | 避免按 Unicode 码点排序导致的「假拼音序」 |
| `PersonAvatar` 用姓名首字 + 关系色 | 原型即如此；不引入图片加载库与权限 |
| 关系存成独立的 `relationships` 表 | 关系由用户维护，可新增/改名/删除；人物存 `relationshipId` 外键 |
| 分类在数据库里存中文标签 | 分类是固定枚举，可读性优先，读取时 `fromLabel()` 兜底到「其他」 |

---

## 4. 数据层设计

### 4.1 表结构

`relationships`（关系表，由用户维护）

| 列 | 类型 | 说明 |
| --- | --- | --- |
| id | INTEGER PK AUTOINCREMENT | 主键 |
| name | TEXT NOT NULL | 关系名称，如「朋友」「球友」 |
| colorIndex | INTEGER NOT NULL | 色板序号 0..7，决定头像与筛选点的颜色 |
| sortOrder | INTEGER NOT NULL | 展示顺序，新增的关系排在最后 |
| createdAt | INTEGER | epoch millis |

`persons`

| 列 | 类型 | 说明 |
| --- | --- | --- |
| id | INTEGER PK AUTOINCREMENT | 主键 |
| name | TEXT NOT NULL | 姓名 |
| avatarUri | TEXT NULL | 预留，当前恒为 null |
| relationshipId | INTEGER NOT NULL | 外键 → `relationships.id`，`onDelete = RESTRICT` |
| gender | TEXT NULL | 男 / 女 / 未知 |
| description | TEXT NULL | 个人简介 |
| createdAt | INTEGER | epoch millis |
| updatedAt | INTEGER | epoch millis，词条变更时同步刷新 |

`entries`

| 列 | 类型 | 说明 |
| --- | --- | --- |
| id | INTEGER PK AUTOINCREMENT | 主键 |
| personId | INTEGER NOT NULL | 外键 → `persons.id`，`onDelete = CASCADE` |
| title | TEXT NOT NULL | 词条名称 |
| category | TEXT NOT NULL | 分类中文标签（9 类） |
| content | TEXT NULL | 详细描述 |
| createdAt | INTEGER | epoch millis |
| updatedAt | INTEGER | epoch millis |

索引：`entries.personId`、`persons.relationshipId`（外键查询与 `@Relation` 都需要）。

### 4.2 DAO

`PersonDao`

| 方法 | 返回 | 用途 |
| --- | --- | --- |
| `observeAllWithEntries()` | `Flow<List<PersonWithEntries>>` | 首页列表，`ORDER BY updatedAt DESC` |
| `observeWithEntries(id)` | `Flow<PersonWithEntries?>` | 详情页单人观察 |
| `findById(id)` | `PersonEntity?` | 编辑时取原始实体 |
| `count()` | `Int` | 判断是否需要写种子数据 |
| `countByRelationship(id)` | `Int` | 删除关系前检查是否仍被人物引用 |
| `insert` / `update` / `deleteById` | — | 增改删 |
| `touch(id, timestamp)` | — | 词条变更时刷新人物最后更新时间 |

`RelationshipDao`：`observeAll` / `findById` / `count` / `insert` / `update` / `deleteById`。

`EntryDao`：`findById` / `insert` / `update` / `deleteById`。

`@Transaction + @Relation` 一次查询拿到「人物 + 关系 + 全部词条」，避免 N+1 查询；映射在 `data/mapper/Mappers.kt` 中完成，词条按 `updatedAt` 倒序。

关系删除有双重保护：

1. `PersonRepositoryImpl.deleteRelationship()` 先检查 `count() <= 1` 与 `countByRelationship()`，不满足直接返回 false（页面提示原因）。
2. 数据库外键 `onDelete = RESTRICT` 作为第二道保险，即使绕过 Repository 也不会删除有人物引用的关系。

### 4.3 存储与迁移策略

- 数据库名 `memorandum.db`，`version = 2`（v2 新增 `relationships` 表、`persons.relationship` 改为 `relationshipId`），`exportSchema = false`。
- 当前使用 `fallbackToDestructiveMigration(dropAllTables = true)`：**升级版本会清空数据**。正式发版前应改为：`exportSchema = true` + 手写 `Migration`，或引入 `AutoMigration`。
- 种子标记使用 `SharedPreferences("memorandum_prefs")` 的 `seed_done_v2` 布尔值。key 带版本号是为了让清库升级后能重新写入默认关系；标记为 true 后即使用户把这些关系删光，也不会再自动恢复。

### 4.4 种子数据

[SeedData.kt](file:///Users/kika/code/android_project/demo/Memorandum/app/src/main/kotlin/com/jfeng2048/memorandum/data/seed/SeedData.kt) 只负责写入 6 个默认关系（朋友/同事/家人/同学/客户/其他），不预置人物与词条——首页首次打开即空态。写入发生在 `App.onCreate()` 的 IO 协程里，不阻塞首帧；触发条件是「`SharedPreferences` 未标记」且「关系表为空」，因此用户把这些关系删光后不会再被自动灌回。

---

## 5. 导航

路由常量集中在 [Routes.kt](file:///Users/kika/code/android_project/demo/Memorandum/app/src/main/kotlin/com/jfeng2048/memorandum/core/navigation/Routes.kt)，注册在 [AppNavHost.kt](file:///Users/kika/code/android_project/demo/Memorandum/app/src/main/kotlin/com/jfeng2048/memorandum/core/navigation/AppNavHost.kt)。

| 路由 | 参数 | 页面 | 说明 |
| --- | --- | --- | --- |
| `home` | — | `HomeScreen` | 启动目的地 |
| `relationships` | — | `RelationshipScreen` | 关系管理 |
| `person/create` | — | `PersonFormScreen` | 新建人物 |
| `person/{personId}` | `Long` | `DetailScreen` | 人物详情 |
| `person/{personId}/edit` | `Long` | `PersonFormScreen` | 编辑人物 |
| `person/{personId}/entry/create` | `Long` | `EntryFormScreen` | 新建词条 |
| `person/{personId}/entry/{entryId}/edit` | `Long, Long` | `EntryFormScreen` | 编辑词条 |

返回栈约定：

- 新建人物成功：`navigate(person/{id}) { popUpTo("home") }`，返回键直接回首页，不会退回表单。
- 编辑人物 / 新建 / 编辑词条成功：`popBackStack()` 回到详情页，详情页靠 Room Flow 自动刷新。
- 删除人物成功：`DetailEffect.Back` → `popBackStack()`，回到首页。
- 词条浮层打开时，系统返回键由 `BackHandler` 优先拦截，先关闭浮层。

参数均声明为 `NavType.LongType`，ViewModel 通过 `SavedStateHandle.get<Long>(Routes.ARG_PERSON_ID)` 读取。

---

## 6. 设计系统

`core/designsystem/theme/`

| 文件 | 内容 |
| --- | --- |
| `Color.kt` | 基础色 token：`BrandBlue`、`Ink`、`Muted`、`Hairline`、`Canvas`、`SurfaceMuted`、`TrackGrey`、`DangerRed` 等 |
| `Tints.kt` | `Tint(background, foreground)`；`relationshipTint(colorIndex)` 8 色关系色板（按 `colorIndex` 取模）；`categoryColor()` 九种分类色 |
| `Type.kt` | `AppTypography`；`PersonNameTextStyle`（Serif 29sp）、`NumericTextStyle` |
| `Dimens.kt` | `Spacing`（4/8/12/16/20/24/32dp）、`Radius`（input 12 / card 16 / panel 10 / tag 999）、`AppShapes` |
| `Theme.kt` | `lightColorScheme` + `MemorandumTheme` |

`core/designsystem/component/` 提供：`PersonAvatar`、`RelationshipTag`、`CategoryDot`、`CategoryFilterLabel`、`SearchField`、`SimpleTopBar`、`TopBarAction`、`SectionLabel`、`FormTextField`、`PrimaryButton`、`GenderSegmentedControl`、`RelationshipChip`、`CategoryChip`、`EmptyPeopleState`、`ToastHost`、`ConfirmDialog`、`LoadingView`、`ErrorView`。

约定：

- 新增颜色先进 `Color.kt` / `Tints.kt`，页面中禁止 `Color(0xFF...)`。
- 间距、圆角优先用 `Spacing` / `Radius`，不要散落裸 `dp` 常量。
- 输入框统一 `FormTextField`（灰底无边框），按钮统一 `PrimaryButton`，顶栏统一 `SimpleTopBar`。

---

## 7. 依赖注入

| 位置 | 说明 |
| --- | --- |
| `App` | `@HiltAndroidApp`；`@Inject lateinit var personRepository` 用于首启种子数据 |
| `MainActivity` | `@AndroidEntryPoint` |
| `HomeViewModel` 等 | `@HiltViewModel` + `@Inject constructor`；Screen 用 `hiltViewModel()` 获取 |
| `DatabaseModule` | `@Provides` 提供 `AppDatabase`、`PersonDao`、`EntryDao`（`@Singleton`） |
| `RepositoryModule` | `@Binds` 把 `PersonRepositoryImpl` 绑定到 `PersonRepository`（`@Singleton`） |
| `AppPreferences` | 构造注入 `@ApplicationContext Context`，标记 `@Singleton` |

新增一个可注入对象：写 `class Foo @Inject constructor(...)`；需要绑定接口时在 `di/` 里加 `@Binds`。**不要用 `kapt`**，本工程全部走 KSP。

---

## 8. 打包

### 8.1 Debug

```bash
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

Debug 包可直接安装，用于自测与联调。

### 8.2 Release（未配置签名时）

`app/build.gradle.kts` 中的 release 构建类型开启了 AGP 9 的优化块：

```kotlin
release {
    optimization {
        enable = true
        packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
    }
}
```

若打包时优化过程报错，可先注释该块定位问题；正式发版建议保留并配合 `proguard-rules.pro` 补充 keep 规则。

### 8.3 配置签名

1) 生成 keystore（一次性）：

```bash
keytool -genkeypair -v \
  -keystore memorandum-release.jks \
  -alias memorandum \
  -keyalg RSA -keysize 2048 -validity 10000
```

2) 在 `~/.gradle/gradle.properties` 或工程 `gradle.properties`（不要提交到 Git）中写入：

```properties
RELEASE_STORE_FILE=/absolute/path/memorandum-release.jks
RELEASE_STORE_PASSWORD=******
RELEASE_KEY_ALIAS=memorandum
RELEASE_KEY_PASSWORD=******
```

3) 在 `app/build.gradle.kts` 增加：

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(providers.gradleProperty("RELEASE_STORE_FILE").get())
            storePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD").get()
            keyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS").get()
            keyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD").get()
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

4) 打包：

```bash
./gradlew :app:assembleRelease    # APK → app/build/outputs/apk/release/
./gradlew :app:bundleRelease      # AAB → app/build/outputs/bundle/release/
```

版本号在 `defaultConfig` 的 `versionCode` / `versionName` 修改；每次上架 `versionCode` 必须递增。

---

## 9. 调试

### 9.1 Logcat

全局未捕获异常由 `App.installCrashLogger()` 记录，统一 TAG 为 `Memorandum`：

```bash
adb logcat -s Memorandum:V
```

日志会先打印堆栈，再交还给系统默认处理器（不会吞掉崩溃）。

### 9.2 数据库调试

Android Studio → **App Inspection → Database Inspector**，选择 `memorandum.db` 可直接查看 `relationships` / `persons` / `entries` 三张表并执行 SQL。也可用命令行：

```bash
adb shell "run-as com.jfeng2048.memorandum ls databases"
adb exec-out run-as com.jfeng2048.memorandum cat databases/memorandum.db > memo.db
```

> 注意：App 正在运行时直接导出可能拿到未提交的数据，建议先停掉进程。

### 9.3 UI 调试

- **Layout Inspector**：查看 Compose 层级、重组次数与语义树。
- **Compose Preview**：给 `XxxContent` 加 `@Preview` 即可脱离数据库预览（`Content` 接收纯 `UiState`，这正是拆分它的原因）。
- **Layout Bounds**：开发者选项 → 显示布局边界，检查 8dp 栅格与留白。

### 9.4 常用 adb

```bash
adb shell am start -n com.jfeng2048.memorandum/.MainActivity   # 冷启动
adb shell pm clear com.jfeng2048.memorandum                     # 清数据，恢复默认关系
adb shell dumpsys activity activities | grep memorandum        # 查看任务栈
```

---

## 10. 测试

已包含的 JVM 单测：

| 文件 | 覆盖点 |
| --- | --- |
| `domain/PersonFiltersTest.kt` | 关键词命中词条标题 / 词条描述、无误命中、按关系 id 筛选、最近更新倒序 |
| `core/util/DateFormatsTest.kt` | 今天 / 昨天 / 同年月日 / 跨年完整日期 / 建档完整日期 |

```bash
./gradlew :app:testDebugUnitTest
```

新增测试建议：

- 领域规则（排序、筛选、校验）放 `domain`，用纯 JUnit，不需要 Robolectric。
- DAO 测试可用 `Room.inMemoryDatabaseBuilder` + `androidx.test`，放在 `androidTest`。
- 页面逻辑优先通过 ViewModel 单测覆盖（配合 `kotlinx-coroutines-test` 的 `runTest`）。

---

## 11. 常见问题（FAQ）

**Q1：AGP 9 内置 Kotlin 与 KSP / Hilt 冲突怎么办？**

AGP 9 起 Kotlin 编译内置，工程不再 apply `org.jetbrains.kotlin.android`。若 Sync 报「KSP 需要 Kotlin Gradle Plugin」一类错误，可在 `gradle.properties` 中关闭内置 Kotlin 并显式应用 KGP：

```properties
android.builtInKotlin=false
```

```kotlin
// 根 build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
}
```

然后在 `app/build.gradle.kts` 的 `plugins` 中 `alias(libs.plugins.kotlin.android)`。两种模式二选一，不要同时开。

**Q2：KSP 版本为什么必须和 Kotlin 对齐？**

KSP 版本号形如 `<kotlin>-<ksp>`（本工程 `2.2.10-2.0.2`）。升级 Kotlin 时 KSP 必须一起改，否则会出现编译器 API 不匹配。

**Q3：编译报 `compileSdk 37` 找不到？**

需要在 SDK Manager 安装 **Android SDK Platform 37**。`compileSdk { version = release(37) }` 是 AGP 9 的新写法，等价于旧的 `compileSdk = 37`。

**Q4：为什么不用 `java.time`？**

minSdk 24 下使用 `java.time` 需要开启 core library desugaring；本工程日期逻辑简单，用 `SimpleDateFormat` + `Calendar` 即可，少一层构建配置。

**Q5：中文姓名按拼音排序不准？**

使用了 `Collator.getInstance(Locale.CHINA)`，依赖系统 ICU 的中文排序规则。不同 ROM 的 ICU 版本可能略有差异，但不会出现「按码点乱排」的问题。若要严格拼音序，可引入 `pinyin4j` 之类的库，代价是包体变大。

**Q6：改了表结构后旧数据没了？**

当前使用 `fallbackToDestructiveMigration(dropAllTables = true)`，升级 `version` 会清空数据库。开发期可以接受；发版前请补 `Migration`。

**Q7：为什么删除人物后词条也消失了？**

`entries.personId` 声明了外键 `onDelete = CASCADE`，由 SQLite 级联删除。注意 Room 默认在创建表时启用外键约束，不需要额外开关。

**Q8：Toast 在旋转屏幕后重复出现？**

不会。`Toast` 由 `Channel` 发出，`receiveAsFlow()` 是一次性消费；且 Toast 文案是 Screen 内的 `remember` 状态，重建后自然为空。

---

## 12. 二次开发指南

**新增一个人物字段（例如「生日」）**

1. `PersonEntity` 加列（可空），`AppDatabase` 版本 +1。
2. `Person` 领域模型加字段，`Mappers.toDomain()` 补齐映射。
3. `PersonRepository` / `Impl` 的 create / update 参数加字段。
4. `PersonFormUiState` 加字段与事件，`PersonFormScreen` 加 `FormTextField`。
5. `DetailScreen` 需要展示时在资料区补一行。

**新增一个页面**

1. `feature/xxx/` 下建 `XxxScreen.kt`、`XxxViewModel.kt`、`XxxUiState.kt`。
2. `Routes` 加常量与参数构造函数。
3. `AppNavHost` 注册 `composable(...)`，声明 `navArgument` 类型。
4. Screen 使用 `hiltViewModel()`，ViewModel 用 `@HiltViewModel`。
5. 三态齐全：`isLoading` / 空态 / `errorMessage`。

**从本地数据换成云端**

只改两处：新增 `data/remote` 与新的 `PersonRepository` 实现，然后在 `di/RepositoryModule` 里把 `@Binds` 指向新实现。页面与 ViewModel 不需要动。

---

## 13. 已知限制与后续规划

当前限制：

- 未在开发机执行完整构建 / 真机验证（按项目要求跳过），首次 Sync 可能遇到环境相关提示，参考第 11 节。
- 固定浅色主题，无深色模式。
- 头像为生成式（关系色 + 首字），未接入相册与裁剪。
- 关系可新增 / 改名 / 删除，但暂不支持拖动排序，新增的关系固定在末尾。
- 无回收站、无导入导出、无云同步。
- 搜索为内存全量扫描，数据量极大时需改为 `LIKE` 查询 + 分页。

后续可做：

1. 接入 DataStore 保存「排序方式 / 最近筛选」等偏好。
2. 人物列表按关系分组 + `stickyHeader`。
3. 词条置顶、常用词条快捷添加。
4. 深色主题（补 `darkColorScheme`，页面改用语义色）。
5. 导出 / 导入 JSON，便于换机。
