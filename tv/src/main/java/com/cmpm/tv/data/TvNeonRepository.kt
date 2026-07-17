package com.cmpm.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TvNeonRepository {

    /** Obtener historial completo de los 3 dispositivos */
    suspend fun obtenerHistorialCompleto(limite: Int = 50): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query  = """SELECT id,bpm,estado,dispositivo,hora,created_at 
                               FROM lecturas_fc 
                               ORDER BY created_at DESC 
                               LIMIT $1""".trimIndent(),
                    params = listOf(limite)
                )
            ).rows
        }

    /** Estadísticas por dispositivo */
    suspend fun obtenerEstadisticas(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query  = """SELECT dispositivo, 
                               ROUND(AVG(bpm)) AS bpm, 
                               'Promedio' AS estado, 
                               MAX(hora) AS hora 
                               FROM lecturas_fc 
                               GROUP BY dispositivo""".trimIndent()
                )
            ).rows
        }
}
