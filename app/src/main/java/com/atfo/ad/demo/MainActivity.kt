package com.atfo.ad.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.demo.databinding.ActivityMainBinding
import com.atfo.ad.demo.feed.FeedAdActivity
import com.atfo.ad.demo.feed.FeedSelfRenderAdActivity
import com.atfo.ad.demo.interstitial.InterstitialAdActivity
import com.atfo.ad.demo.notify.NotifyAdActivity
import com.atfo.ad.demo.reward.RewardAdActivity
import com.atfo.ad.demo.splash.SplashAdActivity
import com.atfo.ad.demo.utils.applySystemBarPadding
import com.atfo.ad.demo.utils.lightStatusBar

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
        lightStatusBar()
        binding.main.applySystemBarPadding()

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
}
