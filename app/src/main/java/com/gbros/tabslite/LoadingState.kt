package com.gbros.tabslite

sealed class LoadingState {
    data object NotStarted : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val messageStringRef: Int, val errorDetails: String = "") : LoadingState()
}