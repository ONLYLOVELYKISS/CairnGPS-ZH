<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="15%" alt="CairnGPS 图标" />
</p>
<h1 align="center">CairnGPS-ZH</h1>
<p align="center">
<b>面向简体中文用户的 CairnGPS Android 本地化项目</b>
<br />
实时显示 GPS/GNSS 信息，并通过成就、记录和等级等游戏化功能，让户外定位更加有趣。
</p>
<p align="center">
  <a href="https://github.com/ONLYLOVELYKISS/CairnGPS-ZH/releases">下载简体中文测试 APK</a>
</p>

> Releases 中的 APK 使用 Android Debug 签名，仅供安装和功能测试，不是正式发行版，也不代表 Google Play 版本。

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="30%" alt="位置界面：坐标、速度、海拔和精度" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="30%" alt="卫星天空图" />
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="30%" alt="卫星分布三维地球仪" />
</p>

## 项目定位

本仓库 Fork 自 [MatthieuTinnes/CairnGPS](https://github.com/MatthieuTinnes/CairnGPS)，由 [ONLYLOVELYKISS/CairnGPS-ZH](https://github.com/ONLYLOVELYKISS/CairnGPS-ZH) 维护。这里专注于简体中文界面、中文测试包和相关验证；上游通用功能仍保留在应用中。

当前中文本地化包括：

- 新增完整的简体中文界面与中文资源。
- 将历史记录中的海拔、速度图表标签本地化为“最小值”和“最大值”。
- 将语言选择器中的语言名称本地化为“英语”“法语”和“简体中文”。

## 主要功能

- **实时定位**：GPS 坐标（十进制度和度分秒）、海拔、速度、水平/垂直精度及实时更新。
- **罗盘导航**：磁北与真北方向（本地计算磁偏角），以及航点导航。
- **卫星信息**：原始 GNSS 状态（`GnssStatus`）、可见/使用中的卫星、天空图、3D 地球仪及各卫星星座信息。
- **行程记录**：航点、轨迹、海拔和速度曲线，以及 GPX 导出。
- **轨迹跟踪**：通过前台服务在后台记录距离、时长、平均/最高速度和累计爬升/下降。
- **游戏化功能**：可解锁成就、个人纪录和经验等级。
- **备份与恢复**：将全部应用数据导出/导入为单个文件。
- 浅色和深色主题，支持英语、法语和简体中文。

## 测试 APK

最新的简体中文 Debug 测试包发布在本仓库的 [Releases](https://github.com/ONLYLOVELYKISS/CairnGPS-ZH/releases) 页面。发布包会明确标记为测试版，并附带 SHA-256 校验文件。

测试包的限制：

- 使用 Debug 签名，不应当被当作正式发行版或用于应用商店发布。
- 版本升级兼容性不作保证；正式使用前请备份应用数据。
- 测试反馈请在本仓库提交 [Issue](https://github.com/ONLYLOVELYKISS/CairnGPS-ZH/issues)。

## 技术栈

- **Kotlin** + **Jetpack Compose**（Material 3）
- **MVVM 架构**：`Repository`（数据访问）→ `ViewModel`（通过 `StateFlow` 提供状态）→ `Composable`（界面展示，不包含业务逻辑）
- **Coroutines + Flow**：异步操作
- **Room**（KSP）：持久化业务数据（航点、轨迹、成就、纪录）；**DataStore Preferences**：用户设置
- **LocationManager / `GPS_PROVIDER`**：不使用 FusedLocationProvider，因为应用需要原始卫星数据（`GnssStatus`）
- minSdk 26 · targetSdk 37 · compileSdk 37

## 环境要求

- 较新版本的 Android Studio（AGP 9.3、Kotlin 2.4）
- JDK 17+（可使用 Android Studio 自带的 JBR）
- Android SDK Platform 37（compileSdk/targetSdk）
- 用于测试定位功能的 GPS 设备或模拟器（模拟器可以模拟位置/路线）

## 构建

```bash
./gradlew assembleDebug
```

在 Windows（PowerShell）上运行前，确保 `JAVA_HOME` 指向 Android Studio 自带的 JBR：

```powershell
$env:JAVA_HOME = "<path to Android Studio>\jbr"
.\gradlew.bat assembleDebug
```

## 测试

```bash
./gradlew testDebugUnitTest
```

CI 会在 Pull Request 和 `main` 分支更新时执行 Debug 构建、单元测试和 lint。维护者可以在 GitHub Actions 中手动运行 **Publish Chinese debug APK**，从 `main` 构建并发布一个带有测试说明的 prerelease；该流程不需要正式签名密钥，也不会发布到上游仓库。

## 项目结构

```
app/src/main/java/app/matthieu/cairngps/
├── data/         Repositories, Room entities, DAOs, data models
├── domain/       Pure business logic (geodesy, formatting, gamification)
├── service/      Foreground service for track recording
└── ui/           Compose screens and ViewModels, by feature
    ├── location/     Position screen (home)
    ├── compass/      Compass
    ├── satellites/   GNSS status, 3D globe, constellation info
    ├── history/      Logbook (waypoints, tracks)
    ├── waypoints/
    ├── gamification/ Achievements, records, levels
    ├── profile/      Profile hub
    ├── settings/
    └── permission/   Location permission request screen
```

Room 数据库（`AppDatabase`，文件名 `cairn.db`）和各 Repository 由 `CairnApplication` 统一创建并提供。

## 许可证

CairnGPS 是自由软件，依据 [GNU Affero General Public License v3.0](LICENSE)（`AGPL-3.0-only`）授权。

应用不包含 Google Play 服务、Firebase 或第三方跟踪代码，仅依赖 Android 平台 API 和 AndroidX（Apache-2.0）。

## 鸣谢

应用使用的第三方数据与资源：

- **[Natural Earth](https://www.naturalearthdata.com/)** — 1:110m 文化地理矢量数据（世界陆地），属于公有领域。
- **[EGM96 大地水准面网格](https://earth-info.nga.mil/)**（NGA）— 用于海拔校正，属于公有领域。
- **[Roboto Mono](https://fonts.google.com/specimen/Roboto+Mono)** and
  **[Material Symbols](https://fonts.google.com/icons)**（Google）—
  依据 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 授权。

项目使用的工具：

- **[Screenshots Pro](https://screenshots.pro/)** — 用于制作 README 中展示的截图。
