package com.dopaminebox.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dopaminebox.app.data.formatCurrency
import com.dopaminebox.app.ui.screens.GameReelScreen
import com.dopaminebox.app.ui.screens.StatsScreen
import com.dopaminebox.app.viewmodel.GameViewModel
import com.dopaminebox.app.viewmodel.GameViewModelFactory

enum class Tab { FEED, STATS }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DopamineBoxApp() {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(context)
    )
    
    val gameState by viewModel.gameState.collectAsState()
    var currentTab by remember { mutableStateOf(Tab.FEED) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Content
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "tab_content"
        ) { tab ->
            when (tab) {
                Tab.FEED -> GameReelScreen(viewModel = viewModel)
                Tab.STATS -> StatsScreen(
                    gameState = gameState,
                    onResetBalance = { viewModel.resetBalance() }
                )
            }
        }
        
        // Bottom Navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White.copy(alpha = 0.08f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    NavButton(
                        emoji = "🎮",
                        label = "Play",
                        isActive = currentTab == Tab.FEED,
                        onClick = { currentTab = Tab.FEED },
                        showDivider = true
                    )
                    NavButton(
                        emoji = "📊",
                        label = "Stats",
                        isActive = currentTab == Tab.STATS,
                        onClick = { currentTab = Tab.STATS },
                        showDivider = false
                    )
                }
            }
        }
    }
}

@Composable
fun NavButton(
    emoji: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Box {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 10.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (isActive) Color.White.copy(alpha = 0.12f) else Color.Transparent
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }
        }
        
        if (showDivider) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }
    }
}
