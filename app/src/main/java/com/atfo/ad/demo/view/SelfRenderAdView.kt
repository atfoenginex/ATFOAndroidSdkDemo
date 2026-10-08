package com.atfo.ad.demo.view

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.core.content.ContextCompat
import com.atfo.ad.demo.R
import com.atfo.ad.demo.databinding.ViewSelfRenderAdBinding
import com.atfo.ad.model.ATFONativeFeedAdData
import com.atfo.ad.model.primaryImageUrl
import com.atfo.ad.source.AdSource
import com.bumptech.glide.Glide

/**
 * 信息流自渲染广告卡片（左图右文）：素材全部来自 [ATFONativeFeedAdData]，布局见 `view_self_render_ad.xml`。
 * 点击区域与关闭按钮通过 [com.atfo.ad.model.ATFOAd.registerInteraction] 交回 SDK，曝光/点击/关闭上报由 SDK 负责。
 * 卡片背景/圆角/高程在代码里设置（merge 根带不了属性），外边距由承载方设置。
 */
class SelfRenderAdView(context: Context) : FrameLayout(context) {

    private val binding =
        ViewSelfRenderAdBinding.inflate(LayoutInflater.from(context), this)

    init {
        setBackgroundResource(R.drawable.bg_ad_card)
        // 容器与卡片同为白底，靠阴影给出边界
        elevation = dp(3).toFloat()
        // 圆角裁剪用代码设置（XML 的 android:clipToOutline 属性要 API 31 才生效）
        binding.flMedia.clipToOutline = true
        binding.llBadge.clipToOutline = true
    }

    /** 媒体位，作为显式点击区域传给 SDK */
    val mediaView: View = binding.flMedia

    /** 「查看 / 立即下载」按钮，作为显式点击区域传给 SDK */
    val moreButton: View = binding.flMore

    /** 右上角 ✕，经 closeViews 传给 SDK，关闭上报走 onAdClosed */
    val closeView: View = binding.ivClose

    private var videoView: VideoView? = null

    /** 三方/SDK 自带的媒体视图（如 GDT MediaView、SDK 播放器），挂进媒体位后宿主不再自播 */
    private var attachedMediaView: View? = null

    /** 需在 [bind] 之前调用：媒体位交给 SDK 视图，宿主只画文字与角标 */
    fun attachMediaView(view: View?) {
        if (view == null) return
        attachedMediaView = view
        binding.ivCover.visibility = View.GONE
        binding.flMedia.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun bind(info: ATFONativeFeedAdData) {
        setText(binding.tvTitle, info.title)
        setText(binding.tvDesc, info.description)
        setText(binding.tvSource, info.appInfo?.name ?: info.source)
        renderMoreButton(info.buttonText)
        renderMedia(info)
        renderAdBadge(info)
    }

    /** 媒体位三选一：SDK 给的媒体视图 > 宿主自播 videoUrl > 封面图 */
    private fun renderMedia(info: ATFONativeFeedAdData) {
        if (attachedMediaView != null) return
        val videoUrl = info.videoUrl
        if (!videoUrl.isNullOrBlank()) {
            // 视频物料不加载封面：省一份图片解码与带宽，起播前显示媒体位底色
            binding.ivCover.visibility = View.GONE
            playVideo(videoUrl)
            return
        }
        info.primaryImageUrl()?.let { url ->
            Glide.with(this).load(url).into(binding.ivCover)
        }
    }

    /** 空文本不占位 */
    private fun setText(view: TextView, value: String?) {
        if (value.isNullOrBlank()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = value
        }
    }

    /** buttonText 为图片地址时展示图片（服务端下发图形式的 CTA），否则展示文字 */
    private fun renderMoreButton(buttonText: String?) {
        if (!buttonText.isNullOrBlank() && isImageUrl(buttonText)) {
            binding.tvMore.visibility = View.GONE
            binding.ivMoreImage.visibility = View.VISIBLE
            Glide.with(this).load(buttonText).into(binding.ivMoreImage)
        } else {
            binding.ivMoreImage.visibility = View.GONE
            binding.tvMore.visibility = View.VISIBLE
            binding.tvMore.text = buttonText?.takeIf { it.isNotBlank() } ?: "查看"
        }
    }

    /**
     * 广告合规角标：三方广告显示其平台 logo（adLogo 的 URL 或 Bitmap）；
     * ATFO 自家广告（无 adLogo）只显示「广告」文字。
     */
    private fun renderAdBadge(info: ATFONativeFeedAdData) {
        val logoUrl = info.adLogo?.url
        val logoBitmap = info.adLogo?.bitmap
        binding.llBadge.visibility = View.VISIBLE
        when {
            !logoUrl.isNullOrBlank() -> {
                binding.ivBadgeLogo.visibility = View.VISIBLE
                Glide.with(this).load(logoUrl).into(binding.ivBadgeLogo)
            }

            logoBitmap != null -> {
                binding.ivBadgeLogo.visibility = View.VISIBLE
                binding.ivBadgeLogo.setImageBitmap(logoBitmap)
            }

            else -> binding.ivBadgeLogo.visibility = View.GONE
        }
    }


    /** 媒体位内静音循环自播（与 SDK 内置模板一致），起播前显示容器底色 */
    private fun playVideo(url: String) {
        val player = VideoView(context)
        videoView = player
        player.setVideoURI(Uri.parse(url))
        player.setOnPreparedListener { media ->
            media.isLooping = true
            media.setVolume(0f, 0f)
            media.start()
        }
        binding.flMedia.addView(
            player,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    fun clickableViews(): List<View> =
        listOf(this, mediaView, binding.tvTitle, binding.tvDesc, moreButton)

    fun closeableViews(): List<View> = listOf(closeView)

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    /** http(s) 且以图片扩展名结尾，视为服务端下发的图片形式 CTA */
    private fun isImageUrl(url: String): Boolean {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        val path = url.substringBefore('?')
        return IMAGE_EXTENSIONS.any { path.endsWith(it, ignoreCase = true) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        videoView?.stopPlayback()
        videoView = null
    }

    private companion object {
        val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
    }
}
