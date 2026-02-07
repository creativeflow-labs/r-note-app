package com.rnote.app.ui.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rnote.app.ui.theme.CardBackground
import com.rnote.app.ui.theme.CloudDancerDark
import com.rnote.app.ui.theme.DividerColor
import com.rnote.app.ui.theme.SagePrimary
import com.rnote.app.ui.theme.TextHint
import com.rnote.app.ui.theme.TextPrimary
import com.rnote.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    // Auto-save draft when going to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveDraft()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Back handler
    BackHandler {
        if (uiState.hasChanges) {
            showExitDialog = true
        } else {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar with save button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.saveNote(onNavigateBack) },
                enabled = !uiState.isSaving
            ) {
                Text(
                    text = if (uiState.isSaving) "저장 중..." else "저장",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.isSaving) TextHint else SagePrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Emotion input area
            EmotionSection(
                selectedEmoji = uiState.selectedEmoji,
                emotionScore = uiState.emotionScore,
                emotionLabel = uiState.emotionLabel,
                onEmojiSelected = { viewModel.selectEmotion(it) },
                onIncreaseScore = { viewModel.increaseScore() },
                onDecreaseScore = { viewModel.decreaseScore() },
                onLabelChanged = { viewModel.updateEmotionLabel(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title input
            BasicTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                cursorBrush = SolidColor(SagePrimary),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = "제목 (선택)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextHint
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerColor)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body input
            BasicTextField(
                value = uiState.body,
                onValueChange = { viewModel.updateBody(it) },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = TextPrimary,
                    lineHeight = 26.sp
                ),
                cursorBrush = SolidColor(SagePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.body.isEmpty()) {
                            Text(
                                text = "오늘의 감정을 자유롭게 적어보세요...",
                                fontSize = 16.sp,
                                color = TextHint,
                                lineHeight = 26.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    // Exit confirmation bottom sheet
    if (showExitDialog) {
        ExitConfirmBottomSheet(
            onSaveAndExit = {
                showExitDialog = false
                viewModel.saveNote(onNavigateBack)
            },
            onDiscardAndExit = {
                showExitDialog = false
                onNavigateBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }
}

@Composable
private fun EmotionSection(
    selectedEmoji: String,
    emotionScore: Int,
    emotionLabel: String,
    onEmojiSelected: (Emotion) -> Unit,
    onIncreaseScore: () -> Unit,
    onDecreaseScore: () -> Unit,
    onLabelChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        // Emoji selection row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EMOTIONS.forEach { emotion ->
                val isSelected = emotion.emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) SagePrimary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.background
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SagePrimary else DividerColor,
                            shape = CircleShape
                        )
                        .clickable { onEmojiSelected(emotion) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emotion.emoji, fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Score control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecreaseScore,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CloudDancerDark)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "감소",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = "${emotionScore}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SagePrimary
            )

            Spacer(modifier = Modifier.width(20.dp))

            IconButton(
                onClick = onIncreaseScore,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CloudDancerDark)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "증가",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emotion label
        BasicTextField(
            value = emotionLabel,
            onValueChange = onLabelChanged,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            cursorBrush = SolidColor(SagePrimary),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (emotionLabel.isEmpty()) {
                        Text(
                            text = "한줄로 감정을 표현해보세요",
                            fontSize = 14.sp,
                            color = TextHint,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitConfirmBottomSheet(
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "저장하지 않고 나가시겠습니까?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "작성 중인 내용이 사라집니다",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDiscardAndExit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "나가기",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
                TextButton(
                    onClick = onSaveAndExit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "저장하고 나가기",
                        color = SagePrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
