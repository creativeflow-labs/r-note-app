package com.rnote.app.ui.notelist

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
import java.text.DateFormat
import java.util.Date
import java.util.Locale

data class NoteListUiState(
    val allNotes: List<NoteEntity> = emptyList(),
    val visibleNotes: List<NoteEntity> = emptyList(),
    val groupedNotes: Map<String, List<NoteEntity>> = emptyMap(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val sortOption: NoteSortOption = NoteSortOption.LATEST,
    val isEditMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val showPromptSelector: Boolean = false,
    val exportTarget: ExportTarget = ExportTarget.ALL
)

enum class ExportTarget { ALL, SELECTED }

enum class NoteSortOption {
    LATEST,
    OLDEST,
    BEST_MOOD,
    LOW_MOOD
}

class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                _uiState.update {
                    val visibleNotes = applySearchAndSort(
                        notes = notes,
                        query = it.searchQuery,
                        sortOption = it.sortOption
                    )
                    it.copy(
                        allNotes = notes,
                        visibleNotes = visibleNotes,
                        groupedNotes = groupNotes(visibleNotes),
                        selectedIds = it.selectedIds.intersect(visibleNotes.map { note -> note.id }.toSet())
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            val visibleNotes = applySearchAndSort(
                notes = it.allNotes,
                query = query,
                sortOption = it.sortOption
            )
            it.copy(
                isSearchActive = true,
                searchQuery = query,
                visibleNotes = visibleNotes,
                groupedNotes = groupNotes(visibleNotes),
                selectedIds = it.selectedIds.intersect(visibleNotes.map { note -> note.id }.toSet())
            )
        }
    }

    fun activateSearch() {
        _uiState.update { it.copy(isSearchActive = true) }
    }

    fun closeSearch() {
        _uiState.update {
            val visibleNotes = applySearchAndSort(
                notes = it.allNotes,
                query = "",
                sortOption = it.sortOption
            )
            it.copy(
                isSearchActive = false,
                searchQuery = "",
                visibleNotes = visibleNotes,
                groupedNotes = groupNotes(visibleNotes),
                selectedIds = it.selectedIds.intersect(visibleNotes.map { note -> note.id }.toSet())
            )
        }
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun updateSortOption(sortOption: NoteSortOption) {
        _uiState.update {
            val visibleNotes = applySearchAndSort(
                notes = it.allNotes,
                query = it.searchQuery,
                sortOption = sortOption
            )
            it.copy(
                sortOption = sortOption,
                visibleNotes = visibleNotes,
                groupedNotes = groupNotes(visibleNotes)
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update {
            it.copy(
                isEditMode = !it.isEditMode,
                selectedIds = emptySet()
            )
        }
    }

    fun toggleSelection(noteId: String) {
        _uiState.update {
            val newSelection = if (noteId in it.selectedIds) {
                it.selectedIds - noteId
            } else {
                it.selectedIds + noteId
            }
            it.copy(selectedIds = newSelection)
        }
    }

    fun selectAll() {
        _uiState.update {
            it.copy(selectedIds = it.visibleNotes.map { note -> note.id }.toSet())
        }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedIds.toList()
            if (ids.isNotEmpty()) {
                repository.deleteNotesByIds(ids)
            }
            _uiState.update {
                it.copy(isEditMode = false, selectedIds = emptySet())
            }
        }
    }

    fun requestChatGptExport(target: ExportTarget) {
        _uiState.update {
            it.copy(showPromptSelector = true, exportTarget = target)
        }
    }

    fun hidePromptSelector() {
        _uiState.update { it.copy(showPromptSelector = false) }
    }

    fun getNotesForExport(): List<NoteEntity> {
        val state = _uiState.value
        return when (state.exportTarget) {
            ExportTarget.ALL -> state.visibleNotes
            ExportTarget.SELECTED -> state.allNotes.filter { it.id in state.selectedIds }
        }
    }

    private fun applySearchAndSort(
        notes: List<NoteEntity>,
        query: String,
        sortOption: NoteSortOption
    ): List<NoteEntity> {
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        val filtered = if (normalizedQuery.isEmpty()) {
            notes
        } else {
            notes.filter { note ->
                val date = dateFormat.format(Date(note.createdAt))
                listOf(
                    note.title,
                    note.body,
                    note.emotionLabel,
                    note.emotionEmoji,
                    note.emotionScore.toString(),
                    date
                ).any { value -> value.lowercase(Locale.getDefault()).contains(normalizedQuery) }
            }
        }

        return when (sortOption) {
            NoteSortOption.LATEST -> filtered.sortedByDescending { it.createdAt }
            NoteSortOption.OLDEST -> filtered.sortedBy { it.createdAt }
            NoteSortOption.BEST_MOOD -> filtered.sortedWith(
                compareByDescending<NoteEntity> { it.emotionScore }.thenByDescending { it.createdAt }
            )
            NoteSortOption.LOW_MOOD -> filtered.sortedWith(
                compareBy<NoteEntity> { it.emotionScore }.thenByDescending { it.createdAt }
            )
        }
    }

    private fun groupNotes(notes: List<NoteEntity>): Map<String, List<NoteEntity>> {
        return notes.groupBy { note ->
            dateFormat.format(Date(note.createdAt))
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteListViewModel(repository) as T
        }
    }
}
