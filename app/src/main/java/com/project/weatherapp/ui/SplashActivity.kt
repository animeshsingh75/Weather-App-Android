package com.project.weatherapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.project.weatherapp.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val actionBar= supportActionBar
        actionBar!!.hide()
        val handler = Handler().postDelayed({
            startActivity(Intent(this,MainActivity::class.java))

        }, 2500)

    }
}