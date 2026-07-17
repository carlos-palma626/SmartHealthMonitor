package mx.utng.cmpm.smarthealthmonitor.wear.data

import mx.utng.cmpm.health_wear.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NeonClient {
    private const val BASE_URL = "https://${BuildConfig.NEON_HOST}/"
    val CONN_STRING  = "postgresql://${BuildConfig.NEON_USER}:${BuildConfig.NEON_PASS}@${BuildConfig.NEON_HOST}/neondb?sslmode=require"

    val api: NeonApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build())
            .build()
            .create(NeonApiService::class.java)
    }
}
