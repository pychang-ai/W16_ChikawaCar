package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopScores(limit: Int = 100): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Query("DELETE FROM game_scores")
    suspend fun clearAllScores()
}
