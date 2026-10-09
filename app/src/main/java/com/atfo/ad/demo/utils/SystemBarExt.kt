package com.atfo.ad.demo.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

/** 状态栏白底黑字：页面背景铺到状态栏后面（各页布局均为白底），图标改成深色。 */
fun AppCompatActivity.lightStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
}

/** targetSdk 36 起默认边到边，顶部按钮条会被状态栏盖住，这里让出状态栏高度（保留原 padding）。 */
fun View.applyStatusBarPadding() {
    val basePaddingTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        view.updatePadding(top = basePaddingTop + statusBarTop)
        insets
    }
}

/** targetSdk 36 起默认边到边，内容会顶到状态栏/导航栏，这里把系统栏四边尺寸让出来。 */
fun View.applySystemBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}
