package com.atfo.ad.demo.view

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import com.atfo.ad.model.ATFONativeFeedAdData
import com.atfo.ad.model.primaryImageUrl
import com.bumptech.glide.Glide

/**
 * 自渲染广告的宿主视图：全部素材来自 [com.atfo.ad.model.ATFONativeFeedAdData]，
 * 点击区域与关闭按钮通过 ATFOAd.registerInteraction 交回 SDK 处理。
 */
class SelfRenderAdView(context: Context) : FrameLayout(context) {

    private val density = context.resources.displayMetrics.density
    private val coverView = ImageView(context)
    private val titleView = TextView(context)
    private val descView = TextView(context)
    private val sourceView = TextView(context)
    private val ctaButton = Button(context)
    private val closeButton = Button(context)
    private val logoView = ImageView(context)
    private val content = LinearLayout(context)
    private var videoView: VideoView? = null

    init {
        setBackgroundColor(0xFF202124.toInt())
        val padding = (12 * density).toInt()
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(padding, padding, padding, padding)
        addView(content, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, 0, 0, 0)
        }
        titleView.apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            maxLines = 2
        }
        descView.apply {
            setTextColor(0xFFBDC1C6.toInt())
            textSize = 13f
            maxLines = 2
        }
        sourceView.apply {
            setTextColor(0xFF9AA0A6.toInt())
            textSize = 11f
        }
        textColumn.addView(titleView)
        textColumn.addView(descView)
        textColumn.addView(sourceView)

        val imageRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        imageRow.addView(coverView, LinearLayout.LayoutParams((110 * density).toInt(), (110 * density).toInt()))
        imageRow.addView(
            textColumn,
            LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        )
        content.addView(imageRow)

        val footer = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        footer.addView(ctaButton, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        footer.addView(closeButton, LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        content.addView(footer)

        // 合规角标
        addView(
            logoView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END or Gravity.TOP
                topMargin = padding
                marginEnd = padding
            }
        )
    }

    fun bind(info: ATFONativeFeedAdData) {
        titleView.text = info.title
        descView.text = info.description
        sourceView.text = info.appInfo?.name ?: info.source
        ctaButton.text = info.buttonText
        closeButton.text = "关闭"

        info.primaryImageUrl()?.let { url ->
            Glide.with(this).load(url).into(coverView)
        }
        info.adLogo?.url?.let { url ->
            Glide.with(this).load(url).into(logoView)
        }
        playVideoIfNeed(info)
    }

    /** sdkVideoRender=false 时由宿主播放 videoUrl；为 true 时素材已由三方 SDK 渲染。 */
    private fun playVideoIfNeed(info: ATFONativeFeedAdData) {
        val videoUrl = info.videoUrl
        if (videoUrl.isNullOrEmpty() || info.sdkVideoRender) return

        val player = VideoView(context)
        videoView = player
        val controller = MediaController(context).apply { setAnchorView(player) }
        player.setVideoURI(Uri.parse(videoUrl))
        player.setMediaController(controller)
        player.setOnPreparedListener { media ->
            media.isLooping = true
            controller.show()
        }
        content.addView(
            player,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (200 * density).toInt()
            ).apply { topMargin = (8 * density).toInt() }
        )
        player.start()
    }

    fun clickableViews(): List<View> = listOf(this, coverView, titleView, descView, ctaButton)

    fun closeableViews(): List<View> = listOf(closeButton)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        videoView?.stopPlayback()
        videoView = null
    }
}