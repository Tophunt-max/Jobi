package com.example.data.remote.cloudflare

import android.content.Context
import com.example.BuildConfig
import com.example.data.remote.model.AiGenerateRequest
import com.example.data.remote.model.AiGenerateResponse
import com.example.data.remote.model.BackendAllDataSyncResponse
import com.example.data.remote.model.ReportRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Public, read-mostly API exposed by the Cloudflare Worker. The app has no admin
 * capabilities: moderation happens only in the admin panel.
 */
interface CloudflareApiService {

    @GET("api/sync/all")
    suspend fun getSyncAllData(): BackendAllDataSyncResponse

    @GET("api/jobs/{id}")
    suspend fun getJobById(@Path("id") id: String): CloudflareSingleResponse

    @GET("api/sources")
    suspend fun getPublicSources(@Query("state") state: String? = null): CloudflareSourcesResponse

    @GET("api/health")
    suspend fun checkHealth(): CloudflareHealthResponse

    /** AI mentor proxy. The Gemini key stays on the Worker. */
    @POST("api/ai/generate")
    suspend fun aiGenerate(@Body request: AiGenerateRequest): AiGenerateResponse

    /** Users reporting wrong or outdated information on a notice. */
    @POST("api/reports")
    suspend fun submitReport(@Body request: ReportRequest): CloudflareActionResponse
}

object CloudflareClient {
    @Volatile
    private var appContext: Context? = null

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "SarkariJobs-Android/${BuildConfig.VERSION_NAME}")
                        .header("Accept", "application/json")
                        .build()
                )
            }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        }

        appContext?.let { ctx ->
            builder.cache(Cache(File(ctx.cacheDir, "http_backend_cache"), 10L * 1024 * 1024))
        }
        builder.build()
    }

    /** Long-running calls (AI generation) get a separate, more patient client. */
    private val slowClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(90, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(client: OkHttpClient): CloudflareApiService {
        check(CloudflareConfig.isConfigured()) { "Backend URL is not configured in this build" }
        return Retrofit.Builder()
            .baseUrl(CloudflareConfig.getActiveWorkerUrl() + "/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CloudflareApiService::class.java)
    }

    private val defaultApi: CloudflareApiService by lazy { retrofit(okHttpClient) }
    private val slowApi: CloudflareApiService by lazy { retrofit(slowClient) }

    fun getService(): CloudflareApiService = defaultApi

    fun getSlowService(): CloudflareApiService = slowApi
}
