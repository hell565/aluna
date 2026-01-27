package com.kl.aluna.voice

import android.content.Context
import android.util.Log
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.player.Track
import kotlinx.coroutines.*

/**
 * VoiceAssistant - handles voice commands for Aluna.
 * Implements fuzzy matching, wake word triggers, and audio ducking logic.
 */
class VoiceAssistant(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isDucked = false
    private var resetDuckingJob: Job? = null

    // Wake word
    private val WAKE_WORD = "алуна"

    fun processTranscription(text: String) {
        val input = text.lowercase().trim()
        Log.d("VoiceAssistant", "Transcription: $input")

        // 1. Check for wake word
        if (input.contains(WAKE_WORD)) {
            handleWakeWord(input)
            return
        }

        // 2. If already ducked (waiting for command after wake word)
        if (isDucked) {
            val command = input.removePrefix(WAKE_WORD).trim()
            if (command.isNotEmpty()) {
                executeCommand(command)
                unduck() // Unduck after command execution
            }
        }
    }

    private fun handleWakeWord(input: String) {
        duck()
        
        val commandAfterWake = input.substringAfter(WAKE_WORD).trim()
        if (commandAfterWake.isNotEmpty()) {
            executeCommand(commandAfterWake)
            unduck()
        } else {
            // Just said "Aluna", wait for command
            resetDuckingJob?.cancel()
            resetDuckingJob = scope.launch {
                delay(5000) // Wait 5 seconds for command
                if (isDucked) unduck()
            }
        }
    }

    private fun duck() {
        if (!isDucked) {
            isDucked = true
            MusicPlayer.exoPlayer.volume = 0.2f // Lower volume
        }
    }

    private fun unduck() {
        isDucked = false
        MusicPlayer.exoPlayer.volume = 1.0f // Restore volume
        resetDuckingJob?.cancel()
    }

    private fun executeCommand(command: String) {
        Log.d("VoiceAssistant", "Executing command: $command")

        when {
            fuzzyMatch(command, listOf("пауза", "стоп", "хватит", "прекрати")) -> {
                MusicPlayer.exoPlayer.pause()
            }
            fuzzyMatch(command, listOf("плей", "играй", "музыку", "продолжи")) -> {
                MusicPlayer.exoPlayer.play()
            }
            fuzzyMatch(command, listOf("следующий", "вперед", "дальше", "некст")) -> {
                MusicPlayer.next()
            }
            fuzzyMatch(command, listOf("предыдущий", "назад", "верни")) -> {
                MusicPlayer.previous()
            }
            command.contains("включи") || command.contains("найди") || command.contains("поставь") -> {
                val trackName = extractName(command, listOf("включи", "найди", "поставь", "песню", "трек"))
                if (trackName != null) {
                    playTrackFuzzy(trackName)
                }
            }
            command.contains("фаворит") || command.contains("лайк") || command.contains("нравится") -> {
                MusicPlayer.currentTrack.value?.let { MusicPlayer.toggleFavorite(it) }
            }
        }
    }

    /**
     * Fuzzy matching using Levenshtein distance or simple contains
     */
    private fun fuzzyMatch(input: String, candidates: List<String>): Boolean {
        return candidates.any { candidate ->
            input.contains(candidate) || levenshteinDistance(input, candidate) <= 2
        }
    }

    private fun extractName(command: String, triggers: List<String>): String? {
        var result = command
        triggers.forEach { trigger ->
            if (result.contains(trigger)) {
                result = result.substringAfter(trigger).trim()
            }
        }
        return if (result.length > 1) result else null
    }

    private fun playTrackFuzzy(name: String) {
        val tracks = MusicPlayer.playlist
        var bestMatch: Track? = null
        var minDistance = Int.MAX_VALUE

        for (track in tracks) {
            val titleDist = levenshteinDistance(name, track.title.lowercase())
            val artistDist = levenshteinDistance(name, track.artist.lowercase())
            val currentMin = minOf(titleDist, artistDist)
            
            if (currentMin < minDistance) {
                minDistance = currentMin
                bestMatch = track
            }
        }

        // If match is decent (threshold depends on name length)
        if (bestMatch != null && minDistance <= (name.length / 2).coerceAtLeast(2)) {
            MusicPlayer.playTrack(bestMatch)
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }
}
