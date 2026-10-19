---
title: 聚合接入指南
nav_order: 3
---

# ATFOpenAdSDK Android 接入指南（聚合）

> 本文移植自飞书《【聚合】ATFOpenAdSDK Android 接入指南》（[原文链接](https://ulogtm3c7z.feishu.cn/docx/CdAhdFq2Do8Nz4xPdfHcois6nhg)，2026-10-15 revision 407），全部 API 已对照 `com.atfo.ad:core:2.9.5` 发布构件与 SDK 源码核实，过期信息以本文为准。

## 集成说明

### 快速指南

⬇️ Demo 下载：<https://github.com/atfoenginex/ATFOAndroidSdkDemo>

### 简介

ATFOpenAdSDK 是一款聚合广告 SDK，整合了多家主流广告平台，为开发者提供统一的广告接入接口。通过一次集成，即可接入多个广告平台，提高广告收益和填充率。

### 接入提示

- 请确保您的应用已在各广告平台完成注册和审核
- 建议在测试环境中完成 SDK 集成和调试后再发布到生产环境
- 注意遵守各广告平台的规范和政策

### 注册开户

1. 联系商务人员获取媒体账户
2. 在管理后台创建应用，获取应用 ID
3. 创建广告位，获取广告位 ID
4. 获取安全密钥用于 SDK 初始化

联系对应的商务对接人员！

## 一、依赖配置

### 1、远程依赖方式引入

1、在项目的 `settings.gradle.kts` 中添加 Maven 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.cxwlad.com/repository/maven-releases/")
        }
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")
        }
        google()
        mavenCentral()
    }
}
```

2、在项目的 app 下的 `build.gradle.kts` 中添加依赖：

**最新版本号：2.9.5**

```kotlin
dependencies {
    // ATFO 广告核心组件
    implementation("com.atfo.ad:core:2.9.5")
    // 百度
    implementation("com.atfo.ad.adapt:adapt-baidu:2.9.5")
    // 倍孜
    implementation("com.atfo.ad.adapt:adapt-bz:2.9.5")
    // 优量汇（广点通）
    implementation("com.atfo.ad.adapt:adapt-gdt:2.9.5")
    // GroMore（穿山甲聚合）
    implementation("com.atfo.ad.adapt:adapt-gromore:2.9.5")
    // 快手
    implementation("com.atfo.ad.adapt:adapt-ks:2.9.5")
    // 美数
    implementation("com.atfo.ad.adapt:adapt-ms:2.9.5")
    // 汇川
    implementation("com.atfo.ad.adapt:adapt-noah:2.9.5")
    // 趣盟
    implementation("com.atfo.ad.adapt:adapt-qumeng:2.9.5")
    // 优酷
    implementation("com.atfo.ad.adapt:adapt-yk:2.9.5")
    // Sigmob
    implementation("com.atfo.ad.adapt:adapt-sigmob:2.9.5")
    // 旺脉
    implementation("com.atfo.ad.adapt:adapt-wm:2.9.5")
}
```

三方广告 SDK 本体由 adapter 的 POM 传递引入（以 ATFO 托管在自有 Maven 仓库的二次打包构件形式，如 GroMore 对应 `com.atfo.grm:grm-sdk`），不需要单独添加依赖。

本地 aar 依赖方式引入（本地文件和接入方式，请联系 ATFO 侧）。

### 2、权限配置

在 `AndroidManifest.xml` 中添加必要的权限：

```xml
<!-- Network permissions -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- 可选权限（防作弊+优化广告投放） -->
<uses-permission android:name="android.permission.READ_BASIC_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## 二、SDK 初始化

### 在 Application 中初始化

