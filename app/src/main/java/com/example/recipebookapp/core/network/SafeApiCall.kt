package com.example.recipebookapp.core.network

import com.example.recipebookapp.core.common.Resource
import com.example.recipebookapp.core.datastore.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeApiCall @Inject constructor(
    private val sessionStorage: SessionStorage,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> execute(block: suspend () -> T): Resource<T> = withContext(Dispatchers.IO) {
        runCatching { block() }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { throwable ->
                when (throwable) {
                    is HttpException -> {
                        if (throwable.code() == 401) {
                            sessionStorage.clearToken()
                        }
                        val body = throwable.response()?.errorBody()?.string()
                        val message = body?.let {
                            runCatching { json.decodeFromString<ErrorDto>(it).error }.getOrNull()
                        } ?: "Server error ${throwable.code()}"
                        Resource.Error(message, throwable)
                    }
                    is IOException -> Resource.Error("Проблема с сетью или сервером. Попробуйте еще раз", throwable)
                    else -> Resource.Error(throwable.message ?: "Unknown error", throwable)
                }
            },
        )
    }

    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 2,
        initialDelayMs: Long = 350L,
        block: suspend () -> T,
    ): Resource<T> {
        require(maxAttempts >= 1) { "maxAttempts must be >= 1" }

        var currentDelayMs = initialDelayMs
        repeat(maxAttempts) { attempt ->
            when (val result = execute(block)) {
                is Resource.Success -> return result
                is Resource.Error -> {
                    if (attempt == maxAttempts - 1 || !isTransientFailure(result.throwable)) {
                        return result
                    }
                    delay(currentDelayMs)
                    currentDelayMs *= 2
                }
            }
        }

        return Resource.Error("Unknown error")
    }

    private fun isTransientFailure(throwable: Throwable?): Boolean {
        return when (throwable) {
            is IOException -> true
            is HttpException -> throwable.code() in setOf(408, 500, 502, 503, 504)
            else -> false
        }
    }
}
