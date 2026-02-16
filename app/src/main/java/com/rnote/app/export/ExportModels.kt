package com.rnote.app.export

import android.content.Context
import androidx.annotation.StringRes
import com.rnote.app.R
import com.rnote.app.data.local.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportPackage(
    val export_info: ExportInfo,
    val emotion_timeline: List<EmotionTimelineEntry>,
    val notes: List<ExportNote>
)

data class ExportInfo(
    val app: String = "R:Note",
    val version: String = "0.3.0",
    val exported_at: String,
    val period: ExportPeriod,
    val total_notes: Int,
    val avg_emotion_score: Int,
    val sentiment_distribution: SentimentDistribution
)

data class ExportPeriod(
    val from: String,
    val to: String
)

data class SentimentDistribution(
    val positive: Int,
    val neutral: Int,
    val negative: Int
)

data class EmotionTimelineEntry(
    val date: String,
    val score: Int,
    val emoji: String
)

data class ExportNote(
    val id: String,
    val date: String,
    val emotion_emoji: String,
    val emotion_score: Int,
    val emotion_label: String,
    val sentiment: String,
    val title: String,
    val content: String,
    val word_count: Int
)

object ExportMapper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun toExportPackage(notes: List<NoteEntity>): ExportPackage {
        val sorted = notes.sortedBy { it.createdAt }
        val exportNotes = sorted.map { toExportNote(it) }
        val timeline = sorted.map { toTimelineEntry(it) }

        val fromDate = sorted.firstOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val toDate = sorted.lastOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val avgScore = if (sorted.isNotEmpty()) sorted.map { it.emotionScore }.average().toInt() else 0

        val sentimentDist = SentimentDistribution(
            positive = sorted.count { it.sentimentHint == "positive" },
            neutral = sorted.count { it.sentimentHint == "neutral" },
            negative = sorted.count { it.sentimentHint == "negative" }
        )

        return ExportPackage(
            export_info = ExportInfo(
                exported_at = dateTimeFormat.format(Date()),
                period = ExportPeriod(from = fromDate, to = toDate),
                total_notes = sorted.size,
                avg_emotion_score = avgScore,
                sentiment_distribution = sentimentDist
            ),
            emotion_timeline = timeline,
            notes = exportNotes
        )
    }

    private fun toExportNote(entity: NoteEntity): ExportNote {
        return ExportNote(
            id = entity.id,
            date = dateFormat.format(Date(entity.createdAt)),
            emotion_emoji = entity.emotionEmoji,
            emotion_score = entity.emotionScore,
            emotion_label = entity.emotionLabel,
            sentiment = entity.sentimentHint,
            title = entity.title,
            content = entity.body,
            word_count = entity.wordCount
        )
    }

    private fun toTimelineEntry(entity: NoteEntity): EmotionTimelineEntry {
        return EmotionTimelineEntry(
            date = dateFormat.format(Date(entity.createdAt)),
            score = entity.emotionScore,
            emoji = entity.emotionEmoji
        )
    }

    fun toShareText(context: Context, notes: List<NoteEntity>, promptType: PromptType): String {
        val sorted = notes.sortedBy { it.createdAt }
        val fromDate = sorted.firstOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val toDate = sorted.lastOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val avgScore = if (sorted.isNotEmpty()) sorted.map { it.emotionScore }.average().toInt() else 0

        val positiveCount = sorted.count { it.sentimentHint == "positive" }
        val neutralCount = sorted.count { it.sentimentHint == "neutral" }
        val negativeCount = sorted.count { it.sentimentHint == "negative" }

        val sb = StringBuilder()

        // Prompt header
        sb.appendLine(context.getString(promptType.promptRes))
        sb.appendLine()

        // Summary
        sb.appendLine("---")
        sb.appendLine(context.getString(R.string.export_data_summary))
        sb.appendLine(context.getString(R.string.export_period, fromDate, toDate))
        sb.appendLine(context.getString(R.string.export_total_notes, sorted.size))
        sb.appendLine(context.getString(R.string.export_avg_score, avgScore))
        sb.appendLine(context.getString(R.string.export_sentiment_dist, positiveCount, neutralCount, negativeCount))
        sb.appendLine()

        // Timeline
        sb.appendLine(context.getString(R.string.export_emotion_flow))
        sorted.forEach { note ->
            val date = dateFormat.format(Date(note.createdAt))
            sb.appendLine("$date | ${note.emotionEmoji} ${note.emotionScore}%")
        }
        sb.appendLine()

        // Notes
        sb.appendLine(context.getString(R.string.export_detail_records))
        sorted.forEach { note ->
            val date = dateFormat.format(Date(note.createdAt))
            sb.appendLine("---")
            sb.appendLine("\uD83D\uDCC5 $date | ${note.emotionEmoji} ${note.emotionScore}%")
            if (note.emotionLabel.isNotEmpty()) {
                sb.appendLine(context.getString(R.string.export_emotion_label, note.emotionLabel))
            }
            if (note.title.isNotEmpty()) {
                sb.appendLine(context.getString(R.string.export_title_label, note.title))
            }
            if (note.body.isNotEmpty()) {
                sb.appendLine(note.body)
            }
            sb.appendLine()
        }

        return sb.toString()
    }
}

enum class PromptType(
    @StringRes val labelRes: Int,
    @StringRes val promptRes: Int,
    @StringRes val descRes: Int,
    val emoji: String
) {
    EMOTION_ANALYSIS(
        labelRes = R.string.prompt_emotion_label,
        promptRes = R.string.prompt_emotion_analysis,
        descRes = R.string.prompt_emotion_desc,
        emoji = "\uD83D\uDCC8"  // 📈
    ),
    WEEKLY_REPORT(
        labelRes = R.string.prompt_weekly_label,
        promptRes = R.string.prompt_weekly_report,
        descRes = R.string.prompt_weekly_desc,
        emoji = "\uD83D\uDCCB"  // 📋
    ),
    COUNSELING(
        labelRes = R.string.prompt_counseling_label,
        promptRes = R.string.prompt_counseling,
        descRes = R.string.prompt_counseling_desc,
        emoji = "\uD83D\uDCAC"  // 💬
    )
}
