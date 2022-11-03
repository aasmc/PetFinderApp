package ru.aasmc.petfinderapp.common.data.api.interceptors

import com.google.common.truth.Truth
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import ru.aasmc.petfinderapp.common.data.api.ApiConstants
import ru.aasmc.petfinderapp.common.data.api.ApiParameters
import ru.aasmc.petfinderapp.common.data.api.utils.JsonReader
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import java.time.Instant

/**
 * Test of the AuthenticationInterceptor class. Performed with mocks.
 * This, however, means that we test not only the end result of the methods,
 * but the implementation as well. If we change the implementation of the class,
 * then the tests will fail.
 */
@RunWith(RobolectricTestRunner::class)
class AuthenticationInterceptorTest {

    private lateinit var preferences: Preferences
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authenticationInterceptor: AuthenticationInterceptor
    private lateinit var okHttpClient: OkHttpClient

    private val endpointSeparator = "/"
    private val animalsEndpointPath = endpointSeparator + ApiConstants.ANIMALS_ENDPOINT
    private val authEndpointPath = endpointSeparator + ApiConstants.AUTH_ENDPOINT
    private val validToken = "validToken"
    private val expiredToken = "expiredToken"

    @Before
    fun setup() {
        preferences = mock(Preferences::class.java)

        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        authenticationInterceptor = AuthenticationInterceptor(preferences)
        okHttpClient = OkHttpClient().newBuilder().addInterceptor(authenticationInterceptor).build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun authenticationInterceptor_validToken() {
        //Given
        `when`(preferences.getToken()).thenReturn(validToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().plusSeconds(3600).epochSecond
        )

        mockWebServer.dispatcher = getDispatcherForValidToken()

        // When
        okHttpClient.newCall(
            Request.Builder().url(mockWebServer.url(ApiConstants.ANIMALS_ENDPOINT)).build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()

        with(request) {
            Truth.assertThat(method).isEqualTo("GET")
            Truth.assertThat(path).isEqualTo(animalsEndpointPath)
            Truth.assertThat(getHeader(ApiParameters.AUTH_HEADER)).isEqualTo(ApiParameters.TOKEN_TYPE + validToken)
        }
    }

    @Test
    fun authenticatorInterceptor_expiredToken() {
        // Given
        `when`(preferences.getToken()).thenReturn(expiredToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().minusSeconds(3600).epochSecond
        )

        mockWebServer.dispatcher = getDispatcherForExpiredToken()

        // When
        okHttpClient.newCall(
            Request.Builder().url(mockWebServer.url(ApiConstants.ANIMALS_ENDPOINT)).build()
        ).execute()

        // Then
        val tokenRequest = mockWebServer.takeRequest()
        val animalsRequest = mockWebServer.takeRequest()

        with(tokenRequest) {
            Truth.assertThat(method).isEqualTo("POST")
            Truth.assertThat(path).isEqualTo(authEndpointPath)
        }

        val inOrder = inOrder(preferences)

        inOrder.verify(preferences).getToken()
        inOrder.verify(preferences).putToken(validToken)

        verify(preferences, times(1)).getToken()
        verify(preferences, times(1)).putToken(validToken)
        verify(preferences, times(1)).getTokenExpirationTime()
        verify(preferences, times(1)).putTokenExpirationTime(anyLong())
        verify(preferences, times(1)).putTokenType(ApiParameters.TOKEN_TYPE.trim())
        verifyNoMoreInteractions(preferences)

        with(animalsRequest) {
            Truth.assertThat(method).isEqualTo("GET")
            Truth.assertThat(path).isEqualTo(animalsEndpointPath)
            Truth.assertThat(getHeader(ApiParameters.AUTH_HEADER)).isEqualTo(ApiParameters.TOKEN_TYPE + validToken)
        }
    }

    private fun getDispatcherForValidToken() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                animalsEndpointPath -> { MockResponse().setResponseCode(200) }
                else -> { MockResponse().setResponseCode(404) }
            }
        }
    }

    private fun getDispatcherForExpiredToken() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                authEndpointPath -> {
                    MockResponse().setResponseCode(200).setBody(JsonReader.getJson("validToken.json"))
                }
                animalsEndpointPath -> { MockResponse().setResponseCode(200) }
                else -> { MockResponse().setResponseCode(404) }
            }
        }
    }
}
