package com.atfo.ad.demo.reward

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
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
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class RewardAdActivity : AppCompatActivity() {

    private var loader: ATFOAdLoader? = null
    private var rewardAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 状态栏白底黑字：页面背景铺到状态栏后面（见布局的白色背景），图标改成深色
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        setContentView(R.layout.activity_reward_ad)
        findViewById<Button>(R.id.btn_load).setOnClickListener { loadRewardAd() }
        findViewById<Button>(R.id.btn_show).setOnClickListener { showRewardAd() }
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
        applyStatusBarPadding(findViewById(R.id.button_bar))
    }

    /** targetSdk 36 起默认边到边，顶部按钮条会被状态栏盖住，这里让出状态栏高度。 */
    private fun applyStatusBarPadding(buttonBar: View) {
        val basePaddingTop = buttonBar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(buttonBar) { view, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.updatePadding(top = basePaddingTop + statusBarTop)
            insets
        }
    }

    private fun loadRewardAd() {
        // 重新加载前先释放上一次的广告对象与请求
        rewardAdObject?.destroy()
        rewardAdObject = null
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
                Log.d(TAG, msg)
                toast(msg)
                rewardAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "激励视频加载失败, code=${error.code}, message=${error.message}"
                Log.e(TAG, msg)
                toast(msg)
            }
        })
    }

    private fun showRewardAd() {
        val adObject = rewardAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        if (!adObject.isValid()) {
            val msg = "激励视频已失效，请重新加载"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        adObject.showAd(this@RewardAdActivity, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "激励视频展示成功"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "激励视频展示失败, code=$errorCode, message=$errorMessage"
                Log.e(TAG, msg)
                toast(msg)
            }

            override fun onAdClicked() {
                val msg = "激励视频被点击"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdClosed() {
                val msg = "激励视频被关闭"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "激励视频渲染失败, code=$errorCode, message=$errorMessage"
                Log.e(TAG, msg)
                toast(msg)
            }

            override fun onAdExposed() {
                val msg = "激励视频曝光"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onReward() {
                // 达标回调即发奖时机，真实业务在这里发放奖励
                val msg = "激励视频达标，发放奖励"
                Log.d(TAG, msg)
                toast(msg)
            }
        })
    }

    /** 回调文案同步弹 Toast，不连 adb 也能看到状态。 */
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
