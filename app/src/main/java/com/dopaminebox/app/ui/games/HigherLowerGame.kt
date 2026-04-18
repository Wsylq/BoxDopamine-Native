package com.dopaminebox.app.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dopaminebox.app.data.formatCurrency
import com.dopaminebox.app.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.random.Random

data class Card(val suit: String, val value: String, val num: Int)

val SUITS = listOf("♠", "♥", "♦", "♣")
val VALUES = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")
val HL_BETS = listOf(10, 25, 50, 100, 250, 500)
val MULTIPLIERS = listOf(2, 4, 8, 16, 32, 64)

fun randomCard() = Card(
    suit = SUITS.random(),
    value = VALUES.random(),
    num = Random.nextInt(13)
)

enum class HLPhase { IDLE, PLAYING, CASHED, LOST }

@Composable
fun HigherLowerGame(viewModel: GameViewModel, balance: Int) {
    var bet by remember { mutableStateOf(50) }
    var phase by remember { mutableStateOf(HLPhase.IDLE) }
    var currentCard by remember { mutableStateOf(randomCard()) }
    var nextCard by remember { mutableStateOf<Card?>(null) }
    var streak by remember { mutableStateOf(0) }
    var pot by remember { mutableStateOf(0) }
    var showNext by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val currentPot = if (streak == 0) min(bet, balance) else pot
    
    fun startGame() {
        val realBet = min(bet, balance)
        viewModel.addBalance(-realBet)
        pot = realBet
        streak = 0
        currentCard = randomCard()
        nextCard = null
        showNext = false
        phase = HLPhase.PLAYING
        viewModel.vibrateMedium()
    }
    
    fun guess(dir: String) {
        if (phase != HLPhase.PLAYING) return
        val next = randomCard()
        nextCard = next
        showNext = true
        viewModel.vibrateMedium()
        
        scope.launch {
            delay(800)
            val correct = if (dir == "higher") next.num >= currentCard.num else next.num <= currentCard.num
            
            if (correct) {
                val newStreak = streak + 1
                val newPot = currentPot * 2
                streak = newStreak
                pot = newPot
                currentCard = next
                nextCard = null
                showNext = false
                viewModel.vibrateWin()
            } else {
                phase = HLPhase.LOST
                viewModel.vibrateLose()
            }
        }
    }
    
    fun cashOut() {
        if (phase != HLPhase.PLAYING || streak == 0) return
        viewModel.addBalance(currentPot)
        phase = HLPhase.CASHED
        viewModel.vibrateWin()
    }
    
    fun reset() {
        phase = HLPhase.IDLE
        currentCard = randomCard()
        nextCard = null
        showNext = false
        streak = 0
        pot = 0
    }
    
    val isRed = { card: Card -> card.suit == "♥" || card.suit == "♦" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Multiplier bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            MULTIPLIERS.forEachIndexed { i, m ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                i < streak -> Brush.linearGradient(
                                    colors = listOf(Color(0xFF22c55e), Color(0xFF16a34a))
                                )
                                i == streak && phase == HLPhase.PLAYING -> Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                )
                                else -> Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${m}x",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (i <= streak) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
        
        // Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current card
            CardView(card = currentCard, isRed = isRed(currentCard))
            
            Text(text = "→", fontSize = 28.sp, color = Color.White.copy(alpha = 0.3f))
            
            // Next card
            if (showNext && nextCard != null) {
                CardView(card = nextCard!!, isRed = isRed(nextCard!!))
            } else {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(155.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🂠", fontSize = 36.sp)
                }
            }
        }
        
        // Pot display
        if (phase == HLPhase.PLAYING || phase == HLPhase.CASHED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CURRENT POT",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatCurrency(currentPot),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                if (streak > 0) {
                    Text(
                        text = "${MULTIPLIERS[min(streak - 1, 5)]}x multiplier!",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        // Result messages
        AnimatedVisibility(visible = phase == HLPhase.CASHED) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "💰 CASHED OUT!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF22c55e)
                )
                Text(
                    text = "${formatCurrency(currentPot)} secured",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        AnimatedVisibility(visible = phase == HLPhase.LOST) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "💀 WRONG!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFef4444)
                )
                Text(
                    text = "Lost everything",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bet selector (idle only)
        if (phase == HLPhase.IDLE) {
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
                    HL_BETS.forEach { b ->
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
                                selectedContainerColor = Color(0xFFa78bfa).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFa78bfa),
                                containerColor = Color.White.copy(alpha = 0.06f),
                                labelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
        
        // Action buttons
        when (phase) {
            HLPhase.IDLE -> {
                Button(
                    onClick = { startGame() },
                    enabled = balance > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (balance > 0) Color(0xFFa78bfa) else Color.White.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Start — ${formatCurrency(min(bet, balance))}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (balance > 0) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            HLPhase.PLAYING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { guess("higher") },
                            enabled = !showNext,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!showNext) Color(0xFF22c55e) else Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "↑ Higher",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (!showNext) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                        Button(
                            onClick = { guess("lower") },
                            enabled = !showNext,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!showNext) Color(0xFFef4444) else Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "↓ Lower",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (!showNext) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                    if (streak > 0) {
                        Button(
                            onClick = { cashOut() },
                            enabled = !showNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "💰 Cash Out ${formatCurrency(currentPot)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
            HLPhase.CASHED, HLPhase.LOST -> {
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
            }
        }
    }
}

@Composable
fun CardView(card: Card, isRed: Boolean) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(155.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e))
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.value,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = if (isRed) Color(0xFFef4444) else Color.White
            )
            Text(
                text = card.suit,
                fontSize = 24.sp,
                color = if (isRed) Color(0xFFef4444) else Color.White
            )
        }
    }
}
