package com.atfo.ad.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.feed.FeedAdActivity
import com.atfo.ad.demo.feed.FeedSelfRenderAdActivity
import com.atfo.ad.demo.interstitial.InterstitialAdActivity
import com.atfo.ad.demo.notify.NotifyAdActivity
import com.atfo.ad.demo.reward.RewardAdActivity
import com.atfo.ad.demo.splash.SplashAdActivity

/**
 * 入口页：按广告类型分别演示接入方式。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        applySystemBarPadding(findViewById(R.id.main))

        launch(R.id.btn_splash, SplashAdActivity::class.java)
        launch(R.id.btn_feed, FeedAdActivity::class.java)
        launch(R.id.btn_feed_self_render, FeedSelfRenderAdActivity::class.java)
        launch(R.id.btn_interstitial, InterstitialAdActivity::class.java)
        launch(R.id.btn_reward, RewardAdActivity::class.java)
        launch(R.id.btn_notify, NotifyAdActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.tv_state).text =
            if (DemoConfig.isAccountConfigured()) DemoConfig.describe()
            else "appId / secret 仍是占位值，请在 local.properties 配置后重新编译"
    }

    private fun launch(buttonId: Int, target: Class<out AppCompatActivity>) {
        findViewById<Button>(buttonId).setOnClickListener {
            startActivity(Intent(this, target))
        }
    }

    /** targetSdk 36 起默认边到边，内容会顶到状态栏/导航栏，这里把系统栏尺寸让出来。 */
    private fun applySystemBarPadding(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
