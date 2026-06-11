package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val characterType: String, // "CHIIKAWA", "HACHIWARE", "USAGI"
    val timestamp: Long = System.currentTimeMillis()
)
