package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    fun getTopScores(limit: Int = 100): Flow<List<ScoreEntity>> = scoreDao.getTopScores(limit)

    suspend fun saveScore(score: ScoreEntity) {
        scoreDao.insertScore(score)
    }

    suspend fun clearAllScores() {
        scoreDao.clearAllScores()
    }
}
