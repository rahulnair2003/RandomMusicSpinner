package com.example.groupfinalproject

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.Random



class SpinnerActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var spinButton: Button
    private lateinit var wheelImg: ImageView
    private lateinit var timer: CountDownTimer
    private lateinit var revealTv: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var yearToSongArtistsMap :  Map<Int, List<Pair<String, String>>>
    private var randomGenre: String = ""
    private var randomYear: String = ""
    private var randomArtist: String = ""
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var songs: Set<String>
    private lateinit var artists: Set<String>
    private lateinit var years: Array<String>
    val model = MainActivity.model
    val firebase = MainActivity.firebase
    val reference = MainActivity.reference
    val username = MainActivity.username
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        var count: Int = 0
        var genres = arrayOf("rap", "pop", "country", "r&b", "classical", "rock")

        spinButton = findViewById(R.id.spinButton)
        wheelImg = findViewById(R.id.wheelImg)
        revealTv = findViewById(R.id.revealTextView)
        revealTv.text = ""
        progressBar = findViewById(R.id.progressBar)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        val random = Random()
        var spin = (random.nextInt(6) + 1) * 60


        spinButton.setOnClickListener {
            spinButton.isEnabled = false
            count++
            progressBar.progress = count
            /*
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request the permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE)
            }*/
            speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))


            CoroutineScope(Dispatchers.Main).launch {
            // Call the suspending function within the coroutine
            try {
                generateArtists()
                years = model.getYears(yearToSongArtistsMap)

                timer = object : CountDownTimer(spin * 60L, 1) {
                    override fun onTick(l: Long) {
                        var rotation = wheelImg.rotation + 10
                        wheelImg.rotation = rotation
                    }

                    override fun onFinish() {
                        spinButton.isEnabled = true
                        if (count == 1) {
                            randomGenre = genres[random.nextInt(genres.size)]
                            revealTv.text = "Genre:  $randomGenre"
                            speechRecognizer.stopListening()
                        }
                        else if (count == 2) {
                            randomYear = years[random.nextInt(years.size)]
                            revealTv.text = "Year: $randomYear"
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

                            speechRecognizer.stopListening()
                            val intent = Intent(this@SpinnerActivity, SongActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }.start()



                songs = model.getSongs(yearToSongArtistsMap, randomYear.toInt())
                artists = model.getArtists(yearToSongArtistsMap, randomYear.toInt())
                songArtistPair = model.getRandomSongArtistPair(yearToSongArtistsMap, randomYear.toInt())
                Log.d("Artists", "$artists")

            } catch (e: Exception) {
                // Handle exceptions here
                Log.w("Exception occurred:", "${e.message}")
            }
        }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
    override fun onReadyForSpeech(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBeginningOfSpeech() {
        TODO("Not yet implemented")
    }

    override fun onRmsChanged(p0: Float) {
        TODO("Not yet implemented")
    }

    override fun onBufferReceived(p0: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onEndOfSpeech() {
        TODO("Not yet implemented")
    }

    override fun onError(p0: Int) {
        Log.w("error: ", p0.toString())
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.w("matches: ", matches.toString())
        matches?.let {
            for (result in matches) {
                if (result.equals("STOP", ignoreCase = true)) {
                    // Stop spinning animation when "STOP" is detected
                    timer.cancel()
                    spinButton.isEnabled = true
                }
            }
        }
    }

    override fun onPartialResults(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        TODO("Not yet implemented")
    }

    suspend fun generateArtists() {
        try {
            val popCategoryPlaylistId = "4K99Kk1PLl10SHd3XwI8QG"
            yearToSongArtistsMap = model.getPlaylistItems(MainActivity.token, popCategoryPlaylistId)
            Log.d("Test", "Top Artists in Pop Category: $yearToSongArtistsMap")
        } catch (e: Exception) {
            Log.e("Test", "Error: ${e.message}", e)
        }
    }

    companion object {
        var songArtistPair : Pair<String, String>? = null
    }
}