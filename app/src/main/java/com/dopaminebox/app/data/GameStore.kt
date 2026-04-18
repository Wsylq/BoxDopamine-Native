package com.dopaminebox.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dopamine_box_v2")

data class GameState(
    val balance: Int = 1000,
    val streak: Int = 1,
    val lastPlayed: String = "",
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val biggestWin: Int = 0
)

class GameStore(private val context: Context) {
    
    companion object {
        private val BALANCE_KEY = intPreferencesKey("balance")
        private val STREAK_KEY = intPreferencesKey("streak")
        private val LAST_PLAYED_KEY = stringPreferencesKey("last_played")
        private val TOTAL_WINS_KEY = intPreferencesKey("total_wins")
        private val TOTAL_LOSSES_KEY = intPreferencesKey("total_losses")
        private val BIGGEST_WIN_KEY = intPreferencesKey("biggest_win")
    }
    
    val gameStateFlow: Flow<GameState> = context.dataStore.data.map { prefs ->
        val today = getTodayString()
        val lastPlayed = prefs[LAST_PLAYED_KEY] ?: today
        val yesterday = getYesterdayString()
        
        val streak = when {
            lastPlayed == today -> prefs[STREAK_KEY] ?: 1
            lastPlayed == yesterday -> (prefs[STREAK_KEY] ?: 0) + 1
            else -> 1
        }
        
        GameState(
            balance = prefs[BALANCE_KEY] ?: 1000,
            streak = streak,
            lastPlayed = lastPlayed,
            totalWins = prefs[TOTAL_WINS_KEY] ?: 0,
            totalLosses = prefs[TOTAL_LOSSES_KEY] ?: 0,
            biggestWin = prefs[BIGGEST_WIN_KEY] ?: 0
        )
    }
    
    suspend fun addBalance(amount: Int) {
        context.dataStore.edit { prefs ->
            val currentBalance = prefs[BALANCE_KEY] ?: 1000
            val newBalance = maxOf(0, currentBalance + amount)
            prefs[BALANCE_KEY] = newBalance
            
            if (amount > 0) {
                val currentBiggest = prefs[BIGGEST_WIN_KEY] ?: 0
                if (amount > currentBiggest) {
                    prefs[BIGGEST_WIN_KEY] = amount
                }
                prefs[TOTAL_WINS_KEY] = (prefs[TOTAL_WINS_KEY] ?: 0) + 1
            } else if (amount < 0) {
                prefs[TOTAL_LOSSES_KEY] = (prefs[TOTAL_LOSSES_KEY] ?: 0) + 1
            }
            
            prefs[LAST_PLAYED_KEY] = getTodayString()
        }
    }
    
    suspend fun resetBalance() {
        context.dataStore.edit { prefs ->
            prefs[BALANCE_KEY] = 1000
        }
    }
    
    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
    
    private fun getYesterdayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }
}

fun formatCurrency(amount: Int): String {
    return when {
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format("%.1fK", amount / 1_000.0)
        else -> amount.toString()
    }
}
