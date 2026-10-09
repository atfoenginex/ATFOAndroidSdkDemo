package com.atfo.ad.demo.utils

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

/** 全局主线程 Handler：广告 SDK 回调可能来自子线程，弹 Toast 前统一经它切回主线程。 */
private val toastHandler = Handler(Looper.getMainLooper())

/** 回调文案同步弹 Toast，不连 adb 也能看到状态；子线程调用时自动切主线程。 */
fun Context.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(this, msg, duration).show()
    } else {
        toastHandler.post { Toast.makeText(this, msg, duration).show() }
    }
}

/** 打日志并弹 Toast（各回调统一走这里）；TAG 沿用各页「ATFO+类名」的约定，优先级缺省为 DEBUG。 */
fun Activity.logAndToast(msg: String, priority: Int = Log.DEBUG) {
    Log.println(priority, "ATFO${this::class.java.simpleName}", msg)
    toast(msg)
}
