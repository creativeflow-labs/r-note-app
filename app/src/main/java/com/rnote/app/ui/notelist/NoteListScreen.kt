package com.rnote.app.ui.notelist

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnote.app.data.local.NoteEntity
import com.rnote.app.export.ExportHelper
import com.rnote.app.export.PromptType
import com.rnote.app.ui.theme.AccentCoral
import com.rnote.app.ui.theme.CardBackground
import com.rnote.app.ui.theme.SagePrimary
import com.rnote.app.ui.theme.SurfaceWhite
import com.rnote.app.ui.theme.TextHint
import com.rnote.app.ui.theme.TextPrimary
import com.rnote.app.ui.theme.TextSecondary

@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNewNote: () -> Unit,
    onNoteClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NoteListTopBar(
                isEditMode = uiState.isEditMode,
                selectedCount = uiState.selectedIds.size,
                hasNotes = uiState.notes.isNotEmpty(),
                showExportMenu = uiState.showExportMenu,
                onToggleEditMode = { viewModel.toggleEditMode() },
                onDelete = { viewModel.deleteSelected() },
                onShowExportMenu = { viewModel.showExportMenu() },
                onDismissExportMenu = { viewModel.hideExportMenu() },
                onExportChatGptAll = {
                    viewModel.requestChatGptExport(ExportTarget.ALL)
                },
                onExportJsonAll = {
                    viewModel.hideExportMenu()
                    val intent = ExportHelper.createJsonShareIntent(context, uiState.notes)
                    context.startActivity(Intent.createChooser(intent, "JSON 내보내기"))
                },
                onExportChatGptSelected = {
                    viewModel.requestChatGptExport(ExportTarget.SELECTED)
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isEditMode) {
                FloatingActionButton(
                    onClick = onNewNote,
                    containerColor = SagePrimary,
                    contentColor = SurfaceWhite,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "새 노트 작성")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.notes.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                uiState.groupedNotes.forEach { (date, notes) ->
                    item(key = "header_$date") {
                        DateHeader(date = date)
                    }
                    items(notes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            isEditMode = uiState.isEditMode,
                            isSelected = note.id in uiState.selectedIds,
                            onToggleSelection = { viewModel.toggleSelection(note.id) },
                            onClick = {
                                if (uiState.isEditMode) {
                                    viewModel.toggleSelection(note.id)
                                } else {
                                    onNoteClick(note.id)
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Prompt selector bottom sheet
    if (uiState.showPromptSelector) {
        PromptSelectorBottomSheet(
            onSelectPrompt = { promptType ->
                viewModel.hidePromptSelector()
                val notes = viewModel.getNotesForExport()
                val intent = ExportHelper.createChatGptShareIntent(notes, promptType)
                context.startActivity(Intent.createChooser(intent, "ChatGPT로 분석 요청"))
            },
            onDismiss = { viewModel.hidePromptSelector() }
        )
    }
}

@Composable
private fun NoteListTopBar(
    isEditMode: Boolean,
    selectedCount: Int,
    hasNotes: Boolean,
    showExportMenu: Boolean,
    onToggleEditMode: () -> Unit,
    onDelete: () -> Unit,
    onShowExportMenu: () -> Unit,
    onDismissExportMenu: () -> Unit,
    onExportChatGptAll: () -> Unit,
    onExportJsonAll: () -> Unit,
    onExportChatGptSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isEditMode) "${selectedCount}개 선택" else "R:note",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditMode) {
                if (selectedCount > 0) {
                    IconButton(onClick = onExportChatGptSelected) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "선택 내보내기",
                            tint = SagePrimary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = AccentCoral
                        )
                    }
                }
                TextButton(onClick = onToggleEditMode) {
                    Text(
                        text = "완료",
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                if (hasNotes) {
                    Box {
                        IconButton(onClick = onShowExportMenu) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "메뉴",
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = onDismissExportMenu
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("ChatGPT로 분석 요청", fontSize = 14.sp)
                                },
                                onClick = onExportChatGptAll
                            )
                            DropdownMenuItem(
                                text = {
                                    Text("JSON 파일로 내보내기", fontSize = 14.sp)
                                },
                                onClick = onExportJsonAll
                            )
                        }
                    }
                }
                TextButton(onClick = onToggleEditMode) {
                    Text(
                        text = "편집",
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromptSelectorBottomSheet(
    onSelectPrompt: (PromptType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "ChatGPT에서 어떤 분석을 원하시나요?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "선택한 분석 유형에 맞는 프롬프트가 자동으로 포함됩니다",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))

            PromptType.entries.forEach { promptType ->
                PromptOption(
                    promptType = promptType,
                    onClick = { onSelectPrompt(promptType) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PromptOption(
    promptType: PromptType,
    onClick: () -> Unit
) {
    val (emoji, description) = when (promptType) {
        PromptType.EMOTION_ANALYSIS -> "\uD83D\uDCC8" to "감정 흐름, 트리거, 변화 추이를 분석합니다"
        PromptType.WEEKLY_REPORT -> "\uD83D\uDCCB" to "주간/월간 요약 리포트를 생성합니다"
        PromptType.COUNSELING -> "\uD83D\uDCAC" to "따뜻한 심리 상담 관점으로 조언합니다"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = promptType.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    Text(
        text = date,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun NoteItem(
    note: NoteEntity,
    isEditMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SagePrimary,
                    uncheckedColor = TextHint
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = note.emotionEmoji,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.title.ifEmpty {
                    note.body.take(50).ifEmpty { "감정 기록" }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.emotionLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.emotionLabel,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCDD",
            fontSize = 56.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 기록이 없어요",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "오른쪽 아래 버튼을 눌러\n첫 번째 감정을 기록해보세요",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}
