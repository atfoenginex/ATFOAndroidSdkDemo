package com.atfo.ad.demo.notify

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityNotifyAdBinding
import com.atfo.ad.demo.utils.applyStatusBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 通知广告：客户端按信息流（[com.atfo.ad.model.AdType.FEED]）拉取，由 SDK 模板渲染。
 * 后台需同时满足两项配置：渲染方式选「ATFO 模板渲染」，模板选「信息流-通知广告」。
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class NotifyAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotifyAdBinding
    private var loader: ATFOAdLoader? = null
    private var notifyAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusBar()
        binding = ActivityNotifyAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadNotifyAd() }
        binding.btnShow.setOnClickListener { showNotifyAd() }
        binding.btnBack.setOnClickListener { finish() }
        binding.buttonBar.applyStatusBarPadding()
    }

    private fun loadNotifyAd() {
        // 重新加载前先释放上一次的广告对象，并取消在途请求
        releaseAd()
        loader?.cancelLoad()
        binding.adContainer.removeAllViews()

        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.FEED)
            .activity(this)
            .slotId(DemoConfig.slotIdOf(AdType.FEED, DemoConfig.SCENE_NOTIFY))
            .size(1000, 1000)
            .extras(mapOf("scene" to DemoConfig.SCENE_NOTIFY))
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                val msg = "通知广告加载成功, adId=${adObject.adId}, price=${adObject.price}"
                logAndToast(msg)
                notifyAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "通知广告加载失败, code=${error.code}, message=${error.message}"
                logAndToast(msg, Log.ERROR)
            }
        })
    }

    private fun showNotifyAd() {
        val adObject = notifyAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isValid()) {
            val msg = "通知广告已失效，请重新加载"
            logAndToast(msg, Log.WARN)
            return
        }
        adObject.showAd(binding.adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "通知广告展示成功"
                logAndToast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "通知广告展示失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdClicked() {
                val msg = "通知广告被点击"
                logAndToast(msg)
            }

            override fun onAdClosed() {
                val msg = "通知广告被关闭"
                logAndToast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "通知广告渲染失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdExposed() {
                val msg = "通知广告曝光"
                logAndToast(msg)
            }

            override fun onReward() {
                val msg = "通知广告收到奖励回调"
                logAndToast(msg)
            }
        })
    }

    /** 释放当前广告对象；重新加载前与 onDestroy 调用。 */
    private fun releaseAd() {
        notifyAdObject?.destroy()
        notifyAdObject = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAd()
        loader?.cancelLoad()
        loader = null
        binding.adContainer.removeAllViews()
    }
}
