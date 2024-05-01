package com.example.groupfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {
    lateinit var adView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Spotify API Client Testing
        val apiClient = SpotifyApiClient()
        apiClient.test()

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
            val username = usernamePrompt.text.toString() //@RIYA
            if (username != "" && username != "Type your username here") {
                // firebase stuff
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

}