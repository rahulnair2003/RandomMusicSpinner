package com.example.groupfinalproject

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
        val playlistIds = mapOf(
            "rap" to mapOf("1970" to "37i9dQZF1EIee1TiRnBd3C", "1980" to "37i9dQZF1DX2XmsXL2WBQd", "1990" to "37i9dQZF1DX186v583rmzp", "2000" to "37i9dQZF1DX1lHW2vbQwNN", "2010" to "37i9dQZF1DX97h7ftpNSYT", "2020" to "37i9dQZF1EIezQcATIWbSB"),
            "pop" to mapOf("1970" to "37i9dQZF1EIg0r197lDGql", "1980" to "49PAThhKRCCTXeydvq9uAp", "1990" to "37i9dQZF1DWVcJK7WY4M52", "2000" to "5asIusKloOLOILpjhwjgPH", "2010" to "3FeewjLi5LMzIpV4h35QEz", "2020" to "7bfyBCaVhnd8OywuUVKlhN"),
            "country" to mapOf("1970" to "37i9dQZF1DWYP5PUsVbso9", "1980" to "37i9dQZF1DX6RCydf9ytsj", "1990" to "37i9dQZF1DWVpjAJGB70vU", "2000" to "37i9dQZF1DXdxUH6sNtcDe", "2010" to "0wqUVPa19eClnNClEMQQoY", "2020" to "7vGNRrlvEtUX6hRdQvLq7U"),
            "r&b" to mapOf("1970" to "37i9dQZF1EIdpeTOIJBUe0", "1980" to "7oSFWAqfNN4UON82z8yst0", "1990" to "37i9dQZF1DX6VDO8a6cQME", "2000" to "37i9dQZF1DWYmmr74INQlb", "2010" to "37i9dQZF1DWXbttAJcbphz", "2020" to "37i9dQZF1EIhKysdf5HuRS"),
            "indie" to mapOf("1970" to "37i9dQZF1EIfEuk5mHRSID", "1980" to "37i9dQZF1EIevGiMQyNtSW", "1990" to "37i9dQZF1EIdAFUuQXTjDp", "2000" to "4irf7OeR9mM7KVxNTYoiXx", "2010" to "2HgmyUctw7UAi6fLlIMZJH", "2020" to "37i9dQZF1EIgo0ld2W1RyS"),
            "rock" to mapOf("1970" to "3za8xUPaO5ng9AC7rpbMNB", "1980" to "37i9dQZF1EIelF7Dvo3Edn", "1990" to "2HfFccisPxQfprhgIHM7XH", "2000" to "37i9dQZF1DX3oM43CtKnRV", "2010" to "37i9dQZF1DX99DRG9N39X3", "2020" to "37i9dQZF1EIfFB4LmpxPTW")
        )



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
                // Call the suspending function within the coroutine
                try {
                    generateArtists()
                    years = arrayOf("1970", "1980", "1990", "2000", "2010", "2020")
                    timer = object : CountDownTimer(spin * 20L, 1) {
                        override fun onTick(l: Long) {
                            var rotation = wheelImg.rotation + 10
                            wheelImg.rotation = rotation
                        }

                        override fun onFinish() {
                            spinButton.isEnabled = true
                            if (count == 1) {
                                randomGenre = genres[random.nextInt(genres.size)]
                                revealTv.text = "Genre:  $randomGenre"
                                Log.w("prevgenre", MainActivity.prevGenre)
                                if (prevGenre != "")
                                    prevTv.text = "Last time, $username spun $prevGenre"
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
                                playlistId = playlistIds[randomGenre]!![randomYear]!!
                                Log.w("PLAYLISTID", playlistId)
                                Log.w("PLAYLISTID", "$playlistIds")
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
                                //prevTv.text = "Last time, you spun $prevArtist"
                                speechRecognizer.stopListening()
                                SongActivity.spinnerFinish = true
                                val intent = Intent(this@SpinnerActivity, SongActivity::class.java)
                                startActivity(intent)
                                if (SongActivity.spinnerFinish == true)
                                    finish()
                            }
                        }
                    }.start()



                    songs = model.getSongs(yearToSongArtistsMap, randomYear)
                    artists = model.getArtists(yearToSongArtistsMap, randomYear)
                    songArtistTriple = model.getRandomSongArtistPair(yearToSongArtistsMap, randomYear)
                    Log.d("Artists", "$artists")

                } catch (e: Exception) {
                    // Handle exceptions here
                    Log.w("Exception occurred:", "${e.message}")
                }
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


    suspend fun generateArtists() {
        try {
            yearToSongArtistsMap = model.getPlaylistItems(MainActivity.token, playlistId)
            Log.d("Test", "Top Artists in $randomGenre Category from $randomYear: $yearToSongArtistsMap")
        } catch (e: Exception) {
            Log.e("Test", "Error: ${e.message}", e)
        }
    }

    companion object {
        var songArtistTriple : Triple<String, String, String>? = null
    }
}
