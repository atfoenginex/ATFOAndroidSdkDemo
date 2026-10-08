package com.atfo.ad.demo.splash

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.R
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType

/**
 * 开屏广告：加载成功后渲染到容器，本页只负责展示与回调日志。
 * timeout 不在本地设置，统一由管理后台下发。
 */
class SplashAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var loader: ATFOAdLoader? = null
    private var splashAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_container)
        adContainer = findViewById(R.id.ad_container)
        loadSplashAd()
    }

    private fun loadSplashAd() {
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
                Log.d(TAG, "开屏广告加载成功, adId=${adObject.adId}, price=${adObject.price}")
                splashAdObject = adObject
                showSplashAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e(TAG, "开屏广告加载失败, code=${error.code}, message=${error.message}")
            }
        })
    }

    private fun showSplashAd() {
        val adObject = splashAdObject ?: return
        if (!adObject.isValid()) {
            Log.w(TAG, "开屏广告已失效")
            return
        }
        adObject.showAd(adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() { Log.d(TAG, "开屏广告展示成功") }
            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "开屏广告展示失败, code=$errorCode, message=$errorMessage")
            }

            override fun onAdClicked() {
                Log.d(TAG, "开屏广告被点击")
            }

            override fun onAdClosed() {
                Log.d(TAG, "开屏广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "开屏广告渲染失败, code=$errorCode, message=$errorMessage")

            }

            override fun onAdExposed() { Log.d(TAG, "开屏广告曝光") }
            override fun onReward() = Unit
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        splashAdObject?.destroy()
        splashAdObject = null
        loader?.cancelLoad()
    }

    private companion object {
        const val TAG = "ATFOSplashAdActivity"
    }
}