package com.rnote.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.rnote.app.data.local.NoteEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportToJson(context: Context, notes: List<NoteEntity>): File {
        val exportPackage = ExportMapper.toExportPackage(notes)
        val json = gson.toJson(exportPackage)
        val fileName = "rnote_export_${fileTimestamp.format(Date())}.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(json)
        return file
    }

    fun createJsonShareIntent(context: Context, notes: List<NoteEntity>): Intent {
        val file = exportToJson(context, notes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "R:Note 감정 기록 데이터")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createChatGptShareIntent(notes: List<NoteEntity>, promptType: PromptType): Intent {
        val text = ExportMapper.toShareText(notes, promptType)

        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "R:Note 감정 분석 요청")
        }
    }
}
