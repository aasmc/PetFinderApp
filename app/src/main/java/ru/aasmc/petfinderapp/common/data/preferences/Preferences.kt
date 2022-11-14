package ru.aasmc.petfinderapp.common.data.preferences

interface Preferences {

    fun putToken(token: String)

    fun putTokenExpirationTime(time: Long)

    fun putTokenType(tokenType: String)

    fun putLastLoggedInTime()

    fun getToken(): String

    fun getTokenExpirationTime(): Long

    fun getTokenType(): String

    fun getLastLoggedIn(): String?

    fun deleteTokenInfo()
}