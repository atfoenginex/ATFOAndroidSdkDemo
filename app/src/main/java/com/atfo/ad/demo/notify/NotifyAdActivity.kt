package com.atfo.ad.demo.notify

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
 * 通知广告：客户端按信息流（[com.atfo.ad.model.AdType.FEED]）拉取，由 SDK 模板渲染。
 * 后台需同时满足两项配置：渲染方式选「ATFO 模板渲染」，模板选「信息流-通知广告」。
 */
class NotifyAdActivity : AppCompatActivity() {

    private lateinit var adContainer: FrameLayout
    private var loader: ATFOAdLoader? = null
    private var notifyAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_container)
        adContainer = findViewById(R.id.ad_container)
        loadNotifyAd()
    }

    private fun loadNotifyAd() {
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
                Log.d(TAG, "通知广告加载成功, adId=${adObject.adId}, price=${adObject.price}")
                notifyAdObject = adObject
                showNotifyAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e(TAG, "通知广告加载失败, code=${error.code}, message=${error.message}")
            }
        })
    }

    private fun showNotifyAd() {
        val adObject = notifyAdObject ?: return
        if (!adObject.isValid()) {
            Log.w(TAG, "通知广告已失效")
            return
        }
        adObject.showAd(adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() { Log.d(TAG, "通知广告展示成功") }
            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "通知广告展示失败, code=$errorCode, message=$errorMessage")
            }

            override fun onAdClicked() { Log.d(TAG, "通知广告被点击") }
            override fun onAdClosed() { Log.d(TAG, "通知广告被关闭") }
            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "通知广告渲染失败, code=$errorCode, message=$errorMessage")
            }

            override fun onAdExposed() { Log.d(TAG, "通知广告曝光") }
            override fun onReward() = Unit
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        notifyAdObject?.destroy()
        notifyAdObject = null
        loader?.cancelLoad()
        adContainer.removeAllViews()
    }

    private companion object {
        const val TAG = "ATFONotifyAdActivity"
    }
}