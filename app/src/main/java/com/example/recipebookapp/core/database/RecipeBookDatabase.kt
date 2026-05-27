package com.example.recipebookapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class RecipeBookDatabase : RoomDatabase() {
    abstract fun recipeCacheDao(): RecipeCacheDao
}
