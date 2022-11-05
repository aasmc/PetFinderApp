package ru.aasmc.petfinderapp

import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import ru.aasmc.petfinderapp.logging.Logger

@HiltAndroidApp
class PetSaveApplication : SplitCompatApplication() {
    override fun onCreate() {
        super.onCreate()

        initLogger()
    }

    private fun initLogger() {
        Logger.init()
    }

// Alternative to extending SplitCompatApplication
// SplitCompat enables dynamic features
//  override fun attachBaseContext(base: Context?) {
//    super.attachBaseContext(base)
//
//    SplitCompat.install(this)
//  }

}