package com.rnote.app.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rnote.app.data.local.NoteEntity
import com.rnote.app.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Emotion(
    val emoji: String,
    val defaultScore: Int,
    val sentiment: String
)

val EMOTIONS = listOf(
    Emotion("\uD83D\uDE00", 70, "positive"),   // 😀
    Emotion("\uD83D\uDE42", 50, "positive"),   // 🙂
    Emotion("\uD83D\uDE10", 40, "neutral"),    // 😐
    Emotion("\uD83D\uDE14", 30, "negative")    // 😔
)

data class NoteUiState(
    val id: String = "",
    val selectedEmoji: String = "\uD83D\uDE10",  // 😐
    val emotionScore: Int = 40,
    val emotionLabel: String = "",
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

    fun selectEmotion(emotion: Emotion) {
        _uiState.update {
            it.copy(
                selectedEmoji = emotion.emoji,
                emotionScore = emotion.defaultScore,
                hasChanges = true
            )
        }
    }

    fun increaseScore() {
        _uiState.update {
            it.copy(
                emotionScore = (it.emotionScore + 10).coerceAtMost(100),
                hasChanges = true
            )
        }
    }

    fun decreaseScore() {
        _uiState.update {
            it.copy(
                emotionScore = (it.emotionScore - 10).coerceAtLeast(0),
                hasChanges = true
            )
        }
    }

    fun updateEmotionLabel(label: String) {
        _uiState.update { it.copy(emotionLabel = label, hasChanges = true) }
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
        return when {
            score >= 60 -> "positive"
            score >= 35 -> "neutral"
            else -> "negative"
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(repository) as T
        }
    }
}
