package com.atfo.ad.demo.reward

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
 * 激励视频：全屏展示，达标回调 [com.atfo.ad.listener.AdInteractionListener.onReward]，业务在此发奖。
 */
class RewardAdActivity : AppCompatActivity() {

    private var loader: ATFOAdLoader? = null
    private var rewardAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_container)
        loadRewardAd()
    }

    private fun loadRewardAd() {
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
                Log.d(TAG, "激励视频加载成功, adId=${adObject.adId}, price=${adObject.price}")
                rewardAdObject = adObject
                showRewardAd()
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                Log.e(TAG, "激励视频加载失败, code=${error.code}, message=${error.message}")
            }
        })
    }

    private fun showRewardAd() {
        val adObject = rewardAdObject ?: return
        if (!adObject.isValid()) {
            Log.w(TAG, "激励视频已失效")
            return
        }
        adObject.showAd(this@RewardAdActivity, object : AdInteractionListener {
            override fun onAdShowSuccess() { Log.d(TAG, "激励视频展示成功") }
            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "激励视频展示失败, code=$errorCode, message=$errorMessage")
            }

            override fun onAdClicked() { Log.d(TAG, "激励视频被点击") }
            override fun onAdClosed() {
                Log.d(TAG, "激励视频被关闭")
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                Log.e(TAG, "激励视频渲染失败, code=$errorCode, message=$errorMessage")

            }

            override fun onAdExposed() { Log.d(TAG, "激励视频曝光") }
            override fun onReward() {
                Log.d(TAG, "激励视频达标，发放奖励")
                Toast.makeText(this@RewardAdActivity, "奖励已发放", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        rewardAdObject?.destroy()
        rewardAdObject = null
        loader?.cancelLoad()
    }

    private companion object {
        const val TAG = "ATFORewardAdActivity"
    }
}