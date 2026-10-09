package com.atfo.ad.demo.feed

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityFeedAdBinding
import com.atfo.ad.demo.utils.applyStatusBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 信息流广告（模板渲染）：物料由 SDK 渲染，直接 [com.atfo.ad.model.ATFOAd.showAd] 到容器。
 * 代码位取 `atfo.feedSlotId`，后台需配成「ATFO 模板渲染」；自渲染物料见 [FeedSelfRenderAdActivity]。
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class FeedAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedAdBinding
    private var loader: ATFOAdLoader? = null
    private var feedAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusBar()
        binding = ActivityFeedAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadFeedAd() }
        binding.btnShow.setOnClickListener { showFeedAd() }
        binding.btnBack.setOnClickListener { finish() }
        binding.buttonBar.applyStatusBarPadding()
    }

    private fun loadFeedAd() {
        // 重新加载前先释放上一次的广告对象，并取消在途请求
        releaseAd()
        loader?.cancelLoad()
        binding.adContainer.removeAllViews()

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
                val msg = "信息流广告加载成功, adId=${adObject.adId}, price=${adObject.price}, " +
                        "renderType=${if (adObject.isNativeExpress()) "模板" else "自渲染"}"
                logAndToast(msg)
                feedAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "信息流广告加载失败, code=${error.code}, message=${error.message}"
                logAndToast(msg, Log.ERROR)
            }
        })
    }

    private fun showFeedAd() {
        val adObject = feedAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isValid()) {
            val msg = "信息流广告已失效，请重新加载"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isNativeExpress()) {
            val msg = "该代码位返回的是自渲染物料，请用信息流自渲染页展示"
            logAndToast(msg, Log.WARN)
            return
        }
        adObject.showAd(binding.adContainer, interactionListener())
    }

    private fun interactionListener(): AdInteractionListener = object : AdInteractionListener {
        override fun onAdShowSuccess() {
            val msg = "信息流广告展示成功"
            logAndToast(msg)
        }

        override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
            val msg = "信息流广告展示失败, code=$errorCode, message=$errorMessage"
            logAndToast(msg, Log.ERROR)
        }

        override fun onAdClicked() {
            val msg = "信息流广告被点击"
            logAndToast(msg)
        }

        override fun onAdClosed() {
            val msg = "信息流广告被关闭"
            logAndToast(msg)
        }

        override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
            val msg = "信息流广告渲染失败, code=$errorCode, message=$errorMessage"
            logAndToast(msg, Log.ERROR)
        }

        override fun onAdExposed() {
            val msg = "信息流广告曝光"
            logAndToast(msg)
        }

        override fun onReward() {
            val msg = "信息流广告收到奖励回调"
            logAndToast(msg)
        }
    }

    /** 释放当前广告对象；重新加载前与 onDestroy 调用。 */
    private fun releaseAd() {
        feedAdObject?.destroy()
        feedAdObject = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAd()
        loader?.cancelLoad()
        loader = null
        binding.adContainer.removeAllViews()
    }
}
