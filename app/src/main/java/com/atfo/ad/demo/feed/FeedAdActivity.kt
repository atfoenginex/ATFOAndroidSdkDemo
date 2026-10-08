package com.atfo.ad.demo.feed

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.R
import com.atfo.ad.demo.view.SelfRenderAdView
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType
import com.atfo.ad.model.hasRenderableMaterial

/**
 * 信息流广告：模板物料走 [com.atfo.ad.model.ATFOAd.showAd]，自渲染物料（render_type=1）由宿主构建 UI 后
 * 走 [com.atfo.ad.model.ATFOAd.registerInteraction]，曝光/点击/关闭仍由 SDK 统一上报。
 */
class FeedAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var loader: ATFOAdLoader? = null
    private var feedAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_container)
        adContainer = findViewById(R.id.ad_container)
        loadFeedAd()
    }

    private fun loadFeedAd() {
        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.FEED)
            .activity(this)
            .slotId(DemoConfig.slotIdOf(AdType.FEED))
            .size(1080, 1080)
            .extras(mapOf("scene" to "feed_list"))
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                Log.d(TAG, "信息流广告加载成功, adId=${adObject.adId}, price=${adObject.price}, " +
                        "renderType=${if (adObject.isNativeExpress()) "模板" else "自渲染"}")
                feedAdObject = adObject
                showFeedAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e(TAG, "信息流广告加载失败, code=${error.code}, message=${error.message}")
            }
        })
    }

    private fun showFeedAd() {
        val adObject = feedAdObject ?: return
        if (!adObject.isValid()) {
            Log.w(TAG, "信息流广告已失效")
            return
        }
        if (adObject.isNativeExpress()) {
            adObject.showAd(adContainer, interactionListener())
        } else {
            showSelfRenderAd(adObject)
        }
    }

    /** 宿主自渲染：优先使用 SDK 提供的 View，其次按物料自建 UI。 */
    private fun showSelfRenderAd(adObject: ATFOAd) {
        val info = adObject.nativeInfo
        if (info == null || !info.hasRenderableMaterial()) {
            Log.e(TAG, "自渲染广告物料为空或不足")
            return
        }

        val sdkView = adObject.getAdView()
        if (sdkView != null) {
            adContainer.addView(
                sdkView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            adObject.registerInteraction(
                container = adContainer,
                clickableViews = listOf(sdkView),
                closeViews = emptyList(),
                interactionListener = interactionListener(),
                adLogoParams = adLogoParams()
            )
            return
        }

        // sdkVideoRender=true 时视频由三方 SDK 渲染，宿主不能自行用 videoUrl 播放
        if (info.sdkVideoRender) {
            Log.e(TAG, "sdkVideoRender=true 但 getAdView() 为空，无法展示")
            return
        }

        val selfRenderView = SelfRenderAdView(this).apply { bind(info) }
        adContainer.addView(
            selfRenderView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        // 容器要先加入视图树，再绑定交互
        // 宿主自己渲染 adLogo（见 SelfRenderAdView），这里不传 adLogoParams，避免两个角标
        adObject.registerInteraction(
            container = selfRenderView,
            clickableViews = selfRenderView.clickableViews(),
            closeViews = selfRenderView.closeableViews(),
            interactionListener = interactionListener()
        )
    }

    private fun adLogoParams() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = Gravity.END or Gravity.TOP
        val gap = (8f * resources.displayMetrics.density).toInt()
        marginStart = gap
        topMargin = gap
    }

    private fun interactionListener(): AdInteractionListener = object : AdInteractionListener {
        override fun onAdShowSuccess() { Log.d(TAG, "信息流广告展示成功") }
        override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
            Log.e(TAG, "信息流广告展示失败, code=$errorCode, message=$errorMessage")
        }

        override fun onAdClicked() { Log.d(TAG, "信息流广告被点击") }
        override fun onAdClosed() { Log.d(TAG, "信息流广告被关闭") }
        override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
            Log.e(TAG, "信息流广告渲染失败, code=$errorCode, message=$errorMessage")
        }

        override fun onAdExposed() { Log.d(TAG, "信息流广告曝光") }
        override fun onReward() = Unit
    }

    override fun onDestroy() {
        super.onDestroy()
        feedAdObject?.destroy()
        feedAdObject = null
        loader?.cancelLoad()
        adContainer.removeAllViews()
    }

    private companion object {
        const val TAG = "ATFOFeedAdActivity"
    }
}