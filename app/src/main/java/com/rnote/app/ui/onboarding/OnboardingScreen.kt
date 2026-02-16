package com.rnote.app.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.rnote.app.R
import com.rnote.app.ui.components.RNoteButton
import com.rnote.app.ui.theme.SagePrimary
import com.rnote.app.ui.theme.TextHint
import com.rnote.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    @androidx.annotation.StringRes val titleRes: Int,
    @androidx.annotation.StringRes val descRes: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "\uD83D\uDCDD",  // 📝
        titleRes = R.string.onboarding_title_1,
        descRes = R.string.onboarding_desc_1
    ),
    OnboardingPage(
        emoji = "\uD83D\uDE0A\uD83D\uDE14\uD83D\uDE10",  // 😊😔😐
        titleRes = R.string.onboarding_title_2,
        descRes = R.string.onboarding_desc_2
    ),
    OnboardingPage(
        emoji = "\uD83D\uDD12",  // 🔒
        titleRes = R.string.onboarding_title_3,
        descRes = R.string.onboarding_desc_3
    ),
    OnboardingPage(
        emoji = "\uD83D\uDCC2",  // 📂
        titleRes = R.string.onboarding_title_4,
        descRes = R.string.onboarding_desc_4
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Skip button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (!isLastPage) {
                TextButton(onClick = onFinished) {
                    Text(
                        text = stringResource(R.string.skip),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) SagePrimary
                            else TextHint.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Bottom button
        RNoteButton(
            text = if (isLastPage) stringResource(R.string.get_started) else stringResource(R.string.next),
            onClick = {
                if (isLastPage) {
                    onFinished()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.emoji,
            fontSize = 72.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(page.titleRes),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(page.descRes),
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
