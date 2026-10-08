package com.atfo.ad.demo.interstitial

import android.os.Bundle
import android.util.Log
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
 * 插屏广告：全屏展示，直接传入 Activity。
 */
class InterstitialAdActivity : AppCompatActivity() {

    private var loader: ATFOAdLoader? = null
    private var interstitialAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_container)
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
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
                Log.d(TAG, "插屏广告加载成功, adId=${adObject.adId}, price=${adObject.price}")
                interstitialAdObject = adObject
                showInterstitialAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e(TAG, "插屏广告加载失败, code=${error.code}, message=${error.message}")
            }
        })
    }

    private fun showInterstitialAd() {
        val adObject = interstitialAdObject ?: return
        if (!adObject.isValid()) {
            Log.w(TAG, "插屏广告已失效")
            return
        }
        adObject.showAd(this@InterstitialAdActivity, object : AdInteractionListener {
            override fun onAdShowSuccess() { Log.d(TAG, "插屏广告展示成功") }
            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "插屏广告展示失败, code=$errorCode, message=$errorMessage")
            }

            override fun onAdClicked() { Log.d(TAG, "插屏广告被点击") }
            override fun onAdClosed() {
                Log.d(TAG, "插屏广告被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "插屏广告渲染失败, code=$errorCode, message=$errorMessage")

            }

            override fun onAdExposed() { Log.d(TAG, "插屏广告曝光") }
            override fun onReward() = Unit
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAdObject?.destroy()
        interstitialAdObject = null
        loader?.cancelLoad()
    }

    private companion object {
        const val TAG = "ATFOInterstitialAdActivity"
    }
}