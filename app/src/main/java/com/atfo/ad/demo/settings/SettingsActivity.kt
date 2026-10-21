package com.atfo.ad.demo.settings

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.config.ATFOAdConfigManager
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.config.SlotOverrideStore
import com.atfo.ad.demo.databinding.ActivitySettingsBinding
import com.atfo.ad.demo.utils.applySystemBarPadding
import com.atfo.ad.demo.utils.lightStatusBar
import com.atfo.ad.demo.utils.logAndToast
import com.atfo.ad.debug.DebugManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 设置页：展示接入参数、打开 SDK Debug 面板、切换四类代码位。
 *
 * 代码位候选来自 SDK 缓存的 ad_setting（后台下发），选择存 MMKV，
 * 下次加载广告即生效；「恢复默认」回落到 local.properties 注入的值。
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lightStatusBar()
        binding.settings.applySystemBarPadding()

        binding.rowDebug.setOnClickListener { openDebugPage() }
        binding.rowRefresh.setOnClickListener { refreshConfigCache() }
        bindSlotRow(binding.rowSlotSplash, binding.tvSlotSplash, DemoConfig.SLOT_SPLASH, "开屏")
        bindSlotRow(binding.rowSlotFeed, binding.tvSlotFeed, DemoConfig.SLOT_FEED, "信息流（模板渲染）")
        bindSlotRow(
            binding.rowSlotFeedSelfRender, binding.tvSlotFeedSelfRender,
            DemoConfig.SLOT_FEED_SELF_RENDER, "信息流（宿主自渲染）",
        )
        bindSlotRow(
            binding.rowSlotInterstitial, binding.tvSlotInterstitial,
            DemoConfig.SLOT_INTERSTITIAL, "插屏",
        )
        bindSlotRow(binding.rowSlotReward, binding.tvSlotReward, DemoConfig.SLOT_REWARD, "激励视频")
        bindSlotRow(binding.rowSlotNotify, binding.tvSlotNotify, DemoConfig.SLOT_NOTIFY, "通知")
    }

    override fun onResume() {
        super.onResume()
        binding.tvConfig.text = DemoConfig.describe()
    }

    /**
     * SDK 的 Debug Activity 默认 enabled=false（仅 debug 初始化时启用），
     * 所以必须走 DebugManager 入口，debug=false 的构建里会拉不起来，兜个底提示。
     */
    private fun openDebugPage() {
        try {
            DebugManager.openDebugActivity(this)
        } catch (e: Exception) {
            logAndToast("打开 Debug 页面失败（debug=false 时 SDK 不会启用该面板）：${e.message}", android.util.Log.WARN)
        }
    }

    private fun refreshConfigCache() {
        ATFOAdConfigManager.Companion.getInstance().onAppForegroundRefresh()
        logAndToast("已触发配置刷新，稍后重试查看候选列表")
    }

    private fun bindSlotRow(row: LinearLayout, desc: TextView, slotKey: String, label: String) {
        refreshSlotDesc(desc, slotKey)
        row.setOnClickListener { showSlotPicker(slotKey, label) { refreshSlotDesc(desc, slotKey) } }
    }

    private fun refreshSlotDesc(desc: TextView, slotKey: String) {
        val current = DemoConfig.effectiveSlotId(slotKey)
        val text = when {
            current.isBlank() -> "未配置"
            SlotOverrideStore.hasOverride(slotKey) -> "$current（已覆盖默认）"
            else -> current
        }
        desc.text = text
    }

    private fun showSlotPicker(slotKey: String, label: String, onChanged: () -> Unit) {
        val defaultId = DemoConfig.defaultSlotId(slotKey)
        val candidates = DemoConfig.candidateSlotIds(slotKey).toMutableList()
        if (defaultId.isNotBlank() && defaultId !in candidates) candidates.add(0, defaultId)
        if (candidates.isEmpty()) {
            logAndToast("暂无缓存的代码位配置，请先加载一次广告或点「刷新配置缓存」")
            return
        }

        val items = candidates.map { id ->
            if (id == defaultId) "$id（local.properties 默认）" else id
        }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("选择$label 代码位")
            .setItems(items) { _, which ->
                SlotOverrideStore.setOverride(slotKey, candidates[which])
                logAndToast("$label 代码位已切换为 ${candidates[which]}，下次加载广告生效")
                onChanged()
            }
            .setNeutralButton("恢复默认") { _, _ ->
                SlotOverrideStore.setOverride(slotKey, null)
                logAndToast("$label 代码位已恢复默认：$defaultId")
                onChanged()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
