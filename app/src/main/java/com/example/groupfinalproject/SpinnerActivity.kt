package com.example.groupfinalproject

/*CMSC436 Group: Riya Boyapati, Rahul Nair, Srilasya Poruri*/

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random



class SpinnerActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var spinButton: Button
    private lateinit var wheelImg: ImageView
    private lateinit var timer: CountDownTimer
    private lateinit var revealTv: TextView
    private lateinit var prevTv: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var yearToSongArtistsMap :  Map<String, List<Triple<String, String, String>>>
    private var randomGenre: String = ""
    private var randomYear: String = ""
    private var randomArtist: String = ""
    private lateinit var songs: Set<String>
    private lateinit var artists: Set<String>
    private lateinit var years: Array<String>
    var playlistId : String = "64U4ZGXiJ8A5fOaq8HUtiH"
    val model = MainActivity.model
    var firebase = MainActivity.firebase
    var reference = MainActivity.reference
    lateinit var username :String
    lateinit var prevGenre :String
    lateinit var prevYear :String
    lateinit var prevArtist :String
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private val permission = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        var count: Int = 0
        var genres = arrayOf("rap", "pop", "country", "r&b", "indie", "rock")
        username = MainActivity.username
        prevGenre = MainActivity.prevGenre
        prevYear = MainActivity.prevYear
        prevArtist = MainActivity.prevArtist



        spinButton = findViewById(R.id.spinButton)
        wheelImg = findViewById(R.id.wheelImg)
        revealTv = findViewById(R.id.revealTextView)
        revealTv.text = ""
        prevTv = findViewById(R.id.prevDataNotif)
        prevTv.text = ""
        progressBar = findViewById(R.id.progressBar)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")

        ActivityCompat.requestPermissions(this@SpinnerActivity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            permission)

        val random = Random()
        var spin = (random.nextInt(6) + 1) * 60


        spinButton.setOnClickListener {
            speechRecognizer.startListening(recognizerIntent)
            Log.w("SPEECHRECOGNIZER", "start listening")

            spinButton.isEnabled = false
            count++
            progressBar.progress = count


            CoroutineScope(Dispatchers.Main).launch {
                    yearToSongArtistsMap = model.generateArtists(playlistId, randomGenre, randomYear)!!
                    years = arrayOf("1970", "1980", "1990", "2000", "2010", "2020")
                    timer = object : CountDownTimer(spin * 20L, 1) {
                        override fun onTick(l: Long) {
                            var rotation = wheelImg.rotation + 10
                            wheelImg.rotation = rotation
                        }

                        override fun onFinish() {
                            spinButton.isEnabled = true
                            prevGenre = MainActivity.prevGenre
                            prevYear = MainActivity.prevYear
                            if (count == 1) {
                                randomGenre = genres[random.nextInt(genres.size)]
                                revealTv.text = "Genre:  $randomGenre"
                                Log.w("prevgenre", MainActivity.prevGenre)
                                if (prevGenre != "") {
                                    Log.w("NOW", "HIT")
                                    prevTv.text = "Last time, $username spun $prevGenre"
                                }
                                else
                                    prevTv.text = ""
                                Log.w("TESTING", playlistId)
                                speechRecognizer.stopListening()
                            }
                            else if (count == 2) {
                                randomYear = years[random.nextInt(years.size)]
                                revealTv.text = "Decade: ${randomYear}s"
                                Log.w("prevyear", prevYear)
                                if (prevYear != "")
                                    prevTv.text = "Last time, $username spun ${prevYear}s"
                                else
                                    prevTv.text = ""
                                playlistId = model.playlistIds[randomGenre]!![randomYear]!!
                                Log.w("PLAYLISTID", playlistId)
                                Log.w("PLAYLISTID", "${model.playlistIds}")
                                Log.w("PLAYLISTID", "$randomYear $randomGenre")
                                speechRecognizer.stopListening()
                            }

                            if (count >= 3) {
                                spinButton.isEnabled = false
                                //make array of top artists here @ Rahul
                                //randomArtist = artists[random.nextInt(artists.size)]
                                //set artist here
                                //go to next view with song reveal here
                                val spinData = mapOf("genre" to randomGenre, "year" to randomYear, "artist" to randomArtist)
                                reference.child(username).updateChildren(spinData)
                                Log.w("NOW", "$spinData")
                                //prevTv.text = "Last time, you spun $prevArtist"
                                speechRecognizer.stopListening()
                                SongActivity.spinnerFinish = true
                                val intent = Intent(this@SpinnerActivity, SongActivity::class.java)
                                startActivity(intent)
                                if (SongActivity.spinnerFinish)
                                    finish()
                            }
                        }
                    }.start()

                    songs = model.getSongs(yearToSongArtistsMap, randomYear)
                    artists = model.getArtists(yearToSongArtistsMap, randomYear)
                    songArtistTriple = model.getRandomSongArtistPair(yearToSongArtistsMap, randomYear)
                    Log.d("Artists", "$artists")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.w("SPEECHRECOGNIZER", "in on request permissions result")
        when (requestCode) {
            permission -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED) {
                speechRecognizer.startListening(recognizerIntent)
            } else {
                Toast.makeText(this@SpinnerActivity, "Permission Denied",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.w("SPEECHRECOGNIZER", "stop")
    }

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val errorMessage: String = getErrorText(error)
        Log.w("SPEECHRECOGNIZER ERROR", "FAILED $errorMessage")
    }
    private fun getErrorText(error: Int): String {
        var message = ""
        message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        return message
    }

    override fun onResults(results: Bundle?) {
        Log.w("SPEECHRECOGNIZER", "onResults")
        val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var stopDetected = false
        for (result in matches!!) {
            if (result.equals("STOP", ignoreCase = true)) {
                Log.w("SPEECHRECOGNIZER", "MATCHED!!!")
                stopDetected = true
                break
            }
        }
        if (stopDetected) {
            Log.w("SPEECHRECOGNIZER", "stop detected")
            timer.cancel()
            spinButton.isEnabled = true
            speechRecognizer.stopListening()
        }
    }

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}


    companion object {
        var songArtistTriple : Triple<String, String, String>? = null
    }
}
