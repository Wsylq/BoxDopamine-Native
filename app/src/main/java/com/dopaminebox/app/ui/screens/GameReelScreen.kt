package com.dopaminebox.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopaminebox.app.data.formatCurrency
import com.dopaminebox.app.ui.games.CoinFlipGame
import com.dopaminebox.app.ui.games.HigherLowerGame
import com.dopaminebox.app.ui.games.PlinkoGame
import com.dopaminebox.app.viewmodel.GameViewModel
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

enum class GameType {
    COIN_FLIP, HIGHER_LOWER, PLINKO
}

data class GameMeta(
    val label: String,
    val emoji: String,
    val color: Color,
    val sub: String,
    val gradient: Brush
)

val GAME_METADATA = mapOf(
    GameType.COIN_FLIP to GameMeta(
        label = "Coin Flip",
        emoji = "🪙",
        color = Color(0xFFFFD700),
        sub = "50/50 · Win 2×",
        gradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF6B6B).copy(alpha = 0.18f),
                Color(0xFFFF8C00).copy(alpha = 0.12f)
            )
        )
    ),
    GameType.HIGHER_LOWER to GameMeta(
        label = "Higher or Lower",
        emoji = "🃏",
        color = Color(0xFFa78bfa),
        sub = "Chain wins · Up to 64×",
        gradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFa78bfa).copy(alpha = 0.18f),
                Color(0xFF7c3aed).copy(alpha = 0.12f)
            )
        )
    ),
    GameType.PLINKO to GameMeta(
        label = "Plinko",
        emoji = "🎯",
        color = Color(0xFF00FF94),
        sub = "Physics drop · 0.2×–2.0×",
        gradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF00FF94).copy(alpha = 0.15f),
                Color(0xFF00CC77).copy(alpha = 0.08f)
            )
        )
    )
)

@Composable
fun GameReelScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    
    var currentGameIndex by remember { mutableStateOf(0) }
    val games = remember {
        mutableStateListOf(
            GameType.COIN_FLIP,
            GameType.HIGHER_LOWER,
            GameType.PLINKO,
            GameType.COIN_FLIP,
            GameType.HIGHER_LOWER,
            GameType.PLINKO
        )
    }
    
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (dragOffset == 0f) 0f else dragOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "drag_offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Game content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffset.absoluteValue > 100) {
                                if (dragOffset < 0 && currentGameIndex < games.size - 1) {
                                    currentGameIndex++
                                    viewModel.vibrateMedium()
                                } else if (dragOffset > 0 && currentGameIndex > 0) {
                                    currentGameIndex--
                                    viewModel.vibrateMedium()
                                }
                            }
                            dragOffset = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {
            GameSlide(
                gameType = games[currentGameIndex],
                viewModel = viewModel,
                balance = gameState.balance
            )
        }
        
        // Top stats bar
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Balance chip
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💰", fontSize = 14.sp)
                    Text(
                        text = formatCurrency(gameState.balance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            
            // Streak chip
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Text(
                        text = gameState.streak.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "day${if (gameState.streak != 1) "s" else ""}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        // Scroll indicator dots
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            games.take(5).forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentGameIndex) 6.dp else 4.dp,
                            height = if (index == currentGameIndex) 20.dp else 4.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentGameIndex) Color.White
                            else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
fun GameSlide(
    gameType: GameType,
    viewModel: GameViewModel,
    balance: Int
) {
    val meta = GAME_METADATA[gameType]!!
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080810))
    ) {
        // Game header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(meta.gradient)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = meta.color.copy(alpha = 0.22f),
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = meta.emoji, fontSize = 20.sp)
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meta.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = meta.sub,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                        color = meta.color.copy(alpha = 0.22f),
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "LIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = meta.color
                        )
                    }
                }
            }
        }
        
        // Game content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            when (gameType) {
                GameType.COIN_FLIP -> CoinFlipGame(viewModel = viewModel, balance = balance)
                GameType.HIGHER_LOWER -> HigherLowerGame(viewModel = viewModel, balance = balance)
                GameType.PLINKO -> PlinkoGame(viewModel = viewModel, balance = balance)
            }
        }
    }
}
