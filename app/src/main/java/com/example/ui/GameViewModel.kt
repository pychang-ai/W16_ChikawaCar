package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScoreEntity
import com.example.data.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    START,
    CHARACTER_SELECT,
    PLAYING,
    GAME_OVER,
    LEADERBOARD
}

enum class CharacterType(val displayName: String, val speed: Float, val luck: Float, val colorHex: Long, val catchphrase: String) {
    CHIIKAWA("吉伊 (Chiikawa)", 1.0f, 1.4f, 0xFFFFEBF0, "哇！(Wah!)"),
    HACHIWARE("小八 (Hachiware)", 1.2f, 1.1f, 0xFFE1F5FE, "哈哈！(Haha!)"),
    USAGI("兔兔 (Usagi)", 1.5f, 0.8f, 0xFFFFFDE7, "呀哈！(Yaha!)")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScoreRepository
    val topScores: StateFlow<List<ScoreEntity>>

    private val _screenState = MutableStateFlow(ScreenState.START)
    val screenState = _screenState.asStateFlow()

    private val _selectedCharacter = MutableStateFlow(CharacterType.CHIIKAWA)
    val selectedCharacter = _selectedCharacter.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore = _currentScore.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ScoreRepository(database.scoreDao())
        topScores = repository.getTopScores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun setScreen(state: ScreenState) {
        _screenState.value = state
    }

    fun selectCharacter(charType: CharacterType) {
        _selectedCharacter.value = charType
    }

    fun updateScore(newScore: Int) {
        _currentScore.value = newScore
    }

    fun startNewGame() {
        _currentScore.value = 0
        _screenState.value = ScreenState.PLAYING
    }

    fun saveGameScore(playerName: String) {
        viewModelScope.launch {
            val record = ScoreEntity(
                playerName = playerName.trim().ifBlank { "無名小可愛" },
                score = _currentScore.value,
                characterType = _selectedCharacter.value.name
            )
            repository.saveScore(record)
            _screenState.value = ScreenState.LEADERBOARD
        }
    }

    fun clearAllScores() {
        viewModelScope.launch {
            repository.clearAllScores()
        }
    }
}
