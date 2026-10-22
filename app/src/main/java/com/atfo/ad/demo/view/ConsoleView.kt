package com.atfo.ad.demo.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import com.atfo.ad.demo.R
import com.atfo.ad.demo.databinding.ViewConsoleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 调试 Console 视图：终端风深色圆角卡片，等宽字体日志区 + 标题行扫帚清空按钮，布局见 `view_console.xml`。
 * 各广告页可复用：把回调文案 [log] 进来即可，广告 SDK 回调可能来自子线程，内部已切主线程再更新文本。
 * 视图随页面销毁一起销毁，Console 天然清空，不需要页面额外清理。
 */
class ConsoleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding =
        ViewConsoleBinding.inflate(LayoutInflater.from(context), this)

    /** 日志行时间戳用；SimpleDateFormat 非线程安全，但使用点已收敛到主线程，安全。 */
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /** 广告 SDK 回调可能来自子线程，写日志前统一经它切回主线程。 */
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 当前保留的日志行数，超过 [MAX_LINES] 后丢掉最早的，防止长时间演示内存无限涨 */
    private var lineCount = 0

    init {
        // 圆角背景用代码设置（merge 根带不了属性）；裁剪让滚动内容不越过圆角
        setBackgroundResource(R.drawable.bg_console)
        clipToOutline = true
        binding.ivClear.setOnClickListener { clear() }
    }

    /**
     * 打一行日志：同步写 logcat（TAG 沿用各页「ATFO+类名」的约定），UI 上追加
     * 「时间戳 + 级别标记 + 文案」，行尾换行保证每条日志独占一行。
     */
    fun log(msg: String, priority: Int = Log.DEBUG, tag: String = DEFAULT_TAG) {
        Log.println(priority, tag, msg)
        mainHandler.post {
            appendLine("${timeFormat.format(Date())} ${levelMark(priority)} $msg")
        }
    }

    /** 清空所有日志 */
    fun clear() {
        mainHandler.post {
            binding.tvLog.text = ""
            lineCount = 0
        }
    }

    private fun appendLine(line: String) {
        var text = binding.tvLog.text.toString() + line + "\n"
        if (lineCount >= MAX_LINES) {
            // 丢最早的一行，保持总量不涨
            text = text.substringAfter('\n')
        } else {
            lineCount++
        }
        binding.tvLog.text = text
        binding.consoleScroll.post { binding.consoleScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun levelMark(priority: Int) = when (priority) {
        Log.ERROR -> "[E]"
        Log.WARN -> "[W]"
        else -> "[I]"
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachNavigationBarAvoidance()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 移除在途的日志写入，避免持有已离开屏幕的视图
        mainHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 底部自动避开导航栏：把 navigationBars 的 bottom 高度叠加到自身 bottomMargin。
     * Console 永远贴底，避让逻辑收在自己身上，承载方布局零改动；
     * layoutParams 在构造期还是 null（XML 属性尚未回填），所以挂到 attach 时机做。
     */
    private fun attachNavigationBarAvoidance() {
        val baseMarginBottom = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            (view.layoutParams as? ViewGroup.MarginLayoutParams)
                ?.updateMargins(bottom = baseMarginBottom + navBottom)
            insets
        }
        ViewCompat.requestApplyInsets(this)
    }

    private companion object {
        private const val DEFAULT_TAG = "ATFOConsole"
        private const val MAX_LINES = 300
    }
}
