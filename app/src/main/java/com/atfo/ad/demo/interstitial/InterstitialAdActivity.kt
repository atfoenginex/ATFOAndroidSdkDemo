package com.atfo.ad.demo.interstitial

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityInterstitialAdBinding
import com.atfo.ad.demo.utils.applyStatusBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 插屏广告：全屏展示，直接传入 Activity。
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class InterstitialAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterstitialAdBinding
    private var loader: ATFOAdLoader? = null
    private var interstitialAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusBar()
        binding = ActivityInterstitialAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadInterstitialAd() }
        binding.btnShow.setOnClickListener { showInterstitialAd() }
        binding.btnBack.setOnClickListener { finish() }
        binding.buttonBar.applyStatusBarPadding()
    }

    private fun loadInterstitialAd() {
        // 重新加载前先释放上一次的广告对象与请求
        interstitialAdObject?.destroy()
        interstitialAdObject = null
        loader?.cancelLoad()

        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.INTERSTITIAL)
            .activity(this)
            .slotId(DemoConfig.slotIdOf(AdType.INTERSTITIAL))
            .size(1080, 1920)
            .extras(mapOf("scene" to "interstitial"))
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                val msg = "插屏广告加载成功, adId=${adObject.adId}, price=${adObject.price}"
                logAndToast(msg)
                interstitialAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "插屏广告加载失败, code=${error.code}, message=${error.message}"
                logAndToast(msg, Log.ERROR)
            }
        })
    }

    private fun showInterstitialAd() {
        val adObject = interstitialAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isValid()) {
            val msg = "插屏广告已失效，请重新加载"
            logAndToast(msg, Log.WARN)
            return
        }
        adObject.showAd(this@InterstitialAdActivity, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "插屏广告展示成功"
                logAndToast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "插屏广告展示失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdClicked() {
                val msg = "插屏广告被点击"
                logAndToast(msg)
            }

            override fun onAdClosed() {
                val msg = "插屏广告被关闭"
                logAndToast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "插屏广告渲染失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdExposed() {
                val msg = "插屏广告曝光"
                logAndToast(msg)
            }

            override fun onReward() {
                val msg = "插屏广告收到奖励回调"
                logAndToast(msg)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAdObject?.destroy()
        interstitialAdObject = null
        loader?.cancelLoad()
    }
}
