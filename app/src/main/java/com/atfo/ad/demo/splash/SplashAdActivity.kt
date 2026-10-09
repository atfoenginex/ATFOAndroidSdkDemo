package com.atfo.ad.demo.splash

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivitySplashAdBinding
import com.atfo.ad.demo.utils.applyStatusBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 开屏广告：进页面不自动加载，点「加载广告」才发起请求，成功后点「展示广告」渲染到容器。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 * timeout 不在本地设置，统一由管理后台下发。
 */
class SplashAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashAdBinding
    private var loader: ATFOAdLoader? = null
    private var splashAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightStatusBar()
        binding = ActivitySplashAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadSplashAd() }
        binding.btnShow.setOnClickListener { showSplashAd() }
        binding.btnBack.setOnClickListener { finish() }
        binding.buttonBar.applyStatusBarPadding()
    }

    private fun loadSplashAd() {
        // 重新加载前先释放上一次的广告对象与请求
        splashAdObject?.destroy()
        splashAdObject = null
        loader?.cancelLoad()
        binding.adContainer.removeAllViews()

        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.SPLASH)
            .activity(this)
            .slotId(DemoConfig.slotIdOf(AdType.SPLASH))
            .size(1080, 1920)
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                val msg = "开屏广告加载成功, adId=${adObject.adId}, price=${adObject.price}"
                logAndToast(msg)
                splashAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "开屏广告加载失败, code=${error.code}, message=${error.message}"
                logAndToast(msg, Log.ERROR)
            }
        })
    }

    private fun showSplashAd() {
        val adObject = splashAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            logAndToast(msg, Log.WARN)
            return
        }
        if (!adObject.isValid()) {
            val msg = "开屏广告已失效，请重新加载"
            logAndToast(msg, Log.WARN)
            return
        }
        adObject.showAd(binding.adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "开屏广告展示成功"
                logAndToast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "开屏广告展示失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdClicked() {
                val msg = "开屏广告被点击"
                logAndToast(msg)
            }

            override fun onAdClosed() {
                val msg = "开屏广告被关闭"
                logAndToast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "开屏广告渲染失败, code=$errorCode, message=$errorMessage"
                logAndToast(msg, Log.ERROR)
            }

            override fun onAdExposed() {
                val msg = "开屏广告曝光"
                logAndToast(msg)
            }

            override fun onReward() {
                val msg = "开屏广告收到奖励回调"
                logAndToast(msg)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        splashAdObject?.destroy()
        splashAdObject = null
        loader?.cancelLoad()
    }
}