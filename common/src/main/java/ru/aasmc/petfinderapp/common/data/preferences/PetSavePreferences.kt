package ru.aasmc.petfinderapp.common.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.aasmc.petfinderapp.common.data.preferences.PreferencesConstants.KEY_MAX_DISTANCE
import ru.aasmc.petfinderapp.common.data.preferences.PreferencesConstants.KEY_POSTCODE
import ru.aasmc.petfinderapp.common.data.preferences.PreferencesConstants.KEY_TOKEN
import ru.aasmc.petfinderapp.common.data.preferences.PreferencesConstants.KEY_TOKEN_EXPIRATION_TIME
import ru.aasmc.petfinderapp.common.data.preferences.PreferencesConstants.KEY_TOKEN_TYPE
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetSavePreferences @Inject constructor(
    @ApplicationContext context: Context
) : Preferences {

    companion object {
        const val PREFERENCES_NAME = "PET_SAVE_PREFERENCES"
    }

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun putToken(token: String) {
        edit { putString(KEY_TOKEN, token) }
    }

    override fun putTokenExpirationTime(time: Long) {
        edit { putLong(KEY_TOKEN_EXPIRATION_TIME, time) }
    }

    override fun putTokenType(tokenType: String) {
        edit { putString(KEY_TOKEN_TYPE, tokenType) }
    }

    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(preferences.edit()) {
            block()
            commit()
        }
    }

    override fun getToken(): String {
        return preferences.getString(KEY_TOKEN, "").orEmpty()
    }

    override fun getTokenExpirationTime(): Long {
        return preferences.getLong(KEY_TOKEN_EXPIRATION_TIME, -1)
    }

    override fun getTokenType(): String {
        return preferences.getString(KEY_TOKEN_TYPE, "").orEmpty()
    }

    override fun deleteTokenInfo() {
        edit {
            remove(KEY_TOKEN)
            remove(KEY_TOKEN_EXPIRATION_TIME)
            remove(KEY_TOKEN_TYPE)
        }
    }

    override fun getPostcode(): String {
        return preferences.getString(KEY_POSTCODE, "").orEmpty()
    }

    override fun putPostcode(postcode: String) {
        edit { putString(KEY_POSTCODE, postcode) }
    }

    override fun getMaxDistanceAllowedToGetAnimals(): Int {
        return preferences.getInt(KEY_MAX_DISTANCE, 0)
    }

    override fun putMaxDistanceAllowedToGetAnimals(distance: Int) {
        edit { putInt(KEY_MAX_DISTANCE, distance) }
    }

}