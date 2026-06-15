package com.example.aonime.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ─── Entity ───────────────────────────────────────────────────────────────────

@Entity(tableName = "favorites")
data class FavoriteAnime(
    @PrimaryKey val slug: String,
    val title: String,
    val image: String,
    val type: String,
    val score: String,
    val addedAt: Long = System.currentTimeMillis(),
)

// ─── DAO ──────────────────────────────────────────────────────────────────────

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteAnime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(anime: FavoriteAnime)

    @Delete
    suspend fun removeFavorite(anime: FavoriteAnime)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE slug = :slug)")
    fun isFavorite(slug: String): Flow<Boolean>

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun removeBySlug(slug: String)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(entities = [FavoriteAnime::class], version = 1, exportSchema = false)
abstract class AonimeDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