```kotlin
import android.app.Application
import android.util.Log
import com.atfo.ad.adapt.baidu.initializer.BaiduAdSourceInitializer
import com.atfo.ad.adapt.bz.initializer.BZAdSourceInitializer
import com.atfo.ad.adapt.fanti.initializer.YKAdSourceInitializer
import com.atfo.ad.adapt.gdt.initializer.GDTAdSourceInitializer
import com.atfo.ad.adapt.gromore.initializer.GRMAdSourceInitializer
import com.atfo.ad.adapt.ks.initializer.KSAdSourceInitializer
import com.atfo.ad.adapt.noah.initializer.NoahAdSourceInitializer
import com.atfo.ad.adapt.qumeng.initializer.QuMengAdSourceInitializer
import com.atfo.ad.adapt.sigmob.initializer.SigmobAdSourceInitializer
import com.atfo.ad.adapt.wm.initializer.WMAdSourceInitializer
// 注意：美数的包名是 com.atfo.ad.adapter（非 adapt）
import com.atfo.ad.adapter.ms.initializer.MSAdSourceInitializer
import com.atfo.ad.core.ATFOADSDKManager
import com.atfo.ad.core.ATFOSDKConfig
import com.atfo.ad.listener.ATFOAdInitListener
import com.atfo.ad.source.AdSource

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 注册三方适配器（根据需要选择，只注册已添加依赖的 adapter）
        registerThirdPartyAdapters()

        // 初始化 SDK
        initializeATFOSDK()
    }

    private fun registerThirdPartyAdapters() {
        try {
            // 注册穿山甲适配器
            GRMAdSourceInitializer.register()

            // 注册广点通适配器
            GDTAdSourceInitializer.register()

            // 注册百度适配器
            BaiduAdSourceInitializer.register()

            // 注册快手适配器
            KSAdSourceInitializer.register()

            // 注册美数适配器
            MSAdSourceInitializer.register()

            // 注册趣盟适配器
            QuMengAdSourceInitializer.register()

            // 注册倍孜适配器
            BZAdSourceInitializer.register()

            // 注册优酷适配器
            YKAdSourceInitializer.register()

            // 注册汇川适配器
            NoahAdSourceInitializer.register()

            // 注册 sigmob 适配器
            SigmobAdSourceInitializer.register()

            // 注册旺脉适配器
            WMAdSourceInitializer.register()

        } catch (e: Exception) {
            Log.e("MyApplication", "三方适配器注册失败: ${e.message}")
        }
    }

    private fun initializeATFOSDK() {
        val sdkConfig = ATFOSDKConfig.Builder()
            .appId("your_app_id")                    // 应用 ID
            .mediaId("your_mediaId")                 // 媒体 ID
            .secret("your_secret")                   // 安全密钥
            .appName("应用名称")
            // 按需禁用广告网络（可选，默认不禁用）
            // .forbidNetworkList(mutableListOf(AdSource.GDT, AdSource.BAIDU))
            // GroMore 是否由 SDK 自动调用 start；false 时需要媒体自行调用
            .grmAutoStart(true)
            .environment(ATFOSDKConfig.Environment.PRODUCTION)  // 环境配置
            .debug(true)                             // 调试模式，开发阶段建议开启
            .build()

        ATFOADSDKManager.getInstance().init(this, sdkConfig, object : ATFOAdInitListener {
            override fun onSuccess() {
                Log.d("MyApplication", "✅ SDK初始化成功")
            }

            override fun onFail() {
                Log.e("MyApplication", "SDK初始化失败")
            }
        })
    }
}
```

`ATFOSDKConfig.Builder` 参数速查：

| 参数 | 说明 | 默认值 |
|---|---|---|
| `appId` / `mediaId` / `secret` | 开户获取的三个必填凭证（`build()` 要求 `appId` 非空） | — |
| `appName` | 应用名称 | — |
| `environment` | `PRODUCTION` / `STAGING` | `PRODUCTION` |
| `debug` | 调试日志开关 | `false` |
| `grmAutoStart` | GroMore 自动 start | `true` |
| `forbidNetworkList` | 禁用的广告网络列表 | 空 |
| `extra(key, value)` | 透传参数 | 空 |

### 环境配置

SDK 支持多种环境配置：

```kotlin
// 生产环境
.environment(ATFOSDKConfig.Environment.PRODUCTION)

// 测试环境
.environment(ATFOSDKConfig.Environment.STAGING)
```

### 透传参数配置

```kotlin
// 设置透传参数（key 在前，value 在后）
.extra("cur_id", "android_mock_oaid_123")
```

## 三、广告加载和展示

加载统一走 `ATFOAdLoader(context, isDebug)`，第二个参数是调试日志开关，与初始化的 `debug` 保持一致即可；回调接口为 `ATFOAdLoader.AdLoadListener`（`onAdLoadSuccess` / `onAdLoadFailed`）。

