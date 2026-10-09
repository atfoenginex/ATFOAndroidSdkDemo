package com.atfo.ad.demo.reward

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityRewardAdBinding
import com.atfo.ad.demo.utils.applyStatusBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 激励视频：全屏展示，达标回调 [com.atfo.ad.listener.AdInteractionListener.onReward]，业务在此发奖。
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class RewardAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardAdBinding
    private var loader: ATFOAdLoader? = null
    private var rewardAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusBar()
        binding = ActivityRewardAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadRewardAd() }
        binding.btnShow.setOnClickListener { showRewardAd() }
        binding.btnBack.setOnClickListener { finish() }
        binding.buttonBar.applyStatusBarPadding()
    }

    private fun loadRewardAd() {
        // 重新加载前先释放上一次的广告对象，并取消在途请求
        releaseAd()
        loader?.cancelLoad()

        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.REWARD)
            .activity(this)
            .slotId(DemoConfig.slotIdOf(AdType.REWARD))
            .size(720, 1280)
            .extras(mapOf("scene" to "reward"))
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                val msg = "激励视频加载成功, adId=${adObject.adId}, price=${adObject.price}"
                logAndToast(msg)
                rewardAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "激励视频加载失败, code=${error.code}, message=${error.message}"
                logAndToast(msg, Log.ERROR)
            }
        })
    }

    private fun showRewardAd() {
        val adObject = rewardAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isValid()) {
            val msg = "激励视频已失效，请重新加载"
            logAndToast(msg, Log.WARN)
            return
        }
        adObject.showAd(this@RewardAdActivity, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "激励视频展示成功"
                logAndToast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "激励视频展示失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdClicked() {
                val msg = "激励视频被点击"
                logAndToast(msg)
            }

            override fun onAdClosed() {
                val msg = "激励视频被关闭"
                logAndToast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "激励视频渲染失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdExposed() {
                val msg = "激励视频曝光"
                logAndToast(msg)
            }

            override fun onReward() {
                // 达标回调即发奖时机，真实业务在这里发放奖励
                val msg = "激励视频达标，发放奖励"
                logAndToast(msg)
            }
        })
    }

    /** 释放当前广告对象；重新加载前与 onDestroy 调用。 */
    private fun releaseAd() {
        rewardAdObject?.destroy()
        rewardAdObject = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAd()
        loader?.cancelLoad()
        loader = null
    }
}
