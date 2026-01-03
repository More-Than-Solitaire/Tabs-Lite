package com.gbros.tabslite

/**
 * Utility enum-like class to provide a standardized way for viewmodels to communicate the state of the view (or particular parts of the view).
 *
 * Usage: `passedState is LoadingState.Loading`
 */
sealed class LoadingState {
    data object NotStarted : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val messageStringRef: Int, val errorDetails: String = "") : LoadingState()
}