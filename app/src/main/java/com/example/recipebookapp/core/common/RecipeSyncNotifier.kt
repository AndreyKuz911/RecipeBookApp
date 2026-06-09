package com.example.recipebookapp.core.common

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class RecipeSyncNotifier @Inject constructor() {
    private val _recipeMutations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _favoriteMutations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val recipeMutations: SharedFlow<Unit> = _recipeMutations.asSharedFlow()
    val favoriteMutations: SharedFlow<Unit> = _favoriteMutations.asSharedFlow()

    fun notifyRecipeMutated() {
        _recipeMutations.tryEmit(Unit)
    }

    fun notifyFavoriteMutated() {
        _favoriteMutations.tryEmit(Unit)
    }
}
