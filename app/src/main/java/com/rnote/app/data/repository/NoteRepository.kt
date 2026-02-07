package com.rnote.app.data.repository

import com.rnote.app.data.local.NoteDao
import com.rnote.app.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun getDraft(): NoteEntity? = noteDao.getDraft()

    suspend fun saveNote(note: NoteEntity) {
        val existing = noteDao.getNoteById(note.id)
        if (existing != null) {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNotesByIds(ids: List<String>) = noteDao.deleteNotesByIds(ids)

    suspend fun saveDraft(note: NoteEntity) {
        noteDao.clearDrafts()
        noteDao.insertNote(note.copy(isDraft = true))
    }

    suspend fun clearDrafts() = noteDao.clearDrafts()
}
