package com.example.recipebookapp.core.presentation

import com.example.recipebookapp.core.common.Resource

fun <T> Resource<T>.toAsyncState(
    isEmpty: (T) -> Boolean = { false },
): AsyncState<T> = when (this) {
    is Resource.Success -> if (isEmpty(data)) AsyncState.Empty else AsyncState.Success(data)
    is Resource.Error -> AsyncState.Error(message)
}