### 开屏广告

```kotlin
class SplashActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var splashAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        adContainer = findViewById(R.id.ad_container)

        // 加载开屏广告
        loadSplashAd()
    }

    private fun loadSplashAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.SPLASH)
            .timeout(5000)                    // 可选；超时以管理后台下发为准，建议不配置（见「注意事项」）
            .activity(this)                   // 当前 Activity
            .slotId("") // 详情参见：https://ulogtm3c7z.feishu.cn/docx/Sg1fdvx7Zov9adxonslc8oGVnve的代码位Id
            .size(1080, 1920)
            .build()

        ATFOAdLoader(this, true).loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                // 广告加载成功
                Log.d("SplashAd", "广告加载成功")
                Log.d("SplashAd", "广告ID: ${adObject.adId}")
                // 单位是分，如获取价格方法不存在，请联系运营。
                Log.d("SplashAd", "价格: ${adObject.price}")

                splashAdObject = adObject

                // 显示广告
                showSplashAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                // 广告加载失败
                Log.e("SplashAd", "广告加载失败: ${error.message}")
                goToMainActivity()
            }
        })
    }

    private fun showSplashAd() {
        val adObject = splashAdObject ?: return

        if (!adObject.isValid()) {
            // 判断广告是否可用
            return
        }

        adObject.showAd(adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                Log.d("SplashAd", "开屏广告展示成功")
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e("SplashAd", "开屏广告展示失败: $errorMessage")
                goToMainActivity()
            }

            override fun onAdClicked() {
                Log.d("SplashAd", "开屏广告被点击")
                goToMainActivity()
            }

            override fun onAdClosed() {
                Log.d("SplashAd", "开屏广告被关闭")
                goToMainActivity()
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.d("SplashAd", "❌ 开屏广告renderfail")
            }

            override fun onAdExposed() {
                Log.d("SplashAd", "开屏广告exposed")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        splashAdObject?.destroy()
    }
}
```

> `goToMainActivity()` 为示例中的业务跳转方法，需要按自己的页面结构实现。

### 信息流广告

```kotlin
class FeedAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var feedAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        adContainer = findViewById(R.id.ad_container)

        // 加载信息流广告
        loadFeedAd()
    }

    private fun loadFeedAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.FEED)
            .slotId("") // 详情参见：https://ulogtm3c7z.feishu.cn/docx/Sg1fdvx7Zov9adxonslc8oGVnve的代码位Id
            .activity(this)
            .size(1080, 1080)
            .extras(mapOf("scene" to "feed_list"))
            .build()

        ATFOAdLoader(this, true).loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                Log.d("FeedAd", "信息流广告加载成功")
                Log.d("FeedAd", "广告ID: ${adObject.adId}")
                // 单位是分，如获取价格方法不存在，请联系运营。
                Log.d("FeedAd", "价格: ${adObject.price}")

                feedAdObject = adObject

                // 显示广告
                showFeedAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e("FeedAd", "信息流广告加载失败: ${error.message}")
            }
        })
    }

    private fun showFeedAd() {
        val adObject = feedAdObject ?: return

        if (!adObject.isValid()) {
            // 判断广告是否可用
            return
        }

        adObject.showAd(adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                Log.d("FeedAd", "信息流广告展示成功")
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e("FeedAd", "信息流广告展示失败: $errorMessage")
            }

            override fun onAdClicked() {
                Log.d("FeedAd", "信息流广告被点击")
            }

            override fun onAdClosed() {
                Log.d("FeedAd", "信息流广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.d("FeedAd", "❌ 信息流广告renderFail")
            }

            override fun onAdExposed() {
                Log.d("FeedAd", "信息流广告exposed")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        feedAdObject?.destroy()
    }
}
```

### 宿主自渲染（render_type=1）

SDK 通过 `ATFOAd.nativeInfo` 提供物料（标题、描述、图片、视频等），UI 由接入方自行构建，最后调用 `ATFOAd.registerInteraction` 绑定交互。曝光、点击、关闭的上报由 SDK 统一处理，接入方无需重复上报。

