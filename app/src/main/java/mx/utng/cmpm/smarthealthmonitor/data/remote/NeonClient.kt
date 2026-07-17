package mx.utng.cmpm.smarthealthmonitor.data.remote

import mx.utng.cmpm.smarthealthmonitor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NeonClient {
    private const val BASE_URL = "https://${BuildConfig.NEON_HOST}/"

    val AUTH_HEADER  = "Bearer ${BuildConfig.NEON_PASS}"
    // Assuming neon connection string uses username/password provided during setup, but since we use neon HTTP API, we only need the endpoint and API Key for serverless queries. Wait, Neon HTTP API requires the Neon-Connection-String header which must contain the role and database.
    // The instructions say: val CONN_STRING = "postgresql://[usuario]:[pass]@${BuildConfig.NEON_HOST}/neondb?sslmode=require"
    // The user needs to update this or I should just pass it from local properties.
    // Since the API Key has access, we can put dummy or let the user fix it. I will use the one from the instructions.
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
