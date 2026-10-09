# ATFOAndroidSdkDemo

ATFO OpenAdSDK（`com.atfo.ad:core`）Android 接入示例，覆盖开屏、信息流（模板渲染 / 宿主自渲染两页）、插屏、激励视频、通知广告五类，共六个页面。

## 接入文档

- 非聚合：<https://ulogtm3c7z.feishu.cn/docx/Q0VOd7DE9oWMTBx60hicrd7Lnpd>
- 聚合：<https://ulogtm3c7z.feishu.cn/docx/CdAhdFq2Do8Nz4xPdfHcois6nhg>


## 1. 注册开户

1. 联系商务人员获取媒体账户
2. 在管理后台创建应用，获取应用 ID
3. 创建广告位，获取广告位 ID
4. 获取安全密钥用于 SDK 初始化

以上步骤请联系对应的商务对接人员！

## 2. 配置参数

参数全部走 `local.properties`，不需要改任何代码。仓库里只提供一份空值模板 `local.properties.example`（每个 key 都有注释），先复制再生效：

```bash
cp local.properties.example local.properties
```

然后填入对接运营给的账户与代码位：

```properties
atfo.appId=
atfo.secret=
atfo.mediaId=
atfo.splashSlotId=
atfo.feedSlotId=
atfo.feedSelfRenderSlotId=
atfo.interstitialSlotId=
atfo.rewardSlotId=
atfo.notifySlotId=
```

`local.properties` 已被 `.gitignore` 忽略，不会误提交密钥；`atfo.applicationId`、`atfo.appName`、`atfo.env`、`atfo.debug` 模板里已给了默认值，按需修改。

- `atfo.debug=true` 时 SDK 输出内部日志，联调阶段建议打开。
- `atfo.env` 要与后台环境一致（`PRODUCTION` / `STAGING`），不一致会取不到广告配置。
- 通知广告复用信息流代码位，需在后台把该代码位配成「ATFO 模板渲染 + 信息流-通知广告」。
- 信息流按渲染方式分两个代码位：`atfo.feedSlotId` 后台配成「ATFO 模板渲染」给模板页用，`atfo.feedSelfRenderSlotId` 配成「自渲染」给自渲染页用。加载日志/Toast 里的 `renderType` 会标明本次物料类型，配错时 Toast 直接提示。
- 每个 key 都要保留 `atfo.` 前缀，去掉前缀的写法读不到。

## 3. 运行

要求 JDK 17+（Gradle 9.5 / AGP 9.3）、Android SDK 37。模板里的 `sdk.dir` 是注释状态，命令行构建时用环境变量提供，或按自己机器取消注释：

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk      # macOS 示例
./gradlew :app:assembleDebug                        # 产物 app/build/outputs/apk/debug/
./gradlew :app:installDebug                         # 装机
```

## 4. 查看接入代码

主页六个按钮各对应一个接入示例，每页一个独立 Activity（配套同名布局 `activity_<类型>_ad.xml`），可直接整文件复制到自己的工程：

| 广告类型 | 文件 |
|---|---|
| 开屏 | `app/src/main/java/com/atfo/ad/demo/splash/SplashAdActivity.kt` |
| 信息流（模板渲染） | `app/src/main/java/com/atfo/ad/demo/feed/FeedAdActivity.kt` |
| 信息流（宿主自渲染） | `app/src/main/java/com/atfo/ad/demo/feed/FeedSelfRenderAdActivity.kt` |
| 插屏 | `app/src/main/java/com/atfo/ad/demo/interstitial/InterstitialAdActivity.kt` |
| 激励视频 | `app/src/main/java/com/atfo/ad/demo/reward/RewardAdActivity.kt` |
| 通知广告 | `app/src/main/java/com/atfo/ad/demo/notify/NotifyAdActivity.kt` |

SDK 初始化在 `AtfoDemoApplication.kt`，参数读取在 `config/DemoConfig.kt`，自渲染视图在 `view/SelfRenderAdView.kt`（左图右文卡片，布局 `view_self_render_ad.xml`）。
自渲染页的卡片一律由宿主渲染，`getAdView()` 只是媒体位素材：它为空时用 `videoUrl` 在媒体位自播。用下面的 TAG 过滤即可看到完整回调流：

```bash
adb logcat -s ATFOSplashAdActivity:V ATFOFeedAdActivity:V ATFOFeedSelfRenderAdActivity:V ATFOInterstitialAdActivity:V ATFORewardAdActivity:V ATFONotifyAdActivity:V
```

## 5. 三方广告网络 adapter

Demo 默认只依赖 `com.atfo.ad:core`，ATFO 自有广告源开箱即用。SDK 还内置了多家三方广告网络的 adapter，按需追加依赖（groupId 统一为 `com.atfo.ad.adapt`，版本与 core 保持一致，见 `gradle/libs.versions.toml` 的 `atfoCore`），并在 SDK 初始化前调用对应 `register()`：

| 广告网络           | 依赖坐标（`com.atfo.ad.adapt:` 前缀） | 注册类（`import` 完整类名） |
|----------------|---|---|
| GroMore（穿山甲聚合） | `adapt-gromore` | `com.atfo.ad.adapt.gromore.initializer.GRMAdSourceInitializer` |
| 优量汇（广点通）       | `adapt-gdt` | `com.atfo.ad.adapt.gdt.initializer.GDTAdSourceInitializer` |
| 百度             | `adapt-baidu` | `com.atfo.ad.adapt.baidu.initializer.BaiduAdSourceInitializer` |
| 快手             | `adapt-ks` | `com.atfo.ad.adapt.ks.initializer.KSAdSourceInitializer` |
| 美数             | `adapt-ms` | `com.atfo.ad.adapter.ms.initializer.MSAdSourceInitializer` |
| 趣盟             | `adapt-qumeng` | `com.atfo.ad.adapt.qumeng.initializer.QuMengAdSourceInitializer` |
| 倍孜             | `adapt-bz` | `com.atfo.ad.adapt.bz.initializer.BZAdSourceInitializer` |
| 优酷             | `adapt-yk` | `com.atfo.ad.adapt.fanti.initializer.YKAdSourceInitializer` |
| 汇川（Noah）       | `adapt-noah` | `com.atfo.ad.adapt.noah.initializer.NoahAdSourceInitializer` |
| Sigmob         | `adapt-sigmob` | `com.atfo.ad.adapt.sigmob.initializer.SigmobAdSourceInitializer` |
| 泛为             | `adapt-fw` | `com.atfo.ad.adapt.fw.initializer.FWAdSourceInitializer` |
| 旺脉             | `adapt-wm` | `com.atfo.ad.adapt.wm.initializer.WMAdSourceInitializer` |

三方广告 SDK 本体由 adapter 的 POM 传递引入，不需要单独添加依赖。

以接入优量汇为例，`app/build.gradle.kts` 追加依赖：

```kotlin
implementation("com.atfo.ad.adapt:adapt-gdt:2.9.4")
```

然后在 `Application#onCreate` 里、SDK `init` 之前导入并注册：

```kotlin
import com.atfo.ad.adapt.gdt.initializer.GDTAdSourceInitializer

GDTAdSourceInitializer.register()
```

不需要某家网络参与时，把对应 `AdSource` 放进初始化参数的 `forbidNetworkList` 即可禁用，`AtfoDemoApplication.kt` 里有注释示例。