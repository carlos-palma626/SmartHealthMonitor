package mx.utng.cmpm.smarthealthmonitor.wear.data

import kotlinx.serialization.Serializable
import retrofit2.http.*

data class NeonRequest(val query: String, val params: List<Any> = emptyList())

@Serializable
data class NeonResponse<T>(
    val rows        : List<T>   = emptyList(),
    val rowCount    : Int       = 0,
    val command     : String    = "",
)

@Serializable
data class LecturaFcDto(
    val id          : Int    = 0,
    val bpm         : Int,
    val estado      : String,
    val dispositivo : String,
    val hora        : String,
    val fecha       : String  = "",
    val created_at  : String  = "",
)

interface NeonApiService {
    @POST("sql")
    suspend fun executeQuery(
        @Header("Neon-Connection-String") connStr: String,
        @Body request: NeonRequest
    ): NeonResponse<LecturaFcDto>
}
