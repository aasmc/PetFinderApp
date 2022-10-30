package ru.aasmc.petfinderapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.aasmc.petfinderapp.logging.Logger

@HiltAndroidApp
class PetSaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }
}