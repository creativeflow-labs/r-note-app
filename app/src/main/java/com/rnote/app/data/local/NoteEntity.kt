package com.rnote.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val emotionEmoji: String = "\uD83D\uDE10",  // 😐
    val emotionScore: Int = 40,                   // 0~100
    val emotionLabel: String = "",                 // "생각보다 좋았음"
    val title: String = "",
    val body: String = "",
    val wordCount: Int = 0,
    val sentimentHint: String = "neutral",         // positive | neutral | negative
    val isDraft: Boolean = false,
    val localUserId: String = "local_default"
)
