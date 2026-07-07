package com.cmpm.tv.domain.model

data class LecturaFC(
    val id: Int = 0,
    val bpm: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val hora: String = "12:00",
    val estado: String = "Normal"
)