判断方式：`ATFOAd.isNativeExpress()` 为 `false` 即为自渲染广告，物料见 `ATFOAd.nativeInfo`；为 `true` 为三方模板广告，使用 `ATFOAd.showAd` 展示。

物料字段（ATFONativeFeedAdData）：

| 字段 | 说明 |
|---|---|
| `title` / `description` / `source` | 标题、描述、来源 |
| `icon` / `images` | 图标与主图（`ATFONativeImage`，含 `url` / `width` / `height`） |
| `videoUrl` / `videoCover` / `videoDurationSec` | 视频地址、封面、时长（秒）；`videoUrl` 由宿主 VideoView 播放 |
| `landingUrl` / `deeplinkUrl` / `downloadUrl` | 落地页、深链、下载地址 |
| `buttonText` | CTA 按钮文案 |
| `appInfo` | 应用下载类广告信息（名称、包名、隐私政策等） |
| `sdkVideoRender` | `true` 表示视频由三方 SDK 的 MediaView 播放，必须使用 `getAdView()` |
| `adLogo` | 广告合规角标（如百度/穿山甲），宿主 UI 需展示 |

辅助方法：`primaryImageUrl()` 返回第一张可用主图/封面/图标；`hasRenderableMaterial()` 判断物料是否足以渲染。

#### 展示示例

```kotlin
private fun showSelfRenderFeed(ad: ATFOAd, adContainer: FrameLayout) {
    // 模板广告（render_type=2/3）走 showAd；自渲染走本方法
    if (ad.isNativeExpress()) {
        ad.showAd(adContainer, interactionListener())
        return
    }

    val info = ad.nativeInfo
    if (info == null || !info.hasRenderableMaterial()) {
        Log.e("FeedAd", "自渲染广告物料为空或不足")
        return
    }

    // 1. 优先使用三方 SDK 提供的 View（如 GDT 自渲染视频 MediaView）
    val sdkView = ad.getAdView()
    if (sdkView != null) {
        adContainer.addView(
            sdkView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        ad.registerInteraction(
            container = adContainer,
            clickableViews = listOf(sdkView),
            closeViews = emptyList(),
            interactionListener = interactionListener(),
            adLogoParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END or Gravity.TOP
                leftMargin = dp(8f)
                topMargin = dp(8f)
            }
        )
        return
    }

    // 2. sdkVideoRender=true 必须依赖 getAdView()
    if (info.sdkVideoRender) {
        Log.e("FeedAd", "sdkVideoRender=true 但 getAdView() 为空，无法展示")
        return
    }

    // 3. 宿主自行构建 UI（示例：左图右文；图片加载由接入方实现，如 Glide）
    val root = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(dp(12f), dp(8f), dp(12f), dp(8f))
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    val cover = ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(110f), dp(110f))
        info.primaryImageUrl()?.let { loadImage(it, this) } // 自行实现图片加载（如 Glide）
    }
    val textColumn = TextView(this).apply {
        text = listOf(info.title, info.description)
            .filter { !it.isNullOrBlank() }
            .joinToString("\n")
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }
    root.addView(cover)
    root.addView(textColumn)
    // 合规角标需展示：info.adLogo（registerInteraction 的 adLogoParams 控制其位置）
    adContainer.addView(root)

    // 4. 绑定交互：点击区域、关闭按钮；曝光/点击/关闭由 SDK 自动上报
    ad.registerInteraction(
        container = root,
        clickableViews = listOf(root, cover, textColumn),
        closeViews = emptyList(), // 如需关闭按钮：listOf(closeButton)
        interactionListener = interactionListener(),
        adLogoParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            leftMargin = dp(8f)
            topMargin = dp(8f)
        }
    )
}

// dp 换算由接入方实现，示例：
private fun dp(value: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

// 交互监听器：与模板广告一致
private fun interactionListener(): AdInteractionListener = object : AdInteractionListener {
    override fun onAdShowSuccess() {}
    override fun onAdShowFailed(errorCode: Int, errorMessage: String) {}
    override fun onAdClicked() {}
    override fun onAdClosed() {}
    override fun onAdRenderFail(errorCode: Int, errorMessage: String) {}
    override fun onAdExposed() {}
}
```

#### 注意事项

