package ru.aasmc.petfinderapp.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.animalsnearyou.presentation.AnimalsNearYouFragmentViewModel
import ru.aasmc.petfinderapp.common.data.preferences.PetSavePreferences
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import ru.aasmc.petfinderapp.common.domain.model.user.User
import ru.aasmc.petfinderapp.common.domain.repositories.UserRepository
import ru.aasmc.petfinderapp.common.utils.DATA_SOURCE_FILE_NAME
import ru.aasmc.petfinderapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).findNavController()
    }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            topLevelDestinationIds = setOf(R.id.animalsNearYou, R.id.search, R.id.report)
        )
    }

    private val viewModel: AnimalsNearYouFragmentViewModel by viewModels()
    private var isSignedUp = false
    private var workingFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch to AppTheme for displaying the activity
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        // disable screenshots

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragment()
        setupActionBar()
        setupBottomNav()
        setupWorkingFiles()
        updateLoggedInState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.theme_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val themeMode = when (item.itemId) {
            R.id.light_theme -> {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            R.id.dark_theme -> {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            else -> {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupFragment() {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
        fragment?.let {
            fragmentManager.beginTransaction()
                .hide(it)
                .commit()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.visibility = View.GONE
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupWorkingFiles() {
        workingFile =
            File(filesDir.absolutePath + File.separator + DATA_SOURCE_FILE_NAME)
    }

    fun loginPressed(view: View) {
        displayLogin(view, false)
    }

    private fun updateLoggedInState() {
        val fileExists = workingFile?.exists() ?: false
        if (fileExists) {
            isSignedUp = true
            binding.loginButton.text = getString(R.string.login)
            binding.loginEmail.visibility = View.INVISIBLE
        } else {
            binding.loginButton.text = getString(R.string.signup)
        }
    }

    private fun displayLogin(view: View, fallback: Boolean) {
        // replace below
        performLoginOperation(view)
    }

    private fun performLoginOperation(view: View) {
        var success = false
        val preferences: Preferences = PetSavePreferences(this)

        workingFile?.let {
            // check if already signed up
            if (isSignedUp) {
                val fileInputStream = FileInputStream(it)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val list = objectInputStream.readObject() as ArrayList<User>
                val firstUser = list.first() as? User
                if (firstUser is User) { // 2
                    // replace below with implementation that decrypts password
                    success = true
                }

                if (success) {
                    toast("Last login: ${preferences.getLastLoggedIn()}")
                } else {
                    toast("Please check your credentials and try again.")
                }

                objectInputStream.close()
                fileInputStream.close()
            } else {
                // replace with encrypted data source below
                UserRepository.createDataSource(applicationContext, it, ByteArray(0))
                success = true
            }
        }

        if (success) {
            preferences.putLastLoggedInTime()
            viewModel.setIsLoggedIn(true)

            // show fragment
            binding.loginEmail.visibility = View.GONE
            binding.loginButton.visibility = View.GONE
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.nav_host_fragment)
            fragment?.let {
                fragmentManager.beginTransaction()
                    .show(it)
                    .commit()
            }
            fragmentManager.executePendingTransactions()
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



















