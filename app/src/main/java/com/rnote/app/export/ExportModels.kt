package com.rnote.app.export

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
    val version: String = "0.2.0",
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

    fun toShareText(notes: List<NoteEntity>, promptType: PromptType): String {
        val sorted = notes.sortedBy { it.createdAt }
        val fromDate = sorted.firstOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val toDate = sorted.lastOrNull()?.createdAt?.let { dateFormat.format(Date(it)) } ?: ""
        val avgScore = if (sorted.isNotEmpty()) sorted.map { it.emotionScore }.average().toInt() else 0

        val positiveCount = sorted.count { it.sentimentHint == "positive" }
        val neutralCount = sorted.count { it.sentimentHint == "neutral" }
        val negativeCount = sorted.count { it.sentimentHint == "negative" }

        val sb = StringBuilder()

        // Prompt header
        sb.appendLine(promptType.prompt)
        sb.appendLine()

        // Summary
        sb.appendLine("---")
        sb.appendLine("[데이터 요약]")
        sb.appendLine("기간: $fromDate ~ $toDate")
        sb.appendLine("총 기록: ${sorted.size}개")
        sb.appendLine("평균 감정 점수: ${avgScore}%")
        sb.appendLine("감정 분포: 긍정 ${positiveCount}개 / 중립 ${neutralCount}개 / 부정 ${negativeCount}개")
        sb.appendLine()

        // Timeline
        sb.appendLine("[감정 흐름]")
        sorted.forEach { note ->
            val date = dateFormat.format(Date(note.createdAt))
            sb.appendLine("$date | ${note.emotionEmoji} ${note.emotionScore}%")
        }
        sb.appendLine()

        // Notes
        sb.appendLine("[상세 기록]")
        sorted.forEach { note ->
            val date = dateFormat.format(Date(note.createdAt))
            sb.appendLine("---")
            sb.appendLine("\uD83D\uDCC5 $date | ${note.emotionEmoji} ${note.emotionScore}%")
            if (note.emotionLabel.isNotEmpty()) {
                sb.appendLine("감정: ${note.emotionLabel}")
            }
            if (note.title.isNotEmpty()) {
                sb.appendLine("제목: ${note.title}")
            }
            if (note.body.isNotEmpty()) {
                sb.appendLine(note.body)
            }
            sb.appendLine()
        }

        return sb.toString()
    }
}

enum class PromptType(val label: String, val prompt: String) {
    EMOTION_ANALYSIS(
        label = "감정 패턴 분석",
        prompt = """아래는 R:Note 앱에서 내보낸 감정 기록 데이터입니다.
이 데이터를 바탕으로 다음을 분석해주세요:
1. 전체 감정 흐름과 패턴 (상승/하강 추세, 변동성)
2. 반복되는 감정 트리거나 상황
3. 긍정/부정 감정의 비율과 변화 추이
4. 심리적 건강을 위한 구체적인 제안"""
    ),
    WEEKLY_REPORT(
        label = "주간/월간 리포트",
        prompt = """아래는 R:Note 앱에서 내보낸 감정 기록 데이터입니다.
이 데이터를 바탕으로 주간/월간 리포트를 작성해주세요:
1. 주차별 감정 평균 점수와 변화
2. 가장 기분이 좋았던 날과 힘들었던 날
3. 전체 기간의 감정 요약
4. 다음 기간을 위한 추천 활동"""
    ),
    COUNSELING(
        label = "종합 심리 상담",
        prompt = """아래는 R:Note 앱에서 내보낸 감정 기록 데이터입니다.
심리 상담사의 관점에서 다음을 분석하고 조언해주세요:
1. 기록에서 드러나는 주요 감정 패턴
2. 잠재적인 스트레스 요인 분석
3. 감정 조절을 위한 실용적인 전략
4. 긍정적으로 발전하고 있는 부분 격려
따뜻하고 공감적인 어조로 답변해주세요."""
    )
}
