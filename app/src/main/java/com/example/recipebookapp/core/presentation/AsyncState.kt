package com.example.recipebookapp.core.presentation

sealed interface AsyncState<out T> {
    data object Loading : AsyncState<Nothing>
    data class Success<T>(val data: T) : AsyncState<T>
    data class Error(val message: String) : AsyncState<Nothing>
    data object Empty : AsyncState<Nothing>
}
