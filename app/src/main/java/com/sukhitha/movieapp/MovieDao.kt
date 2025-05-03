package com.sukhitha.movieapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Query("SELECT * FROM movies WHERE actors LIKE '%' || :actor || '%' COLLATE NOCASE")
    suspend fun searchByActor(actor: String): List<Movie>

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<Movie>
}