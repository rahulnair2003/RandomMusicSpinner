package com.example.groupfinalproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SongActivity : AppCompatActivity() {
    lateinit var restartButton: Button
    lateinit var songText : TextView
    lateinit var prevSongTV: TextView
    lateinit var ratingThanksTV : TextView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var shareButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)
        sharedPreferences = getSharedPreferences("prevSongs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        var prevSong = sharedPreferences.getString("songText", null)
        var prevSongNotif: String = ""
        if (prevSong == null) {
            prevSongNotif = "Hope you enjoy your first song!"
            Log.w("prevsong", "null")
        }
        else {
            Log.w("prevsong", prevSong)
            var rating: Float = sharedPreferences.getFloat("songRating", 0.0f)
            Log.w("prevrating", "$rating")
            if (rating == 0.0f)
                prevSongNotif = "The last song you spun was $prevSong and you did not rate it."
            else
                prevSongNotif = "The last song you spun was $prevSong and you rated it $rating stars."
        }

        shareButton = findViewById(R.id.shareButton)
        shareButton.setOnClickListener {
            sendEmail()
        }

        songText = findViewById<TextView>(R.id.songTextView)
        // Assuming songText is a TextView defined in your activity
        songText.text = SpinnerActivity.songArtistPair!!.first
        prevSongTV = findViewById<TextView>(R.id.prevSongTV)
        prevSongTV.text = prevSongNotif
        editor.putString("songText", SpinnerActivity.songArtistPair!!.first)
        editor.commit()
        editor.putFloat("songRating", 0.0f)
        editor.commit()
        val ratingBar = findViewById<RatingBar>(R.id.ratingbar)
        val listener = ratingListener()
        ratingBar.setOnRatingBarChangeListener(listener)
        restartButton = findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            spinnerfinish = true
            finish()
        }

    }
    inner class ratingListener: RatingBar.OnRatingBarChangeListener {
        override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
            if (fromUser) {
                editor.putFloat("songRating", rating)
                editor.commit()
                var ratingNotif: String = "Thanks for rating the song!"
                ratingThanksTV = findViewById<TextView>(R.id.ratingThanksTV)
                ratingThanksTV.text = ratingNotif
            }
        }
    }

    fun sendEmail() {
        val songTitle = SpinnerActivity.songArtistPair!!.first
        val rating = sharedPreferences.getFloat("songRating", 0.0f)

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("text/plain")
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Song Title: $songTitle by: ")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "I rated this song: $rating stars!")

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
    companion object {
        var spinnerfinish = false
    }
}