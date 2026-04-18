package com.dopaminebox.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.dopaminebox.app.data.GameState
import com.dopaminebox.app.data.formatCurrency

@Composable
fun StatsScreen(
    gameState: GameState,
    onResetBalance: () -> Unit
) {
    val GOAL = 10_000_000
    val goalPct = minOf(100f, (gameState.balance.toFloat() / GOAL) * 100f)
    val winRate = if (gameState.totalWins + gameState.totalLosses > 0) {
        ((gameState.totalWins.toFloat() / (gameState.totalWins + gameState.totalLosses)) * 100).toInt()
    } else {
        0
    }
    
    var showResetDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(top = 72.dp, bottom = 100.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Your Stats",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Track your dopamine journey",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Goal Progress Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFFFD700).copy(alpha = 0.12f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🏆", fontSize = 24.sp)
                    Column {
                        Text(
                            text = "$10 Million Goal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${String.format("%.3f", goalPct)}% there",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = goalPct / 100f,
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        label = "progress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                )
                            )
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatCurrency(gameState.balance),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "$10M",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Performance Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White.copy(alpha = 0.04f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "PERFORMANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                StatRow("Balance", formatCurrency(gameState.balance), Color(0xFFFFD700))
                StatRow("Daily Streak", "🔥 ${gameState.streak} day${if (gameState.streak != 1) "s" else ""}", Color(0xFFFF6B6B))
                StatRow("Total Wins", gameState.totalWins.toString(), Color(0xFF22c55e))
                StatRow("Total Losses", gameState.totalLosses.toString(), Color(0xFFef4444))
                StatRow("Win Rate", "$winRate%", Color.White)
                StatRow("Biggest Win", formatCurrency(gameState.biggestWin), Color(0xFFa78bfa), isLast = true)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Educational Note
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White.copy(alpha = 0.03f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "⚠️ EDUCATIONAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This is a satirical/educational project demonstrating psychological techniques: variable ratio reinforcement, loss aversion, sunk cost bias, and fast dopamine feedback loops.\n\nInspired by Jaxon Poulton's YouTube video \"I Built the World's Most Addictive App\".",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 18.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reset Button
        Button(
            onClick = { showResetDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFef4444).copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🔄 Reset Balance to $1,000",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFef4444)
            )
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Balance?") },
            text = { Text("This will reset your balance to $1,000. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetBalance()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = Color(0xFFef4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    accentColor: Color,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        if (!isLast) {
            Divider(
                color = Color.White.copy(alpha = 0.05f),
                thickness = 1.dp
            )
        }
    }
}
