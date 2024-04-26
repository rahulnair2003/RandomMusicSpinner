package com.example.groupfinalproject

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import java.util.Random

class SpinnerActivity : AppCompatActivity(){

    private lateinit var spinButton: Button
    private lateinit var wheelImg: ImageView
    private lateinit var timer: CountDownTimer
    private lateinit var revealTv: TextView
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

        val random = Random()

        spinButton.setOnClickListener {
            spinButton.isEnabled = false
            count++

            // reading random value between 10 to 30
            var spin = random.nextInt(20) + 10
            spin *= 36

            // timer for each degree movement
            timer = object : CountDownTimer(spin * 20L, 1) {
                override fun onTick(l: Long) {
                    // rotate the wheel
                    var rotation = wheelImg.rotation + 2
                    wheelImg.rotation = rotation
                }

                override fun onFinish() {
                    spinButton.isEnabled = true
                    if (count == 1) {
                        val randomGenre = genres[Random().nextInt(genres.size)]
                        revealTv.text = "Genre: " + randomGenre
                    }
                    else if (count == 2) {
                        val randomYear = years[Random().nextInt(years.size)]
                        revealTv.text = "Year: " + randomYear
                    }


                }
            }.start()
        }
    }
}