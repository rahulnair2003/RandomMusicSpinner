package com.example.groupfinalproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SpeechRecognition(private val context: Context) {

    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening: Boolean = false // Track whether recognition is ongoing
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    init {
        checkPermissions()
        setupSpeechRecognizer()
    }


    // Other properties and methods...

    fun startListening() {
        if (!isListening) {
            isListening = true
            val listenIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            listenIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")
            speechRecognizer.startListening(listenIntent)
        }
    }

    fun stopListening() {
        if (isListening) {
            isListening = false
            speechRecognizer.stopListening()
        }
    }


    private fun checkPermissions(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context as MainActivity,
                    permissions,
                    RECORD_AUDIO_PERMISSION_CODE
                )
                return false
            }
        }
        return true
    }


    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val speechListener = SpeechHandler()
        speechRecognizer.setRecognitionListener(speechListener)
    }

    inner class SpeechHandler : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            Log.e("SpeechRecognition", "Error: $error")
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> {
                    // Handle ERROR_NO_MATCH differently or ignore it
                    // For example, you can log a message or take no action
                    Log.e("BOO", "No match found")
                    isListening = false
                    startListening()
                // onally, you can restart listening or perform other actions here
                }
                else -> {
                }
            }
        }


        override fun onResults(results: Bundle?) {
            results?.let {
                val words: ArrayList<String>? =
                    it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores: FloatArray? =
                    it.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                // Process words and scores
                words?.get(0)?.let { recognizedWord ->
                    Log.d("TRUE", "Recognized word: $recognizedWord")
                    if (recognizedWord.equals("stop", ignoreCase = true)) {
                        // If the recognized word is "hello", stop listening
                        stopListening()
                    } else {
                        // Restart listening after results are processed if needed
                    }
                }
            }
        }


        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    companion object {
        const val RECORD_AUDIO_PERMISSION_CODE = 101
    }
}
