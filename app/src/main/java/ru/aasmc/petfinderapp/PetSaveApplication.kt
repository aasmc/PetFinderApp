package ru.aasmc.petfinderapp

import android.app.Application
import ru.aasmc.petfinderapp.logging.Logger

class PetSaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }
}