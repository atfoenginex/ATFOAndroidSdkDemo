# ATFOAndroidSdkDemo

ATFO OpenAdSDK（`com.atfo.ad:core`）Android 接入示例，覆盖开屏、信息流（含宿主自渲染）、插屏、激励视频、通知广告五类。

## 接入文档

- 非聚合：<https://ulogtm3c7z.feishu.cn/docx/Q0VOd7DE9oWMTBx60hicrd7Lnpd>（本工程的代码按这份文档实现）
- 聚合：<https://ulogtm3c7z.feishu.cn/docx/CdAhdFq2Do8Nz4xPdfHcois6nhg>

两份章节结构一致（依赖配置 / SDK 初始化 / 广告加载和展示 / 生命周期 / 错误处理 / 隐私数据 / 联调排查 / 注意事项），按自己的投放方式看对应那份。

## 1. 配置参数

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
atfo.interstitialSlotId=
atfo.rewardSlotId=
atfo.notifySlotId=
```

`local.properties` 已被 `.gitignore` 忽略，不会误提交密钥；`atfo.applicationId`、`atfo.appName`、`atfo.env`、`atfo.debug` 模板里已给了默认值，按需修改。

- `atfo.debug=true` 时 SDK 输出内部日志，联调阶段建议打开。
- `atfo.env` 要与后台环境一致（`PRODUCTION` / `STAGING`），不一致会取不到广告配置。
- 通知广告复用信息流代码位，需在后台把该代码位配成「ATFO 模板渲染 + 信息流-通知广告」。
- 每个 key 都要保留 `atfo.` 前缀，去掉前缀的写法读不到。

## 2. 运行

要求 JDK 17+（Gradle 9.5 / AGP 9.3）、Android SDK 37。模板里的 `sdk.dir` 是注释状态，命令行构建时用环境变量提供，或按自己机器取消注释：

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk      # macOS 示例
./gradlew :app:assembleDebug                        # 产物 app/build/outputs/apk/debug/
./gradlew :app:installDebug                         # 装机
adb shell monkey -p <atfo.applicationId 里的包名> -c android.intent.category.LAUNCHER 1
```

用 Android Studio 打开工程、点 Run 也可以，Studio 会自动写入 `sdk.dir`。工程已配置 `maven.cxwlad.com` 仓库，首次同步自动拉取 SDK 与三方依赖。

## 3. 查看接入代码

主页五个按钮各对应一类广告，每类一个独立 Activity，可直接整文件复制到自己的工程：

| 广告类型 | 文件 |
|---|---|
| 开屏 | `app/src/main/java/com/atfo/ad/demo/splash/SplashAdActivity.kt` |
| 信息流（模板 + 宿主自渲染） | `app/src/main/java/com/atfo/ad/demo/feed/FeedAdActivity.kt` |
| 插屏 | `app/src/main/java/com/atfo/ad/demo/interstitial/InterstitialAdActivity.kt` |
| 激励视频 | `app/src/main/java/com/atfo/ad/demo/reward/RewardAdActivity.kt` |
| 通知广告 | `app/src/main/java/com/atfo/ad/demo/notify/NotifyAdActivity.kt` |

SDK 初始化在 `AtfoDemoApplication.kt`，参数读取在 `config/DemoConfig.kt`，自渲染视图在 `view/SelfRenderAdView.kt`。
广告页不做任何跳转，展示/点击/关闭/奖励等回调只打日志，用下面的 TAG 过滤即可看到完整回调流：

```bash
adb logcat -s ATFOSplashAdActivity:V ATFOFeedAdActivity:V ATFOInterstitialAdActivity:V ATFORewardAdActivity:V ATFONotifyAdActivity:V
```