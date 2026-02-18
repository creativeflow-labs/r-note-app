package com.rnote.app.ui.notelist

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.rnote.app.R
import com.rnote.app.data.local.NoteEntity
import com.rnote.app.export.ExportHelper
import com.rnote.app.export.PromptType
import com.rnote.app.ui.components.BannerAd
import com.rnote.app.ui.note.findEmotionLevel
import com.rnote.app.ui.theme.AccentCoral
import com.rnote.app.ui.theme.CardBackground
import com.rnote.app.ui.theme.CloudDancer
import com.rnote.app.ui.theme.HahmletStyle
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

    // Double back press to exit
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, context.getString(R.string.back_press_exit_toast), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BannerAd() },
        topBar = {
            NoteListTopBar(
                isEditMode = uiState.isEditMode,
                selectedCount = uiState.selectedIds.size,
                totalCount = uiState.notes.size,
                hasNotes = uiState.notes.isNotEmpty(),
                onToggleEditMode = { viewModel.toggleEditMode() },
                onSelectAll = { viewModel.selectAll() },
                onDeselectAll = { viewModel.deselectAll() },
                onDelete = { viewModel.deleteSelected() },
                onExportChatGptAll = {
                    viewModel.requestChatGptExport(ExportTarget.ALL)
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
                    contentColor = CloudDancer,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pen_fill),
                        contentDescription = stringResource(R.string.cd_new_note),
                        modifier = Modifier.size(20.dp)
                    )
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
                val intent = ExportHelper.createChatGptShareIntent(context, notes, promptType)
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.chatgpt_share_chooser)))
            },
            onDismiss = { viewModel.hidePromptSelector() }
        )
    }
}

@Composable
private fun NoteListTopBar(
    isEditMode: Boolean,
    selectedCount: Int,
    totalCount: Int,
    hasNotes: Boolean,
    onToggleEditMode: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDelete: () -> Unit,
    onExportChatGptAll: () -> Unit,
    onExportChatGptSelected: () -> Unit
) {
    val isAllSelected = selectedCount == totalCount && totalCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditMode) {
                Text(
                    text = stringResource(R.string.selected_count, selectedCount),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { if (isAllSelected) onDeselectAll() else onSelectAll() }
                ) {
                    Text(
                        text = if (isAllSelected) stringResource(R.string.deselect_all) else stringResource(R.string.select_all),
                        fontSize = 13.sp,
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "R:note",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditMode) {
                if (selectedCount > 0) {
                    IconButton(onClick = onExportChatGptSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_openai),
                            contentDescription = stringResource(R.string.cd_export_selected),
                            tint = SagePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = AccentCoral
                        )
                    }
                }
                TextButton(onClick = onToggleEditMode) {
                    Text(
                        text = stringResource(R.string.done),
                        color = SagePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                if (hasNotes) {
                    IconButton(onClick = onExportChatGptAll) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_openai),
                            contentDescription = stringResource(R.string.cd_chatgpt_analysis),
                            tint = SagePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onToggleEditMode) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pencil_square),
                        contentDescription = stringResource(R.string.cd_edit),
                        tint = SagePrimary,
                        modifier = Modifier.size(20.dp)
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
                text = stringResource(R.string.prompt_selector_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.prompt_selector_desc),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = promptType.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(promptType.labelRes),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(promptType.descRes),
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
                    note.body.take(50).ifEmpty { stringResource(R.string.empty_note_fallback) }
                },
                style = HahmletStyle.copy(
                    fontSize = 15.sp,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.emotionLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(findEmotionLevel(note.emotionScore).labelRes),
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
            text = stringResource(R.string.empty_state_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_desc),
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}
