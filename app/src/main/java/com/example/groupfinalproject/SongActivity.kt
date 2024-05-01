package com.example.groupfinalproject

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SongActivity : AppCompatActivity() {
    lateinit var songText : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        songText = findViewById<TextView>(R.id.songTextView)
        // Assuming songText is a TextView defined in your activity
        songText.text = SpinnerActivity.songArtistPair!!.first
    }
}