package com.example.data.remote.cloudflare

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CloudflareApiService {

    @GET("api/jobs")
    suspend fun getJobs(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("status") status: String? = "ACTIVE",
        @Query("limit") limit: Int = 50
    ): CloudflareListResponse

    @GET("api/jobs/{id}")
    suspend fun getJobById(
        @Path("id") id: String
    ): CloudflareSingleResponse

    @POST("api/jobs")
    suspend fun createOrUpdateJob(
        @Body job: CloudflareJobDto,
        @Header("X-Admin-Key") adminKey: String? = null
    ): CloudflareActionResponse

    @GET("api/stats")
    suspend fun getStats(): CloudflareStatsResponse

    @GET("api/health")
    suspend fun checkHealth(): CloudflareHealthResponse

    @Multipart
    @POST("api/upload")
    suspend fun uploadPdfToR2(
        @Query("filename") filename: String,
        @Part file: MultipartBody.Part
    ): CloudflareActionResponse
}

object CloudflareClient {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private var cachedBaseUrl: String? = null
    private var cachedService: CloudflareApiService? = null

    fun getService(): CloudflareApiService {
        val currentBase = CloudflareConfig.getActiveWorkerUrl()
        val normalized = if (currentBase.endsWith("/")) currentBase else "$currentBase/"
        
        if (cachedService != null && cachedBaseUrl == normalized) {
            return cachedService!!
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(CloudflareApiService::class.java)
        cachedBaseUrl = normalized
        cachedService = service
        return service
    }
}
