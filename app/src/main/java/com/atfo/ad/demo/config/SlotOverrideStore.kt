package com.atfo.ad.demo.config

import com.tencent.mmkv.MMKV

/**
 * 设置页里手动切换代码位的持久化存储。
 *
 * 独立用一份 mmap id（不与 SDK 的 `atfo_ad_config` 缓存混在一起），
 * 每个 slotKey 一个 key；MMKV 在 Application#onCreate 里随 SDK 一起初始化。
 */
object SlotOverrideStore {

    private const val MMKV_ID = "atfo_demo_settings"

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(MMKV_ID) }

    /** 没有覆盖时返回 null，调用方回落到 local.properties 注入的 BuildConfig 值。 */
    fun overrideOf(slotKey: String): String? =
        mmkv.decodeString(keyOf(slotKey))?.takeIf { it.isNotBlank() }

    fun hasOverride(slotKey: String): Boolean = overrideOf(slotKey) != null

    /** 传 null 或空串即清除覆盖，恢复默认。 */
    fun setOverride(slotKey: String, slotId: String?) {
        val key = keyOf(slotKey)
        if (slotId.isNullOrBlank()) mmkv.removeValueForKey(key) else mmkv.encode(key, slotId)
    }

    private fun keyOf(slotKey: String) = "slot_override_$slotKey"
}
