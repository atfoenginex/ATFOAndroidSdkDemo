package com.atfo.ad.demo.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import com.atfo.ad.demo.databinding.ViewSelfRenderAdBinding
import com.atfo.ad.model.ATFONativeFeedAdData
import com.atfo.ad.model.primaryImageUrl
import com.bumptech.glide.Glide

/**
 * 自渲染广告的宿主视图：左图右文卡片，素材全部来自 [com.atfo.ad.model.ATFONativeFeedAdData]，
 * 布局见 `view_self_render_ad.xml`。
 * 点击区域与关闭按钮通过 ATFOAd.registerInteraction 交回 SDK 处理。
 */
class SelfRenderAdView(context: Context) : FrameLayout(context) {

    private val binding =
        ViewSelfRenderAdBinding.inflate(LayoutInflater.from(context), this, true)

    fun bind(info: ATFONativeFeedAdData) {
        binding.title.text = info.title
        binding.desc.text = info.description
        binding.source.text = info.appInfo?.name ?: info.source
        binding.cta.text = info.buttonText

        info.primaryImageUrl()?.let { url -> Glide.with(this).load(url).into(binding.cover) }
        info.adLogo?.url?.let { url -> Glide.with(this).load(url).into(binding.adLogo) }
        playVideoIfNeed(info)
    }

    /** sdkVideoRender=false 时由宿主播放 videoUrl；为 true 时素材已由三方 SDK 渲染。 */
    private fun playVideoIfNeed(info: ATFONativeFeedAdData) {
        val videoUrl = info.videoUrl
        if (videoUrl.isNullOrEmpty() || info.sdkVideoRender) return

        val player: VideoView = binding.video
        val controller = MediaController(context).apply { setAnchorView(player) }
        // 有视频时不再展示封面图
        binding.coverCard.visibility = View.GONE
        player.visibility = View.VISIBLE
        player.setVideoURI(Uri.parse(videoUrl))
        player.setMediaController(controller)
        player.setOnPreparedListener { media ->
            media.isLooping = true
            controller.show()
        }
        player.start()
    }

    fun clickableViews(): List<View> = listOf(this, binding.cover, binding.title, binding.desc, binding.cta)

    fun closeableViews(): List<View> = listOf(binding.close)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding.video.stopPlayback()
    }
}
