package com.atfo.ad.demo.config

import com.atfo.ad.config.ATFOAdConfigManager
import com.atfo.ad.core.ATFOSDKConfig
import com.atfo.ad.demo.BuildConfig
import com.atfo.ad.model.AdType

/**
 * 接入参数来自 local.properties 的 atfo.* 配置项（由 app/build.gradle.kts 注入 BuildConfig）。
 * 切换账户或代码位只改配置，不改代码。
 *
 * 代码位支持运行时覆盖：设置页选中的 slotId 存 [SlotOverrideStore]（MMKV），
 * 各页加载广告时经 [slotIdOf] 读取，下次加载即生效，无需重启。
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

    // ==== 可切换的代码位 key（设置页展示与 SlotOverrideStore 存储共用） ====
    // 6 个代码位、4 种广告类型：自渲染与通知在后台都属于「信息流」（AdType.FEED），
    // 候选列表按 FEED 过滤，但各自的 slotId 独立覆盖
    const val SLOT_SPLASH = "splash"
    const val SLOT_FEED = "feed"
    const val SLOT_FEED_SELF_RENDER = "feed_self_render"
    const val SLOT_INTERSTITIAL = "interstitial"
    const val SLOT_REWARD = "reward"
    const val SLOT_NOTIFY = "notify"

    /**
     * 取代码位 Id。通知广告复用 [com.atfo.ad.model.AdType.FEED]，按 scene 区分，其余类型直接映射。
     * 设置页覆盖优先于 BuildConfig，每个代码位独立切换。
     */
    fun slotIdOf(adType: AdType, scene: String? = null): String =
        if (scene == SCENE_NOTIFY) {
            effectiveSlotId(SLOT_NOTIFY)
        } else {
            when (adType) {
                AdType.SPLASH -> effectiveSlotId(SLOT_SPLASH)
                AdType.FEED -> effectiveSlotId(SLOT_FEED)
                AdType.INTERSTITIAL -> effectiveSlotId(SLOT_INTERSTITIAL)
                AdType.REWARD -> effectiveSlotId(SLOT_REWARD)
                AdType.UNKNOWN -> ""
            }
        }

    /**
     * 信息流自渲染代码位。后台按代码位区分渲染方式：
     * `atfo.feedSlotId` 配成「ATFO 模板渲染」，`atfo.feedSelfRenderSlotId` 配成「自渲染」。
     */
    val feedSelfRenderSlotId: String
        get() = effectiveSlotId(SLOT_FEED_SELF_RENDER)

    /** 通知广告的场景值，后台需把该代码位配成「ATFO 模板渲染 + 信息流-通知广告」。 */
    const val SCENE_NOTIFY = "notify_ad"

    /** 当前生效的代码位 Id（含设置页覆盖），设置页展示用。 */
    fun effectiveSlotId(slotKey: String): String =
        SlotOverrideStore.overrideOf(slotKey) ?: defaultSlotId(slotKey)

    /** local.properties 注入的默认代码位 Id（不含覆盖），设置页「恢复默认」用。 */
    fun defaultSlotId(slotKey: String): String = when (slotKey) {
        SLOT_SPLASH -> BuildConfig.ATFO_SPLASH_SLOT_ID
        SLOT_FEED -> BuildConfig.ATFO_FEED_SLOT_ID
        SLOT_FEED_SELF_RENDER -> BuildConfig.ATFO_FEED_SELF_RENDER_SLOT_ID
        SLOT_INTERSTITIAL -> BuildConfig.ATFO_INTERSTITIAL_SLOT_ID
        SLOT_REWARD -> BuildConfig.ATFO_REWARD_SLOT_ID
        SLOT_NOTIFY -> BuildConfig.ATFO_NOTIFY_SLOT_ID
        else -> ""
    }

    /**
     * 从 SDK 缓存的代码位配置（后台下发的 ad_setting）里取同类型候选 Id 列表。
     * 缓存为空（首次安装还没拉到配置）时返回空列表，由调用方提示。
     */
    fun candidateSlotIds(slotKey: String): List<String> {
        val adType = when (slotKey) {
            SLOT_SPLASH -> AdType.SPLASH
            // 自渲染与通知都属于「信息流」类型，候选用同一份 FEED 列表
            SLOT_FEED, SLOT_FEED_SELF_RENDER, SLOT_NOTIFY -> AdType.FEED
            SLOT_INTERSTITIAL -> AdType.INTERSTITIAL
            SLOT_REWARD -> AdType.REWARD
            else -> return emptyList()
        }
        // Companion 里私有 instance 属性会遮蔽同名合成属性，这里显式调 getInstance()
        val config = ATFOAdConfigManager.Companion.getInstance().getCachedConfig()
        return config?.adSetting
            ?.filter { it.adType == adType.value }
            ?.map { it.slotId }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?: emptyList()
    }

    fun describe(): String = buildString {
        append("appId=").append(appId)
        append(", mediaId=").append(mediaId)
        append(", env=").append(environment)
        append(", debug=").append(debug)
    }
}
