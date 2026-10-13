package com.atfo.ad.demo.utils

import com.atfo.ad.model.ATFOAd
import java.lang.reflect.Method

/**
 * Demo 专用：读取广告折算价。
 *
 * SDK 的 price 字段由 atfoExposePrice 开关控制（字段物理存在与否），
 * 这里通过反射读取：开关关闭的包没有该字段，返回默认值 0，保证 demo 两种形态都可编译运行。
 */
object AdPriceUtils {

    /** price 字段对应的 getter；noprice 变体的包里不存在，为 null。 */
    private val priceGetter: Method? by lazy {
        try {
            ATFOAd::class.java.getMethod("getPrice")
        } catch (ignored: NoSuchMethodException) {
            null
        }
    }

    /** 有价格返回价格，否则返回默认值 0。 */
    fun getPrice(ad: ATFOAd): Int {
        val getter = priceGetter ?: return 0
        return try {
            (getter.invoke(ad) as? Number)?.toInt() ?: 0
        } catch (ignored: Exception) {
            0
        }
    }
}
