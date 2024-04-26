package com.example.groupfinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startbutton)

        startButton.setOnClickListener {
            //val inputText = numEntered.text.toString() //
            //if (inputText.isNotBlank()) { //i just did this for testing purposes so change if needed! -sri
            val intent = Intent(this, SpinnerActivity::class.java)
            startActivity(intent)
            //}

        }

    }
}