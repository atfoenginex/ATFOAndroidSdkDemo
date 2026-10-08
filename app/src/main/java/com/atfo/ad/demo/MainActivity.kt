package com.atfo.ad.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityMainBinding
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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        applySystemBarPadding(binding.main)

        launch(binding.rowSplash, SplashAdActivity::class.java)
        launch(binding.rowFeed, FeedAdActivity::class.java)
        launch(binding.rowFeedSelfRender, FeedSelfRenderAdActivity::class.java)
        launch(binding.rowInterstitial, InterstitialAdActivity::class.java)
        launch(binding.rowReward, RewardAdActivity::class.java)
        launch(binding.rowNotify, NotifyAdActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        binding.tvState.text =
            if (DemoConfig.isAccountConfigured()) DemoConfig.describe()
            else "appId / secret 仍是占位值，请在 local.properties 配置后重新编译"
    }

    private fun launch(row: View, target: Class<out AppCompatActivity>) {
        row.setOnClickListener {
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
