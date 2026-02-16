package com.rnote.app.ui.note

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rnote.app.R
import com.rnote.app.data.local.NoteEntity
import com.rnote.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmotionLevel(
    val emoji: String,
    val score: Int,
    val labelKey: String,
    @StringRes val labelRes: Int,
    val sentiment: String
)

val EMOTION_SCALE = listOf(
    EmotionLevel("\uD83D\uDE2D",   0, "Worst",      R.string.emotion_worst,        "negative"),  // 😭
    EmotionLevel("\uD83D\uDE22",  10, "Terrible",   R.string.emotion_terrible,     "negative"),  // 😢
    EmotionLevel("\uD83D\uDE1E",  20, "Very Bad",   R.string.emotion_very_bad,     "negative"),  // 😞
    EmotionLevel("\uD83D\uDE15",  30, "Bad",        R.string.emotion_bad,          "negative"),  // 😕
    EmotionLevel("\uD83D\uDE41",  40, "A Bit Down", R.string.emotion_a_bit_down,   "neutral"),   // 🙁
    EmotionLevel("\uD83D\uDE10",  50, "Neutral",    R.string.emotion_neutral,      "neutral"),   // 😐
    EmotionLevel("\uD83D\uDE42",  60, "A Bit Good", R.string.emotion_a_bit_good,   "positive"),  // 🙂
    EmotionLevel("\uD83D\uDE0A",  70, "Good",       R.string.emotion_good,         "positive"),  // 😊
    EmotionLevel("\uD83D\uDE04",  80, "Very Good",  R.string.emotion_very_good,    "positive"),  // 😄
    EmotionLevel("\uD83D\uDE06",  90, "Great",      R.string.emotion_great,        "positive"),  // 😆
    EmotionLevel("\uD83E\uDD29", 100, "Amazing",    R.string.emotion_amazing,      "positive")   // 🤩
)

fun findEmotionLevel(score: Int): EmotionLevel {
    return EMOTION_SCALE.find { it.score == score } ?: EMOTION_SCALE[5]
}

data class NoteUiState(
    val id: String = "",
    val selectedEmoji: String = "\uD83D\uDE10",  // 😐
    val emotionScore: Int = 50,
    val emotionLabel: String = "Neutral",
    val title: String = "",
    val body: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val hasChanges: Boolean = false,
    val isEditing: Boolean = false
)

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    private var initialState = NoteUiState()

    fun loadNote(noteId: String?) {
        if (noteId == null) {
            // Check for drafts
            viewModelScope.launch {
                val draft = repository.getDraft()
                if (draft != null) {
                    val state = NoteUiState(
                        id = draft.id,
                        selectedEmoji = draft.emotionEmoji,
                        emotionScore = draft.emotionScore,
                        emotionLabel = draft.emotionLabel,
                        title = draft.title,
                        body = draft.body,
                        isEditing = false
                    )
                    _uiState.value = state
                    initialState = state
                }
            }
        } else {
            viewModelScope.launch {
                val note = repository.getNoteById(noteId) ?: return@launch
                val state = NoteUiState(
                    id = note.id,
                    selectedEmoji = note.emotionEmoji,
                    emotionScore = note.emotionScore,
                    emotionLabel = note.emotionLabel,
                    title = note.title,
                    body = note.body,
                    isEditing = true
                )
                _uiState.value = state
                initialState = state
            }
        }
    }

    fun selectEmotionLevel(level: EmotionLevel) {
        _uiState.update {
            it.copy(
                selectedEmoji = level.emoji,
                emotionScore = level.score,
                emotionLabel = level.labelKey,
                hasChanges = true
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, hasChanges = true) }
    }

    fun updateBody(body: String) {
        _uiState.update { it.copy(body = body, hasChanges = true) }
    }

    fun saveNote(onSaved: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val state = _uiState.value
            val sentiment = determineSentiment(state.emotionScore)
            val wordCount = (state.title + " " + state.body).trim().split("\\s+".toRegex()).size

            val note = NoteEntity(
                id = state.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                emotionEmoji = state.selectedEmoji,
                emotionScore = state.emotionScore,
                emotionLabel = state.emotionLabel,
                title = state.title,
                body = state.body,
                wordCount = wordCount,
                sentimentHint = sentiment,
                isDraft = false
            )

            repository.clearDrafts()
            repository.saveNote(note)

            _uiState.update { it.copy(isSaving = false, isSaved = true, hasChanges = false) }
            onSaved()
        }
    }

    fun saveDraft() {
        val state = _uiState.value
        if (!state.hasChanges) return
        if (state.selectedEmoji.isEmpty() && state.body.isEmpty() && state.title.isEmpty()) return

        viewModelScope.launch {
            val note = NoteEntity(
                id = state.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                emotionEmoji = state.selectedEmoji,
                emotionScore = state.emotionScore,
                emotionLabel = state.emotionLabel,
                title = state.title,
                body = state.body,
                wordCount = 0,
                sentimentHint = determineSentiment(state.emotionScore),
                isDraft = true
            )
            repository.saveDraft(note)
        }
    }

    private fun determineSentiment(score: Int): String {
        return findEmotionLevel(score).sentiment
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(repository) as T
        }
    }
}