- 自渲染广告不要调用 `showAd`，否则物料无法展示；
- `nativeInfo` 为空或 `hasRenderableMaterial()` 为 `false` 时按渲染失败处理；
- `sdkVideoRender = true` 时视频由三方 SDK 渲染，必须使用 `getAdView()`，不能自行用 `videoUrl` 播放；
- `adLogo` 为广告合规角标，需要展示在广告视图上；
- 容器需先加入视图树再调用 `registerInteraction`；
- 广告对象使用完毕后调用 `destroy()` 释放。

### 插屏广告

```kotlin
class InterstitialAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var interstitialAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)

        adContainer = findViewById(R.id.ad_container)

        // 加载插屏广告
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.INTERSTITIAL)
            .timeout(10000)                   // 可选；超时以管理后台下发为准，建议不配置（见「注意事项」）
            .activity(this)
            .slotId("") // 详情参见：https://ulogtm3c7z.feishu.cn/docx/Sg1fdvx7Zov9adxonslc8oGVnve的代码位Id
            .size(1080, 1920)
            .extras(mapOf("scene" to "interstitial"))
            .build()

        ATFOAdLoader(this, true).loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                Log.d("InterstitialAd", "插屏广告加载成功")
                Log.d("InterstitialAd", "广告ID: ${adObject.adId}")
                // 单位是分，如获取价格方法不存在，请联系运营。
                Log.d("InterstitialAd", "价格: ${adObject.price}")

                interstitialAdObject = adObject

                // 显示广告
                showInterstitialAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e("InterstitialAd", "插屏广告加载失败: ${error.message}")
            }
        })
    }

    private fun showInterstitialAd() {
        val adObject = interstitialAdObject ?: return

        if (!adObject.isValid()) {
            // 判断广告是否可用
            return
        }

        adObject.showAd(this, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                Log.d("InterstitialAd", "插屏广告展示成功")
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e("InterstitialAd", "插屏广告展示失败: $errorMessage")
            }

            override fun onAdClicked() {
                Log.d("InterstitialAd", "插屏广告被点击")
            }

            override fun onAdClosed() {
                Log.d("InterstitialAd", "插屏广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.d("InterstitialAd", "❌ 插屏广告renderFail")
            }

            override fun onAdExposed() {
                Log.d("InterstitialAd", "插屏广告exposed")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAdObject?.destroy()
    }
}
```

### 激励视频广告

```kotlin
class RewardAdActivity : AppCompatActivity() {

    private var rewardAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        // 加载激励视频广告
        loadRewardAd()
    }

    private fun loadRewardAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.REWARD)
            .timeout(10000)                   // 可选；超时以管理后台下发为准，建议不配置（见「注意事项」）
            .activity(this)
            .slotId("") // 详情参见：https://ulogtm3c7z.feishu.cn/docx/Sg1fdvx7Zov9adxonslc8oGVnve的代码位Id
            .size(720, 1280)
            .extras(mapOf("scene" to "reward"))
            .build()

        ATFOAdLoader(this, true).loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                Log.d("RewardAd", "激励视频广告加载成功")
                Log.d("RewardAd", "广告ID: ${adObject.adId}")
                Log.d("RewardAd", "广告类型: ${adObject.adType}")
                // 单位是分，如获取价格方法不存在，请联系运营。
                Log.d("RewardAd", "价格: ${adObject.price}")

                rewardAdObject = adObject

                // 根据业务时机展示广告
                showRewardAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e("RewardAd", "激励视频广告加载失败: ${error.message}")
            }
        })
    }

    private fun showRewardAd() {
        val adObject = rewardAdObject ?: return

        if (!adObject.isValid()) {
            // 判断广告是否可用
            return
        }

        // 激励视频广告为全屏展示，传入当前 Activity 即可
        adObject.showAd(this, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                Log.d("RewardAd", "激励视频广告展示成功")
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e("RewardAd", "激励视频广告展示失败: $errorMessage")
            }

            override fun onAdClicked() {
                Log.d("RewardAd", "激励视频广告被点击")
            }

            override fun onAdClosed() {
                Log.d("RewardAd", "激励视频广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.d("RewardAd", "❌ 激励视频广告renderFail")
            }

            override fun onAdExposed() {
                Log.d("RewardAd", "激励视频广告exposed")
            }

            override fun onReward() {
                Log.d("RewardAd", "激励视频广告获得奖励")
                // 在这里处理业务发奖逻辑
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        rewardAdObject?.destroy()
    }
}
```

