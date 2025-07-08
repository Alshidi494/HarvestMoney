package com.harvestmoney.bounty.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.harvestmoney.bounty.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class HomeViewModelFactory @Inject constructor(
    private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        
        return try {
            @Suppress("UNCHECKED_CAST")
            HomeViewModel(context, applicationScope) as T
        } catch (e: Exception) {
            throw RuntimeException("Failed to create HomeViewModel", e)
        }
    }
}
