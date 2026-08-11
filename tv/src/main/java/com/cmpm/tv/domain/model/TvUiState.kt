package com.cmpm.tv.domain.model

data class TvUiState(
    val lecturas    : List<LecturaFC> = emptyList(),
    val fcActual    : Int             = 0,
    val fcEstado    : String          = "",
    val ultimaHora  : String          = "",
    val isLoading   : Boolean         = true,
    val error       : String?         = null,
)
