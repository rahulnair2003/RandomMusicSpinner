package com.example.groupfinalproject

import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import java.util.Random

class SpinnerActivity : AppCompatActivity() {

    private lateinit var spinButton: Button
    private lateinit var wheelImg: ImageView
    private lateinit var timer: CountDownTimer
    private lateinit var revealTv: TextView
    private lateinit var progressBar: ProgressBar
    private var randomGenre: String = ""
    private var randomYear: String = ""
    private var randomArtist: String = ""
    private lateinit var speechRecognizer: SpeechRecognizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spinner)

        var count: Int = 0

        var genres = arrayOf("rap", "pop", "country", "r&b", "classical", "rock")
        var years = arrayOf("2010", "2012", "2014", "2016", "2018", "2020")

        spinButton = findViewById(R.id.spinButton)
        wheelImg = findViewById(R.id.wheelImg)
        revealTv = findViewById(R.id.revealTextView)
        revealTv.text = ""
        progressBar = findViewById(R.id.progressBar)
        //speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        //speechRecognizer.setRecognitionListener(this)

        val random = Random()

        spinButton.setOnClickListener {
            spinButton.isEnabled = false
            count++
            progressBar.progress = count

            var spin = (random.nextInt(6) + 1) * 60

            timer = object : CountDownTimer(spin * 60L, 1) {
                override fun onTick(l: Long) {
                    var rotation = wheelImg.rotation + 10
                    wheelImg.rotation = rotation
                }

                override fun onFinish() {
                    spinButton.isEnabled = true
                    if (count == 1) {
                        randomGenre = genres[random.nextInt(genres.size)]
                        revealTv.text = "Genre: " + randomGenre
                    }
                    else if (count == 2) {
                        randomYear = years[random.nextInt(years.size)]
                        revealTv.text = "Year: " + randomYear
                    }

                    if (count >= 3) {
                        spinButton.isEnabled = false
                        //make array of top artists here
                        //randomArtist = artists[random.nextInt(artists.size)]
                        //set artist here
                        //go to next view with song reveal here
                    }
                }
            }.start()
        }
    }
/*
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
        TODO("Not yet implemented")
    }

    override fun onResults(p0: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
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
    }*/
}