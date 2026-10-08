import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// 账号与广告位参数只从 local.properties 注入，改配置不用动代码。
// 该文件不入库，取值方式见根目录 local.properties.example。
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun atfoConfig(key: String, defaultValue: String): String =
    localProperties.getProperty("atfo.$key")?.takeIf { it.isNotBlank() } ?: defaultValue

fun stringField(name: String, key: String, defaultValue: String) {
    android.defaultConfig.buildConfigField("String", name, "\"${atfoConfig(key, defaultValue)}\"")
}

fun boolField(name: String, key: String, defaultValue: Boolean) {
    val value = atfoConfig(key, defaultValue.toString()).equals("true", ignoreCase = true)
    android.defaultConfig.buildConfigField("boolean", name, value.toString())
}

android {
    namespace = "com.atfo.ad.demo"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = atfoConfig("applicationId", "com.atfo.ad.demo")
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        stringField("ATFO_APP_ID", "appId", "your_app_id")
        stringField("ATFO_SECRET", "secret", "your_secret")
        stringField("ATFO_MEDIA_ID", "mediaId", "your_media_id")
        stringField("ATFO_APP_NAME", "appName", "ATFO Demo")
        stringField("ATFO_ENV", "env", "PRODUCTION")
        stringField("ATFO_SPLASH_SLOT_ID", "splashSlotId", "")
        stringField("ATFO_FEED_SLOT_ID", "feedSlotId", "")
        stringField("ATFO_FEED_SELF_RENDER_SLOT_ID", "feedSelfRenderSlotId", "")
        stringField("ATFO_INTERSTITIAL_SLOT_ID", "interstitialSlotId", "")
        stringField("ATFO_REWARD_SLOT_ID", "rewardSlotId", "")
        stringField("ATFO_NOTIFY_SLOT_ID", "notifySlotId", "")
        boolField("ATFO_DEBUG", "debug", true)
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.atfo.core)
    implementation(libs.okhttp)
    implementation(libs.glide)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