### 通知广告

> 通知广告是 ATFOpenAdSDK 提供的一种特殊信息流广告：客户端按信息流广告（AdType.FEED）拉取，由 SDK 模板渲染并负责展示。
>
> 后台配置（两项缺一不可）：
>
> 1. 代码位的渲染方式选「ATFO 模板渲染」
> 2. 模板选「信息流-通知广告」
>
> 如果需要使用非 ATFO 广告平台的代码位，需要在对应的平台后台创建代码位，选择 FEED 流广告类型，自渲染即可。

```kotlin
class NotifyAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var notifyAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notify_ad)

        adContainer = findViewById(R.id.ad_container)

        // 加载信息流广告
        loadNotifyAd()
    }

    private fun loadNotifyAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.FEED)
            .timeout(8000)                    // 可选；超时以管理后台下发为准，建议不配置（见「注意事项」）
            .slotId("") // 详情参见：https://ulogtm3c7z.feishu.cn/docx/Sg1fdvx7Zov9adxonslc8oGVnve的代码位Id
            .activity(this)
            .size(1000, 1000)
            .extras(mapOf("scene" to "notify_ad"))
            .build()

        ATFOAdLoader(this, true).loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                Log.d("NotifyAd", "通知广告加载成功")
                Log.d("NotifyAd", "广告ID: ${adObject.adId}")
                // 如获取价格方法不存在，请联系运营
                Log.d("NotifyAd", "价格: ${adObject.price}")

                notifyAdObject = adObject

                // 显示广告
                showNotifyAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e("NotifyAd", "通知广告加载失败: ${error.message}")
            }
        })
    }

    private fun showNotifyAd() {
        val adObject = notifyAdObject ?: return

        if (!adObject.isValid()) {
            // 判断广告是否可用
            return
        }

        adObject.showAd(adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                Log.d("NotifyAd", "通知广告展示成功")
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e("NotifyAd", "通知广告展示失败: $errorMessage")
            }

            override fun onAdClicked() {
                Log.d("NotifyAd", "通知广告被点击")
            }

            override fun onAdClosed() {
                Log.d("NotifyAd", "通知广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.d("NotifyAd", "❌ 通知广告renderFail")
            }

            override fun onAdExposed() {
                Log.d("NotifyAd", "通知广告exposed")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        notifyAdObject?.destroy()
    }
}
```

## 四、竞价回传

当媒体使用 GroMore、TopOn 等第三方聚合平台，且本 SDK 作为竞价渠道参与竞价时，需通过竞价回传功能向本 SDK 通知竞价结果。

找不到竞价回传方法请联系运营。

参与竞价（Bidding）的广告位，在竞价结束后需要把竞胜/竞败结果回传给 SDK，用于计费和效果归因。

### 1、竞胜回传

```kotlin
val winPrice = 100
val secondPrice = 20
val secondPriceWinner = AdSource.QM // 见广告渠道映射
val currency = "CNY" // 币种，默认 CNY
val mBidFeedback = BidFeedback.buildWinFeedback(
    winPrice = winPrice,
    secondPrice = secondPrice,
    secondPriceWinner = secondPriceWinner,
    currency = currency,
)
mATFOAd.sendWinNotification(mBidFeedback)
```

### 2、竞败回传

```kotlin
val winPrice = 100
val lossReason = BidFeedback.LOSS_REASON_LOW_PRICE
val winner = AdSource.QM // 见广告渠道映射
val currency = "CNY" // 币种，默认 CNY
val mBidFeedback = BidFeedback.buildLossFeedback(
    winPrice = winPrice,
    lossReason = lossReason,
    winner = winner,
    currency = currency,
)
adObject.sendLossNotification(mBidFeedback)
```

### 3、广告渠道映射

| `AdSource` | 广告平台    |
|------------|---------|
| `GRM`      | gromore |
| `GDT`      | 广点通     |
| `BAIDU`    | 百度      |
| `KS`       | 快手      |
| `MS`       | 美数      |
| `YK`       | 优酷      |
| `QM`       | 趣盟      |
| `BZ`       | 倍孜      |
| `NOAH`     | 汇川      |
| `SIGMOB`   | sigmob  |
| `WM`       | 旺脉      |

