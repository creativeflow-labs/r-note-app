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
    val notes: List<NoteEntity> = emptyList(),
    val groupedNotes: Map<String, List<NoteEntity>> = emptyMap(),
    val isEditMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
    val showPromptSelector: Boolean = false,
    val exportTarget: ExportTarget = ExportTarget.ALL
)

enum class ExportTarget { ALL, SELECTED }

class NoteListViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                val grouped = notes.groupBy { note ->
                    dateFormat.format(Date(note.createdAt))
                }
                _uiState.update {
                    it.copy(notes = notes, groupedNotes = grouped)
                }
            }
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
            it.copy(selectedIds = it.notes.map { note -> note.id }.toSet())
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
            ExportTarget.ALL -> state.notes
            ExportTarget.SELECTED -> state.notes.filter { it.id in state.selectedIds }
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteListViewModel(repository) as T
        }
    }
}
