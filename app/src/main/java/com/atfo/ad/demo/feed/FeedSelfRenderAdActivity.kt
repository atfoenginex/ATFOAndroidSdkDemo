package com.atfo.ad.demo.feed

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.atfo.ad.core.ATFOAdLoader
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityFeedSelfRenderAdBinding
import com.atfo.ad.demo.view.SelfRenderAdView
import com.atfo.ad.listener.ATFOAdError
import com.atfo.ad.listener.AdInteractionListener
import com.atfo.ad.model.ATFOAd
import com.atfo.ad.model.ATFOAdSlot
import com.atfo.ad.model.AdType
import com.atfo.ad.model.hasRenderableMaterial

/**
 * 信息流广告（宿主自渲染，render_type=1）：宿主按物料自建 UI，再走
 * [com.atfo.ad.model.ATFOAd.registerInteraction] 绑定点击/关闭与曝光，上报仍由 SDK 统一负责。
 * 模板渲染物料请看 [FeedAdActivity]。代码位取 `atfo.feedSelfRenderSlotId`，后台需配成「自渲染」。
 * 进页面不自动加载，点「加载广告」才发起请求，加载成功后点「展示广告」。
 * 本页只负责展示与回调，每个回调都打日志并弹 Toast，不做任何跳转。
 */
class FeedSelfRenderAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedSelfRenderAdBinding
    private var loader: ATFOAdLoader? = null
    private var feedAdObject: ATFOAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 状态栏白底黑字：页面背景铺到状态栏后面（见布局的白色背景），图标改成深色
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        binding = ActivityFeedSelfRenderAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLoad.setOnClickListener { loadFeedAd() }
        binding.btnShow.setOnClickListener { showFeedAd() }
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

    private fun loadFeedAd() {
        // 重新加载前先释放上一次的广告对象与请求
        feedAdObject?.destroy()
        feedAdObject = null
        loader?.cancelLoad()
        binding.adContainer.removeAllViews()

        val adSlot = ATFOAdSlot.Builder()
            .adType(AdType.FEED)
            .activity(this)
            .slotId(DemoConfig.feedSelfRenderSlotId)
            .size(1080, 1080)
            .extras(mapOf("scene" to "feed_list"))
            .build()

        val adLoader = ATFOAdLoader(this, DemoConfig.debug)
        loader = adLoader
        adLoader.loadAd(adSlot, object : ATFOAdLoader.AdLoadListener {
            override fun onAdLoadSuccess(adObject: ATFOAd) {
                val msg = "信息流自渲染广告加载成功, adId=${adObject.adId}, price=${adObject.price}, " +
                        "renderType=${if (adObject.isNativeExpress()) "模板" else "自渲染"}"
                Log.d(TAG, msg)
                toast(msg)
                feedAdObject = adObject
            }

            override fun onAdLoadFailed(error: ATFOAdError) {
                val msg = "信息流自渲染广告加载失败, code=${error.code}, message=${error.message}"
                Log.e(TAG, msg)
                toast(msg)
            }
        })
    }

    private fun showFeedAd() {
        val adObject = feedAdObject
        if (adObject == null) {
            val msg = "尚未加载到广告，请先点击「加载广告」"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        if (!adObject.isValid()) {
            val msg = "信息流自渲染广告已失效，请重新加载"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        if (adObject.isNativeExpress()) {
            val msg = "该代码位返回的是模板渲染物料，请检查后台代码位配置"
            Log.w(TAG, msg)
            toast(msg)
            return
        }
        showSelfRenderAd(adObject)
    }

    /** 宿主自渲染：优先使用 SDK 提供的 View，其次按物料自建 UI。 */
    private fun showSelfRenderAd(adObject: ATFOAd) {
        val info = adObject.nativeInfo
        if (info == null || !info.hasRenderableMaterial()) {
            val msg = "自渲染广告物料为空或不足"
            Log.e(TAG, msg)
            toast(msg)
            return
        }

        val sdkView = adObject.getAdView()
        if (sdkView != null) {
            binding.adContainer.addView(
                sdkView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
            adObject.registerInteraction(
                container = binding.adContainer,
                clickableViews = listOf(sdkView),
                closeViews = emptyList(),
                interactionListener = interactionListener(),
                adLogoParams = adLogoParams()
            )
            return
        }

        // sdkVideoRender=true 时视频由三方 SDK 渲染，宿主不能自行用 videoUrl 播放
        if (info.sdkVideoRender) {
            val msg = "sdkVideoRender=true 但 getAdView() 为空，无法展示"
            Log.e(TAG, msg)
            toast(msg)
            return
        }

        val selfRenderView = SelfRenderAdView(this).apply { bind(info) }
        binding.adContainer.addView(
            selfRenderView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        // 容器要先加入视图树，再绑定交互
        // 宿主自己渲染 adLogo（见 SelfRenderAdView），这里不传 adLogoParams，避免两个角标
        adObject.registerInteraction(
            container = selfRenderView,
            clickableViews = selfRenderView.clickableViews(),
            closeViews = selfRenderView.closeableViews(),
            interactionListener = interactionListener()
        )
    }

    private fun adLogoParams() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = Gravity.END or Gravity.TOP
        val gap = (8f * resources.displayMetrics.density).toInt()
        marginStart = gap
        topMargin = gap
    }

    private fun interactionListener(): AdInteractionListener = object : AdInteractionListener {
        override fun onAdShowSuccess() {
            val msg = "信息流自渲染广告展示成功"
            Log.d(TAG, msg)
            toast(msg)
        }

        override fun onAdShowFailed(errorCode: Int, errorMessage: String) {
            val msg = "信息流自渲染广告展示失败, code=$errorCode, message=$errorMessage"
            Log.e(TAG, msg)
            toast(msg)
        }

        override fun onAdClicked() {
            val msg = "信息流自渲染广告被点击"
            Log.d(TAG, msg)
            toast(msg)
        }

        override fun onAdClosed() {
            val msg = "信息流自渲染广告被关闭"
            Log.d(TAG, msg)
            toast(msg)
        }

        override fun onAdRenderFail(errorCode: Int, errorMessage: String) {
            val msg = "信息流自渲染广告渲染失败, code=$errorCode, message=$errorMessage"
            Log.e(TAG, msg)
            toast(msg)
        }

        override fun onAdExposed() {
            val msg = "信息流自渲染广告曝光"
            Log.d(TAG, msg)
            toast(msg)
        }

        override fun onReward() {
            val msg = "信息流自渲染广告收到奖励回调"
            Log.d(TAG, msg)
            toast(msg)
        }
    }

    /** 回调文案同步弹 Toast，不连 adb 也能看到状态。 */
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        feedAdObject?.destroy()
        feedAdObject = null
        loader?.cancelLoad()
        binding.adContainer.removeAllViews()
    }

    private companion object {
        const val TAG = "ATFOFeedSelfRenderAdActivity"
    }
}