其他平台，使用字符串 OTHER

## 五、清理广告缓存

### 1、清空全部广告缓存

```kotlin
ATFOADSDKManager.getInstance().clearAllCachedAds()
```

### 2、按广告位清理缓存

```kotlin
ATFOADSDKManager.getInstance().clearCachedAds(slotId)
```

## 六、生命周期管理

### 广告对象销毁

在 Activity 或 Fragment 销毁时，必须调用广告对象的销毁方法：

```kotlin
override fun onDestroy() {
    super.onDestroy()

    // 销毁广告对象
    adObject?.destroy()
}
```

### 取消正在加载的广告

如果需要取消正在加载的广告：

```kotlin
// 取消加载
loader.cancelLoad()
```

## 七、错误处理

### 错误码说明

| 错误码 | 常量 | 说明 |
|---|---|---|
| 1001 | `ATFOAdError.ERROR_NETWORK` | 网络错误，可以重试 |
| 1002 | `ATFOAdError.ERROR_NO_AD` | 无广告返回，显示默认内容 |
| 1003 | `ATFOAdError.ERROR_TIMEOUT` | 超时 |
| 1004 | `ATFOAdError.ERROR_INVALID_SLOT` | 无效广告位，检查配置 |
| 1005 | `ATFOAdError.ERROR_SDK_NOT_INIT` | SDK 未初始化，检查初始化流程 |
| 1006 | `ATFOAdError.ERROR_LOAD_FAILED` | 加载失败（同一 slotId 在途重复加载也会以此拒绝） |
| 1007 | `ATFOAdError.ERROR_RENDER_FAILED` | 渲染失败 |
| 1008 | `ATFOAdError.ERROR_THIRD_PARTY` | 三方广告源错误 |
| 1009 | `ATFOAdError.ERROR_BIDDING_FAILED` | 竞价失败 |
| 1010 | `ATFOAdError.ERROR_CONFIG_FAILED` | 配置获取失败 |
| 1011 | `ATFOAdError.ERROR_INVALID_RENDER_TYPE` | 渲染类型无效 |

处理示例：

```kotlin
override fun onAdLoadFailed(error: ATFOAdError) {
    when (error.code) {
        ATFOAdError.ERROR_NETWORK -> {
            // 网络错误，可以重试
            retryLoadAd()
        }
        ATFOAdError.ERROR_NO_AD -> {
            // 无广告返回，显示默认内容
            showDefaultContent()
        }
        ATFOAdError.ERROR_TIMEOUT -> {
            // 超时，可以增加超时时间重试
            loadAdWithLongerTimeout()
        }
        ATFOAdError.ERROR_INVALID_SLOT -> {
            // 无效广告位，检查配置
            Log.e("AdError", "广告位配置错误")
        }
        ATFOAdError.ERROR_SDK_NOT_INIT -> {
            // SDK未初始化，检查初始化流程
            Log.e("AdError", "SDK未初始化")
        }
        else -> {
            Log.e("AdError", "未知错误: ${error.message}")
        }
    }
}
```

## 八、隐私数据获取说明

### 1、聚合 SDK 收集的信息

| 参数 | 说明 |
|---|---|
| 操作系统 | 设备的操作系统（如：Android/iOS） |
| 系统版本名 | 设备的系统版本名 |
| 系统版本号 | 设备的系统版本号 |
| 应用包名 | 当前应用的包名 |
| 应用版本名 | 当前应用版本名 |
| 设备品牌 | 移动设备的品牌名称 |
| 设备型号 | 移动设备的型号名称 |
| 分辨率 | 设备的屏幕分辨率 |
| 屏幕方向 | 设备的屏幕方向（如：1：竖屏，2：横屏） |
| 网络类型 | 网络类型（如：WiFi、3G、4G、5G），可在初始化的时候关闭 |
| 移动网络代码 | 移动设备网络代码 |
| 移动国家代码 | 移动设备国家代码 |
| 系统语言 | 系统语言（如：zh-CN） |
| 时区 | 时区（如：GMT+08:00） |
| User Agent | User Agent 信息 |
| AndroidId | 设备广告标识符 |

