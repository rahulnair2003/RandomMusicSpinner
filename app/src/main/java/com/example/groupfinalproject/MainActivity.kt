package com.example.groupfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var adView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        // Spotify API Client
        CoroutineScope(Dispatchers.Main).launch {
            // Call the suspending function within the coroutine
            try {
                token = model.generateToken()
            } catch (e: Exception) {
                // Handle exceptions here
                println("Exception occurred: ${e.message}")
            }
        }

        // add advertising
        adView = AdView(this)
        var adSize: AdSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)
        var adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
        adView.setAdUnitId(adUnitId)
        var builder: AdRequest.Builder = AdRequest.Builder()
        var request: AdRequest = builder.build()
        var adLayout : LinearLayout = findViewById<LinearLayout>(R.id.ad_view)
        adLayout.addView(adView)
        adView.loadAd(request)

        val startButton = findViewById<Button>(R.id.startbutton)
        val usernamePrompt = findViewById<EditText>(R.id.username)
        startButton.setOnClickListener {
            username = usernamePrompt.text.toString() //@RIYA
            if (username != "" && username != "Type your username here") {
                // firebase stuff
                val valListener = ValListener()
                reference.child(username).addListenerForSingleValueEvent(valListener)
                val intent = Intent(this, SpinnerActivity::class.java)
                startActivity(intent)
            }
            else {
                val toast = Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG)
                toast.show()
            }

        }

    }
    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }
    suspend fun generateGenres() {
        try {
            val topGenres = model.getTopGenres(Companion.token)
            Log.d("Test", "Top Genres: $topGenres")
        } catch (e: Exception) {
            Log.e("Test", "Error: ${e.message}", e)
        }
    }

    class ValListener: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                Log.w("firebase", "found previous data")
                prevGenre = dataSnapshot.child("genre").value.toString()
                prevYear = dataSnapshot.child("year").value.toString()
                prevArtist = dataSnapshot.child("artist").value.toString()
                Log.w("prevdata", prevGenre)
                Log.w("prevdata", prevYear)
                Log.w("prevdata", prevArtist)
            } else {
                Log.w("firebase", "new user")
                reference.child(username).setValue(true)
            }
        }
        override fun onCancelled(error : DatabaseError ) {
            Log.w( "MainActivity", "error: " + error.message )
        }
    }

    companion object {
        lateinit var token : String
        val model = SpotifyApiClient()
        val firebase = FirebaseDatabase.getInstance()
        val reference = firebase.getReference("users")
        lateinit var username: String
        var prevGenre: String = ""
        var prevYear: String = ""
        var prevArtist: String = ""

    }
}