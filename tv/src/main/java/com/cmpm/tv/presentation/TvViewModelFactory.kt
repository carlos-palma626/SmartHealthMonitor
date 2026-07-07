package com.cmpm.tv.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cmpm.tv.domain.repository.FakeSmartHealthRepository

class TvViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TvViewModel(FakeSmartHealthRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
