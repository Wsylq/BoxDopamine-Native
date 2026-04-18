package com.dopaminebox.app.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopaminebox.app.data.formatCurrency
import com.dopaminebox.app.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random

val PLINKO_BETS = listOf(10, 25, 50, 100, 250, 500)
val MULTIPLIERS_PLINKO = listOf(0.2f, 0.5f, 0.8f, 1.0f, 1.5f, 2.0f, 1.5f, 1.0f, 0.8f, 0.5f, 0.2f)

enum class PlinkoPhase { IDLE, DROPPING, RESULT }

@Composable
fun PlinkoGame(viewModel: GameViewModel, balance: Int) {
    var bet by remember { mutableStateOf(50) }
    var phase by remember { mutableStateOf(PlinkoPhase.IDLE) }
    var ballPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var landedSlot by remember { mutableStateOf<Int?>(null) }
    var winAmount by remember { mutableStateOf(0) }
    
    val safeBet = min(bet, balance)
    
    LaunchedEffect(phase) {
        if (phase == PlinkoPhase.DROPPING) {
            // Simulate ball drop
            val rows = 8
            var currentX = 5 // Start in middle
            
            for (row in 0..rows) {
                delay(150)
                ballPosition = Offset(currentX.toFloat(), row.toFloat())
                // Random left or right
                currentX += if (Random.nextBoolean()) 1 else -1
                currentX = currentX.coerceIn(0, 10)
                viewModel.vibrateLight()
            }
            
            // Land in slot
            val slot = currentX.coerceIn(0, MULTIPLIERS_PLINKO.size - 1)
            landedSlot = slot
            phase = PlinkoPhase.RESULT
            
            val multiplier = MULTIPLIERS_PLINKO[slot]
            val payout = (safeBet * multiplier).toInt()
            val profit = payout - safeBet
            
            viewModel.addBalance(profit)
            winAmount = profit
            
            if (profit > 0) {
                viewModel.vibrateWin()
            } else {
                viewModel.vibrateLose()
            }
        }
    }
    
    fun drop() {
        if (phase == PlinkoPhase.IDLE && balance > 0) {
            phase = PlinkoPhase.DROPPING
            landedSlot = null
            viewModel.vibrateMedium()
        }
    }
    
    fun reset() {
        phase = PlinkoPhase.IDLE
        ballPosition = Offset(0f, 0f)
        landedSlot = null
        winAmount = 0
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Plinko board visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0a0a14)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pegRadius = 6f
                val rows = 8
                val cols = 11
                val spacingX = size.width / (cols + 1)
                val spacingY = size.height / (rows + 2)
                
                // Draw pegs
                for (row in 0..rows) {
                    for (col in 0..cols) {
                        val x = spacingX * (col + 1)
                        val y = spacingY * (row + 1)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = pegRadius,
                            center = Offset(x, y)
                        )
                    }
                }
                
                // Draw ball if dropping
                if (phase == PlinkoPhase.DROPPING) {
                    val ballX = spacingX * (ballPosition.x + 1)
                    val ballY = spacingY * (ballPosition.y + 1)
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 12f,
                        center = Offset(ballX, ballY)
                    )
                }
            }
        }
        
        // Multiplier slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MULTIPLIERS_PLINKO.forEachIndexed { index, mult ->
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                landedSlot == index -> Color(0xFFFFD700)
                                mult >= 1.5f -> Color(0xFF22c55e).copy(alpha = 0.3f)
                                mult >= 1.0f -> Color(0xFFa78bfa).copy(alpha = 0.3f)
                                else -> Color(0xFFef4444).copy(alpha = 0.3f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mult}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (landedSlot == index) Color.Black else Color.White
                    )
                }
            }
        }
        
        // Result
        AnimatedVisibility(visible = phase == PlinkoPhase.RESULT) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (winAmount >= 0) "🎉 WIN!" else "💀 LOSS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (winAmount >= 0) Color(0xFF22c55e) else Color(0xFFef4444)
                )
                Text(
                    text = if (winAmount >= 0) "+${formatCurrency(winAmount)}" else formatCurrency(winAmount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (winAmount >= 0) Color(0xFF22c55e) else Color(0xFFef4444)
                )
                landedSlot?.let {
                    Text(
                        text = "${MULTIPLIERS_PLINKO[it]}x multiplier",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        AnimatedVisibility(visible = phase == PlinkoPhase.DROPPING) {
            Text(
                text = "Dropping...",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        AnimatedVisibility(visible = phase == PlinkoPhase.IDLE) {
            Text(
                text = "Drop the ball!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bet selector
        if (phase == PlinkoPhase.IDLE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BET AMOUNT",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    PLINKO_BETS.forEach { b ->
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
                                selectedContainerColor = Color(0xFF00FF94).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00FF94),
                                containerColor = Color.White.copy(alpha = 0.06f),
                                labelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
        
        // Action button
        if (phase == PlinkoPhase.RESULT) {
            Button(
                onClick = { reset() },
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
                onClick = { drop() },
                enabled = phase == PlinkoPhase.IDLE && balance > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (phase == PlinkoPhase.IDLE && balance > 0) {
                        Color(0xFF00FF94)
                    } else {
                        Color.White.copy(alpha = 0.05f)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (phase == PlinkoPhase.DROPPING) "Dropping..." else "Drop for ${formatCurrency(safeBet)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (phase == PlinkoPhase.IDLE && balance > 0) Color.Black else Color.White.copy(alpha = 0.3f)
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
