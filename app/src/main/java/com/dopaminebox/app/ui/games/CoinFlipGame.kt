package com.dopaminebox.app.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopaminebox.app.data.formatCurrency
import com.dopaminebox.app.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.min

enum class CoinSide { HEADS, TAILS }
enum class CoinPhase { IDLE, FLIPPING, RESULT }

val BETS = listOf(10, 25, 50, 100, 250, 500, 1000, 5000)

@Composable
fun CoinFlipGame(viewModel: GameViewModel, balance: Int) {
    var bet by remember { mutableStateOf(50) }
    var choice by remember { mutableStateOf(CoinSide.HEADS) }
    var phase by remember { mutableStateOf(CoinPhase.IDLE) }
    var result by remember { mutableStateOf<CoinSide?>(null) }
    var won by remember { mutableStateOf<Boolean?>(null) }
    var winAmount by remember { mutableStateOf(0) }
    
    val safeBet = min(bet, balance)
    
    LaunchedEffect(phase) {
        if (phase == CoinPhase.FLIPPING) {
            delay(1200)
            val outcome = if (Math.random() < 0.5) CoinSide.HEADS else CoinSide.TAILS
            val didWin = outcome == choice
            result = outcome
            won = didWin
            phase = CoinPhase.RESULT
            
            if (didWin) {
                viewModel.addBalance(safeBet)
                viewModel.vibrateWin()
                winAmount = safeBet
            } else {
                viewModel.addBalance(-safeBet)
                viewModel.vibrateLose()
                winAmount = -safeBet
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Coin
        Box(
            modifier = Modifier.height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (phase == CoinPhase.FLIPPING) 1800f else 0f,
                animationSpec = tween(1200, easing = LinearEasing),
                label = "coin_rotation"
            )
            
            val scale by animateFloatAsState(
                targetValue = when (phase) {
                    CoinPhase.RESULT -> 1.1f
                    else -> 1f
                },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "coin_scale"
            )
            
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(
                        if (result == CoinSide.TAILS) {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFC0C0C0), Color(0xFF808080))
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                        }
                    )
                    .border(4.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        phase == CoinPhase.FLIPPING -> "🪙"
                        result == CoinSide.TAILS -> "🦅"
                        else -> "👑"
                    },
                    fontSize = 60.sp
                )
            }
            
            // Win/Loss indicator
            AnimatedVisibility(
                visible = phase == CoinPhase.RESULT && won != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut()
            ) {
                Text(
                    text = if (won == true) "+${formatCurrency(winAmount)}" else formatCurrency(winAmount),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (won == true) Color(0xFF22c55e) else Color(0xFFef4444),
                    modifier = Modifier.offset(y = (-80).dp)
                )
            }
        }
        
        // Result text
        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "result_text"
        ) { currentPhase ->
            when (currentPhase) {
                CoinPhase.RESULT -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (won == true) "🎉 YOU WIN!" else "💀 YOU LOSE",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (won == true) Color(0xFF22c55e) else Color(0xFFef4444)
                        )
                        Text(
                            text = if (result == CoinSide.HEADS) "👑 Heads" else "🦅 Tails",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                CoinPhase.FLIPPING -> {
                    Text(
                        text = "Flipping...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                CoinPhase.IDLE -> {
                    Text(
                        text = "Pick a side and flip!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        // Choice buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(CoinSide.HEADS to "👑 Heads", CoinSide.TAILS to "🦅 Tails").forEach { (side, label) ->
                Button(
                    onClick = {
                        choice = side
                        viewModel.vibrateLight()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (choice == side) {
                            if (side == CoinSide.HEADS) Color(0xFFFFD700) else Color(0xFFC0C0C0)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (choice == side) Color.Black else Color.White
                    )
                }
            }
        }
        
        // Bet selector
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BET AMOUNT",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                BETS.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        row.forEach { b ->
                            FilterChip(
                                selected = bet == b,
                                onClick = {
                                    bet = b
                                    viewModel.vibrateLight()
                                },
                                label = {
                                    Text(
                                        text = formatCurrency(b),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFFFD700),
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action button
        if (phase == CoinPhase.RESULT) {
            Button(
                onClick = {
                    phase = CoinPhase.IDLE
                    result = null
                    won = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Play Again",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = {
                    if (phase == CoinPhase.IDLE && balance > 0) {
                        phase = CoinPhase.FLIPPING
                        viewModel.vibrateMedium()
                    }
                },
                enabled = phase == CoinPhase.IDLE && balance > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (phase == CoinPhase.IDLE && balance > 0) {
                        Color(0xFFFFD700)
                    } else {
                        Color.White.copy(alpha = 0.05f)
                    },
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (phase == CoinPhase.FLIPPING) "Flipping..." else "Flip for ${formatCurrency(safeBet)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (phase == CoinPhase.IDLE && balance > 0) Color.Black else Color.White.copy(alpha = 0.3f)
                )
            }
        }
        
        if (balance <= 0) {
            Text(
                text = "💀 Broke! Game over.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFef4444)
            )
        }
    }
}
