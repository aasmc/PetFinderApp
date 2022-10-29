package ru.aasmc.petfinderapp.common.data.api.interceptors

import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.aasmc.petfinderapp.BuildConfig
import ru.aasmc.petfinderapp.common.data.api.ApiConstants.AUTH_ENDPOINT
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.AUTH_HEADER
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.CLIENT_ID
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.CLIENT_SECRET
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.GRANT_TYPE_KEY
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.GRANT_TYPE_VALUE
import ru.aasmc.petfinderapp.common.data.api.ApiParameters.TOKEN_TYPE
import ru.aasmc.petfinderapp.common.data.api.model.ApiToken
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import java.time.Instant
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    private val preferences: Preferences
) : Interceptor {

    companion object {
        const val UNAUTHORIZED = 401
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = preferences.getToken()
        val tokenExpirationTime = Instant.ofEpochSecond(
            preferences.getTokenExpirationTime()
        )
        val request = chain.request()
        // this is an example for requests that don't need authentication
//        if (chain.request().headers[NO_AUTH_HEADER] != null) return chain.proceed(request)
        val interceptedRequest: Request

        if (tokenExpirationTime.isAfter(Instant.now())) {
            // token still valid, so we can proceed with the request
            interceptedRequest = chain.createAuthenticatedRequest(token)
        } else {
            // Token expired. Gotta refresh it before proceeding with the actual request
            val tokenRefreshResponse = chain.refreshToken()
            interceptedRequest = if (tokenRefreshResponse.isSuccessful) {
                val newToken = mapToken(tokenRefreshResponse)
                if (newToken.isValid()) {
                    storeNewToken(newToken)
                    chain.createAuthenticatedRequest(newToken.accessToken!!)
                } else {
                    request
                }
            } else {
                request
            }
        }
        return chain.proceedDeletingTokenIfUnauthorized(interceptedRequest)
    }

    private fun Interceptor.Chain.createAuthenticatedRequest(token: String): Request {
        return request()
            .newBuilder()
            .addHeader(AUTH_HEADER, TOKEN_TYPE + token)
            .build()
    }

    private fun Interceptor.Chain.refreshToken(): Response {
        val url = request()
            .url
            .newBuilder(AUTH_ENDPOINT)!!
            .build()

        val body = FormBody.Builder()
            .add(GRANT_TYPE_KEY, GRANT_TYPE_VALUE)
            .add(CLIENT_ID, BuildConfig.API_KEY)
            .add(CLIENT_SECRET, BuildConfig.CLIENT_SECRET)
            .build()

        val tokenRefresh = request()
            .newBuilder()
            .post(body)
            .url(url)
            .build()

        return proceedDeletingTokenIfUnauthorized(tokenRefresh)
    }

    private fun Interceptor.Chain.proceedDeletingTokenIfUnauthorized(request: Request): Response {
        val response = proceed(request)

        if (response.code == UNAUTHORIZED) {
            preferences.deleteTokenInfo()
        }

        return response
    }

    private fun mapToken(tokenRefreshResponse: Response): ApiToken {
        val moshi = Moshi.Builder().build()
        val tokenAdapter = moshi.adapter(ApiToken::class.java)
        val responseBody =
            tokenRefreshResponse.body!!

        return tokenAdapter.fromJson(responseBody.string()) ?: ApiToken.INVALID
    }

    private fun storeNewToken(apiToken: ApiToken) {
        with(preferences) {
            putTokenType(apiToken.tokenType!!)
            putTokenExpirationTime(apiToken.expiresAt)
            putToken(apiToken.accessToken!!)
        }
    }
}
