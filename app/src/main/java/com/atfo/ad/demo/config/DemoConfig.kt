package com.atfo.ad.demo.config

import com.atfo.ad.core.ATFOSDKConfig
import com.atfo.ad.demo.BuildConfig
import com.atfo.ad.model.AdType

/**
 * 接入参数来自 local.properties 的 atfo.* 配置项（由 app/build.gradle.kts 注入 BuildConfig）。
 * 切换账户或代码位只改配置，不改代码。
 */
object DemoConfig {

    val appId: String = BuildConfig.ATFO_APP_ID
    val secret: String = BuildConfig.ATFO_SECRET
    val mediaId: String = BuildConfig.ATFO_MEDIA_ID
    val appName: String = BuildConfig.ATFO_APP_NAME
    val debug: Boolean = BuildConfig.ATFO_DEBUG
    val environment: ATFOSDKConfig.Environment =
        ATFOSDKConfig.Environment.valueOf(BuildConfig.ATFO_ENV)

    /** 未配置真实账户时的占位标记，页面据此提示而不是静默失败。 */
    fun isAccountConfigured(): Boolean =
        !appId.startsWith("your_") && !secret.startsWith("your_")

    /**
     * 取代码位 Id。通知广告复用 [com.atfo.ad.model.AdType.FEED]，按 scene 区分，其余类型直接映射。
     */
    fun slotIdOf(adType: AdType, scene: String? = null): String =
        if (scene == SCENE_NOTIFY) BuildConfig.ATFO_NOTIFY_SLOT_ID
        else when (adType) {
            AdType.SPLASH -> BuildConfig.ATFO_SPLASH_SLOT_ID
            AdType.FEED -> BuildConfig.ATFO_FEED_SLOT_ID
            AdType.INTERSTITIAL -> BuildConfig.ATFO_INTERSTITIAL_SLOT_ID
            AdType.REWARD -> BuildConfig.ATFO_REWARD_SLOT_ID
            AdType.UNKNOWN -> ""
        }

    /**
     * 信息流自渲染代码位。后台按代码位区分渲染方式：
     * `atfo.feedSlotId` 配成「ATFO 模板渲染」，`atfo.feedSelfRenderSlotId` 配成「自渲染」。
     */
    val feedSelfRenderSlotId: String = BuildConfig.ATFO_FEED_SELF_RENDER_SLOT_ID

    /** 通知广告的场景值，后台需把该代码位配成「ATFO 模板渲染 + 信息流-通知广告」。 */
    const val SCENE_NOTIFY = "notify_ad"

    fun describe(): String = buildString {
        append("appId=").append(appId)
        append(", mediaId=").append(mediaId)
        append(", env=").append(environment)
        append(", debug=").append(debug)
    }
}