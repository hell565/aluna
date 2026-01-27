package com.kl.aluna.voice

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.player.Track
import kotlinx.coroutines.*
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.RecognitionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * VoiceAssistant - handles voice commands for Aluna using Vosk engine.
 */
class VoiceAssistant(private val context: Context) : RecognitionListener {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var speechService: SpeechService? = null
    private var model: Model? = null
    private var isDucked = false
    private var resetDuckingJob: Job? = null

    // Wake word (на русском)
    private val WAKE_WORD = "алуна"

    interface VoiceAssistantListener {
        fun onResult(text: String)
        fun onError(error: Exception)
        fun onListeningStateChanged(isListening: Boolean)
    }

    private var listener: VoiceAssistantListener? = null

    init {
        initModel()
    }

    fun setListener(listener: VoiceAssistantListener) {
        this.listener = listener
    }

    private fun initModel() {
        scope.launch {
            try {
                val modelDirName = "vosk-model-small-ru"  // ← измени, если другое имя папки в assets
                val targetDir = File(context.filesDir, modelDirName)

                // Копируем модель из assets только если её нет или она пустая
                if (!targetDir.exists() || targetDir.listFiles()?.isEmpty() == true) {
                    Log.d("VoiceAssistant", "Copying model from assets to ${targetDir.absolutePath}")
                    copyAssetFolder(modelDirName, targetDir)
                }

                model = Model(targetDir.absolutePath)
                Log.d("VoiceAssistant", "Model loaded successfully from: ${targetDir.absolutePath}")
            } catch (e: IOException) {
                Log.e("VoiceAssistant", "Failed to load Vosk model", e)
                listener?.onError(e)
            }
        }
    }

    /**
     * Рекурсивно копирует папку из assets в файловую систему
     */
private fun copyAssetFolder(assetPath: String, targetDir: File) {
    val assetManager: AssetManager = context.assets
    targetDir.mkdirs()

    val files = assetManager.list(assetPath) ?: return  // ← если null → выходим, это не папка или ошибка

    for (fileName in files) {
        val fullAssetPath = if (assetPath.isEmpty()) fileName else "$assetPath/$fileName"
        val outFile = File(targetDir, fileName)

        val subFiles = assetManager.list(fullAssetPath)
        if (subFiles != null && subFiles.isNotEmpty()) {
            // Это папка
            copyAssetFolder(fullAssetPath, outFile)
        } else {
            // Это файл — копируем
            try {
                assetManager.open(fullAssetPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("VoiceAssistant", "Copied: $fullAssetPath")
            } catch (e: IOException) {
                Log.e("VoiceAssistant", "Failed to copy $fullAssetPath", e)
            }
        }
    }
}

    fun startListening() {
        val currentModel = model
        if (currentModel == null) {
            Log.e("VoiceAssistant", "Model not loaded yet")
            listener?.onError(IOException("Model not initialized"))
            return
        }

        try {
            val recognizer = Recognizer(currentModel, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(this)
            listener?.onListeningStateChanged(true)
            Log.d("VoiceAssistant", "Started listening")
        } catch (e: IOException) {
            Log.e("VoiceAssistant", "Failed to start listening", e)
            listener?.onError(e)
        }
    }

    fun stopListening() {
        speechService?.let {
            it.stop()
            speechService = null
            listener?.onListeningStateChanged(false)
            unduck()
            Log.d("VoiceAssistant", "Stopped listening")
        }
    }

    override fun onResult(hypothesis: String) {
        Log.d("VoiceAssistant", "Result: $hypothesis")
        val text = extractTextFromJson(hypothesis)
        if (text.isNotEmpty()) {
            processTranscription(text)
            listener?.onResult(text)
        }
    }

    override fun onFinalResult(hypothesis: String) {
        Log.d("VoiceAssistant", "Final Result: $hypothesis")
        val text = extractTextFromJson(hypothesis)
        if (text.isNotEmpty()) {
            processTranscription(text)
            listener?.onResult(text)
        }
    }

    override fun onPartialResult(hypothesis: String) {
        // Можно добавить partial для UI, если хочешь live-показ текста
    }

    override fun onError(exception: Exception) {
        Log.e("VoiceAssistant", "Error: ", exception)
        listener?.onError(exception)
    }

    override fun onTimeout() {
        stopListening()
    }

    private fun extractTextFromJson(json: String): String {
        return try {
            val prefix = "\"text\" : \""
            if (json.contains(prefix)) {
                json.substringAfter(prefix).substringBefore("\"")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    // Остальной код без изменений (processTranscription, handleWakeWord, duck/unduck, executeCommand и т.д.)
    // ------------------------------------------------------------------------------------------

    private fun processTranscription(text: String) {
        val input = text.lowercase().trim()
        Log.d("VoiceAssistant", "Processing: $input")

        if (input.contains(WAKE_WORD)) {
            handleWakeWord(input)
            return
        }

        if (isDucked) {
            val command = input.removePrefix(WAKE_WORD).trim()
            if (command.isNotEmpty()) {
                executeCommand(command)
                unduck()
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
            resetDuckingJob?.cancel()
            resetDuckingJob = scope.launch {
                delay(5000)
                if (isDucked) unduck()
            }
        }
    }

    private fun duck() {
        if (!isDucked) {
            isDucked = true
            MusicPlayer.exoPlayer.volume = 0.2f
        }
    }

    private fun unduck() {
        isDucked = false
        MusicPlayer.exoPlayer.volume = 1.0f
        resetDuckingJob?.cancel()
    }

    private fun executeCommand(command: String) {
        Log.d("VoiceAssistant", "Executing command: $command")

        when {
            fuzzyMatch(command, listOf("пауза", "стоп", "хватит", "прекрати", "pause", "stop")) -> {
                MusicPlayer.exoPlayer.pause()
            }
            fuzzyMatch(command, listOf("плей", "играй", "музыку", "продолжи", "play", "resume")) -> {
                MusicPlayer.exoPlayer.play()
            }
            fuzzyMatch(command, listOf("следующий", "вперед", "дальше", "некст", "next", "forward")) -> {
                MusicPlayer.next()
            }
            fuzzyMatch(command, listOf("предыдущий", "назад", "верни", "previous", "back")) -> {
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