### 2、三方广告平台隐私政策

| 第三方平台 | 参考网址 |
|---|---|
| 穿山甲 | [开发者个人信息保护合规指引-开发者个人信息保护合规指引-业务规范-穿山甲广告平台](https://www.csjplatform.com/supportcenter/5452) |
| GroMore 穿山甲融合 | [GroMore隐私合规指南-使用指南-操作指南-GroMore-穿山甲广告平台](https://www.csjplatform.com/supportcenter/5879) |
| 优量汇 | 隐私说明：<br/>[优量汇 SDK个人信息保护规则](https://e.qq.com/dev/help_detail.html?cid=2005&pid=5983)<br/>合规指南：<br/>https://e.qq.com/dev/help_detail.html?cid=2004&pid=5795 |
| 快手 | [快手联盟](https://u.kuaishou.com/home/help/detail/1337/1400/1406) |
| 百度 | [百度联盟](https://union.baidu.com/bqt/#/legal/policies) |
| 美数 | [美数](https://admate.yuque.com/se0re5/behodv/nbsr012s6fl3vxni) |
| 倍孜 | http://sdkdoc.beizi.biz/#/zh-cn/guide/UsePrivacy |
| 汇川 | https://pixiu-ssp.alibaba.com/privacy_agreement.html |
| 趣盟 | 隐私协议：https://docs.yunxish.com/privacy/common/privacy-agreement.html |
| Sigmob | — |
| 优酷 | 隐私政策：https://terms.alicdn.com/legal-agreement/terms/privacy_policy_full/20251124233431381/20251124233431381.html<br/>合规说明：https://terms.alicdn.com/legal-agreement/terms/privacy_other/20251125174230778/20251125174230778.html |
| 旺脉 | 隐私条款：https://static.adwangmai.com/privacy-MaxMindSDK.html |

## 九、联调排查

> 为方便联调，SDK 内置了日志导出页面。遇到广告加载、展示异常等问题时，可先导出日志，发给我们研发同学分析定位。
>
> 打开日志导出页面的命令：
>
> ```bash
> adb shell am start -n 你的App包名/com.atfo.ad.debug.ATFODebugActivity
> ```
>
> 在页面中导出日志后，把日志文件发给对接的研发即可。

## 十、注意事项

1. **生命周期管理**：在 Activity 销毁时调用 `adObject.destroy()`
2. **网络权限**：确保添加了网络权限
3. **调试模式**：开发阶段建议开启 `debug(true)`
4. **三方 SDK**：如需使用其他广告源，请添加对应 adapter 依赖（三方 SDK 由 adapter 传递引入）并在初始化前注册
5. **广告显示**：广告加载成功后需要调用 `showAd()` 方法显示广告
6. **错误处理**：实现完善的错误处理和重试机制
7. **内存管理**：及时销毁广告对象，避免内存泄漏
8. **超时管理**：超时时间以管理后台下发为准（未下发时默认 5000ms），建议不在本地通过 `timeout()` 配置

## 十一、技术支持

- **技术支持**：support@atfo.com
- **版本**：2.9.5
- **最低支持**：Android API 23（Android 6.0）

## 十二、SDK 涉及的三方库版本（若存在不兼容等问题，请联系开发进行定制化修改）

```toml
[versions]
# 核心版本
agp = "7.0.4"
kotlin = "1.5.21"

# AndroidX
coreKtx = "1.3.2"
appcompat = "1.2.0"
constraintlayout = "2.1.4"

# 网络和媒体
glide = "4.11.0"
okhttp = "4.9.0"
gson = "2.8.6"

# 协程和工具
coroutines = "1.5.2"
mmkv = "1.2.14"

# Architecture components
room = "2.3.0"

[libraries]

# AndroidX核心
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
androidx-cardview = { group = "androidx.cardview", name = "cardview", version = "1.0.0" }

# 协程
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }

# 媒体
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }

# 网络（SDK 仅直接使用 OkHttp；接入方需引入 okhttp）
okhttp-core = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }

# 存储
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
mmkv = { group = "com.tencent", name = "mmkv-static", version.ref = "mmkv" }

# 数据库和后台
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```
