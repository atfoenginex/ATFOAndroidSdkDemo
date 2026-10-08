package com.atfo.ad.demo.splash

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivitySplashAdBinding
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
        // 状态栏白底黑字：页面背景铺到状态栏后面（见布局的白色背景），图标改成深色
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        binding = ActivitySplashAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadSplashAd() }
        binding.btnShow.setOnClickListener { showSplashAd() }
        binding.btnBack.setOnClickListener { finish() }
        applyStatusBarPadding(binding.buttonBar)
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
                Log.d(TAG, msg)
                toast(msg)
                splashAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "开屏广告加载失败, code=${error.code}, message=${error.message}"
                Log.e(TAG, msg)
                toast(msg)
            }
        })
    }

    private fun showSplashAd() {
        val adObject = splashAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        if (!adObject.isValid()) {
            val msg = "开屏广告已失效，请重新加载"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        adObject.showAd(binding.adContainer, object : AdInteractionListener {
            override fun onAdShowSuccess() {
                val msg = "开屏广告展示成功"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
                val msg = "开屏广告展示失败, code=$errorCode, message=$errorMessage"
                Log.e(TAG, msg)
                toast(msg)
            }

            override fun onAdClicked() {
                val msg = "开屏广告被点击"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdClosed() {
                val msg = "开屏广告被关闭"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
                val msg = "开屏广告渲染失败, code=$errorCode, message=$errorMessage"
                Log.e(TAG, msg)
                toast(msg)
            }

            override fun onAdExposed() {
                val msg = "开屏广告曝光"
                Log.d(TAG, msg)
                toast(msg)
            }

            override fun onReward() {
                val msg = "开屏广告收到奖励回调"
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
        splashAdObject?.destroy()
        splashAdObject = null
        loader?.cancelLoad()
    }

    private companion object {
        const val TAG = "ATFOSplashAdActivity"
    }
}