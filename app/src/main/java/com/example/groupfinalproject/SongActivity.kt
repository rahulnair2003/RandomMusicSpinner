package com.example.groupfinalproject

/*CMSC436 Group: Riya Boyapati, Rahul Nair, Srilasya Poruri*/

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SongActivity : AppCompatActivity() {
    lateinit var restartButton: Button
    lateinit var songText : TextView
    lateinit var lastTime : TextView
    lateinit var artistText : TextView
    lateinit var albumImage : ImageView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var shareButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        sharedPreferences = getSharedPreferences("prevSongs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        val prevSong = sharedPreferences.getString("songText", null)
        val rating = sharedPreferences.getFloat("songRating", 0.0f)

        val prevSongText = if (prevSong == null) {
            "Hope you enjoy your first song!"
        } else {
            if (rating == 0.0f)
                "The last song you spun was $prevSong and you did not rate it."
            else
                "The last song you spun was $prevSong and you rated it $rating stars."
        }


        shareButton = findViewById(R.id.shareButton)
        shareButton.setOnClickListener {
            sendEmail()
        }
        lastTime = findViewById<TextView>(R.id.lasttime)
        songText = findViewById<TextView>(R.id.songTextView)
        albumImage = findViewById<ImageView>(R.id.albumImage)
        artistText = findViewById<TextView>(R.id.artistName)
        lastTime.text = prevSongText
        artistText.text = SpinnerActivity.songArtistTriple!!.second
        songText.text = SpinnerActivity.songArtistTriple!!.first
        Glide.with(this).load(SpinnerActivity.songArtistTriple!!.third).into(albumImage)

        editor.putString("songText", SpinnerActivity.songArtistTriple!!.first)
        editor.commit()
        editor.putFloat("songRating", 0.0f)
        editor.commit()

        val ratingBar = findViewById<RatingBar>(R.id.ratingbar)
        val listener = RatingListener()
        ratingBar.setOnRatingBarChangeListener(listener)

        restartButton = findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            spinnerFinish = true
            finish()
        }
    }

    inner class RatingListener : RatingBar.OnRatingBarChangeListener {
        override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
            if (fromUser) {
                editor.putFloat("songRating", rating)
                editor.apply()
                val ratingThanksToastText = "Thanks for rating the song!"
                Toast.makeText(this@SongActivity, ratingThanksToastText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendEmail() {
        val songTitle = SpinnerActivity.songArtistTriple!!.first
        var artistString = SpinnerActivity.songArtistTriple!!.second
        val rating = sharedPreferences.getFloat("songRating", 0.0f)

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this song!")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Song Title: $songTitle \nArtist: $artistString\nI rated this song: $rating stars!")

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    companion object {
        var spinnerFinish = false
    }
}
