package com.atfo.ad.demo.utils

import android.content.Context

/** dp 转 px。 */
fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
