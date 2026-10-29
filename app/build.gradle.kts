import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

// ---------------------------------------------------------------------------------------------
// Build-time configuration (non-secret). Values come from, in order of precedence:
//   1. Gradle property  (-PAPI_BASE_URL=... or ~/.gradle/gradle.properties)
//   2. Environment variable (CI)
//   3. .env / .env.example in the project root
// No secrets are compiled into the APK: the Gemini key lives only on the Worker.
// ---------------------------------------------------------------------------------------------
val envFile = Properties().apply {
  listOf(".env.example", ".env").forEach { name ->
    val f = rootProject.file(name)
    if (f.exists()) f.inputStream().use { load(it) }
  }
}

// Blank values count as "not set". GitHub Actions passes unset repository variables as
// empty strings, which previously produced an empty AdMob APPLICATION_ID and crashed
// release builds on launch.
fun config(name: String, default: String = ""): String =
  listOf(
    project.findProperty(name) as String?,
    System.getenv(name),
    envFile.getProperty(name),
  ).firstOrNull { !it.isNullOrBlank() }?.trim() ?: default

// Google's public AdMob sample IDs. Used for all debug builds, and for release builds
// until real IDs are configured.
val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestUnits = mapOf(
  "BANNER" to "ca-app-pub-3940256099942544/9214589741",
  "INTERSTITIAL" to "ca-app-pub-3940256099942544/1033173712",
  "REWARDED" to "ca-app-pub-3940256099942544/5224354917",
  "NATIVE" to "ca-app-pub-3940256099942544/2247696110",
)

fun String.asBuildConfigString() = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
  namespace = "com.example"
  compileSdk { version = release(36) }

  defaultConfig {
    applicationId = "com.aistudio.sarkarijobportal.kxrtzp"
    minSdk = 24
    targetSdk = 36
    versionCode = config("VERSION_CODE", "2").toInt()
    versionName = config("VERSION_NAME", "1.1.0")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val apiBaseUrl = config("API_BASE_URL").trimEnd('/')
    buildConfigField("String", "API_BASE_URL", apiBaseUrl.asBuildConfigString())
  }

  // Do not remove or modify these signingConfigs. They are necessary for building, installing,
  // and updating Android apps in AI Studio.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

      val appId = config("ADMOB_APP_ID", admobTestAppId)
      if (!Regex("^ca-app-pub-\\d{16}~\\d{10}$").matches(appId)) {
        throw GradleException("ADMOB_APP_ID '$appId' is not a valid AdMob app ID (ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX)")
      }
      if (appId == admobTestAppId) {
        logger.warn("w: ADMOB_APP_ID not set. Release build will serve Google TEST ads (no revenue).")
      }
      manifestPlaceholders["admobAppId"] = appId
      admobTestUnits.forEach { (name, testId) ->
        val unit = config("ADMOB_${name}_ID", testId)
        if (!Regex("^ca-app-pub-\\d{16}/\\d{10}$").matches(unit)) {
          throw GradleException("ADMOB_${name}_ID '$unit' is not a valid AdMob ad unit ID")
        }
        buildConfigField("String", "ADMOB_${name}_ID", unit.asBuildConfigString())
      }
      buildConfigField("boolean", "ALLOW_REMOTE_AD_UNITS", "true")

      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Debug builds always use Google's test ad units to avoid invalid-traffic policy strikes.
      manifestPlaceholders["admobAppId"] = admobTestAppId
      admobTestUnits.forEach { (name, testId) ->
        buildConfigField("String", "ADMOB_${name}_ID", testId.asBuildConfigString())
      }
      buildConfigField("boolean", "ALLOW_REMOTE_AD_UNITS", "false")

      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    // java.time on API 24-25
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Fail fast instead of shipping a release that cannot reach its backend.
val configuredApiUrl = config("API_BASE_URL")
if (!configuredApiUrl.startsWith("https://")) {
  logger.warn("w: API_BASE_URL is not set to an https:// URL. The app will show an offline error until it is configured.")
}
tasks.configureEach {
  if ((name.startsWith("assemble") || name.startsWith("bundle")) && name.contains("Release")) {
    val url = configuredApiUrl
    doFirst {
      if (!url.startsWith("https://")) {
        throw GradleException(
          "API_BASE_URL must be set to your Worker's https:// URL for release builds (.env, env var, or -PAPI_BASE_URL)."
        )
      }
    }
  }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
  arg("room.generateKotlin", "true")
}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.ads)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
