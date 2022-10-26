package ru.aasmc.petfinderapp.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch to AppTheme for displaying the activity
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
    }
}