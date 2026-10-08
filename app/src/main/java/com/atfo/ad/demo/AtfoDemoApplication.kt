package com.atfo.ad.demo

import android.app.Application
import android.util.Log
import com.atfo.ad.core.ATFOADSDKManager
import com.atfo.ad.core.ATFOSDKConfig
import com.atfo.ad.demo.config.DemoConfig
import com.atfo.ad.listener.ATFOAdInitListener

/**
 * SDK 在 Application 中初始化，接入第三方广告源时在此之前注册对应 adapter。
 */
class AtfoDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeATFOSDK()
    }

    private fun initializeATFOSDK() {
        val sdkConfig = ATFOSDKConfig.Builder()
            .appId(DemoConfig.appId)
            .mediaId(DemoConfig.mediaId)
            .secret(DemoConfig.secret)
            .appName(DemoConfig.appName)
            .environment(DemoConfig.environment)
            .debug(DemoConfig.debug)
            // 禁用广告网络时填写，例如 mutableListOf(AdSource.GDT, AdSource.BAIDU)
            .forbidNetworkList(mutableListOf())
            // false 时由接入方自行调用 ATFOADSDKManager.getInstance().start()
            .grmAutoStart(true)
            // 透传参数，接入方替换为自己的业务 id
            .extra(KEY_CUR_ID, CUR_ID)
            .build()

        Log.i(TAG, "init SDK with ${DemoConfig.describe()}")
        ATFOADSDKManager.getInstance().init(this, sdkConfig, object : ATFOAdInitListener {
            override fun onSuccess() {
                Log.d(TAG, "SDK 初始化成功, version=" + ATFOADSDKManager.getInstance().getSDKVersion())
            }

            override fun onFail() {
                Log.e(TAG, "SDK 初始化失败，请检查 appId / secret / mediaId 及网络")
            }
        })
    }

    private companion object {
        const val TAG = "AtfoDemo"
        const val KEY_CUR_ID = "cur_id"
        const val CUR_ID = "atfo_demo_user_001"
    }
}
