package ru.aasmc.petfinderapp.common.data.api.utils

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import ru.aasmc.petfinderapp.common.data.api.ApiConstants
import ru.aasmc.petfinderapp.logging.Logger
import java.io.IOException

class FakeServer {
    private val mockWebServer = MockWebServer()

    private val endpointSeparator = "/"
    private val responsesBasePath = "networkresponses/"
    private val animalsEndpointPath = endpointSeparator + ApiConstants.ANIMALS_ENDPOINT
    private val notFoundResponse = MockResponse().setResponseCode(404)

    val baseEndpoint
        get() = mockWebServer.url(endpointSeparator)

    fun start() {
        mockWebServer.start(8080)
    }

    fun shutDown() {
        mockWebServer.shutdown()
    }

    fun setHappyPathDispatcher() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return notFoundResponse

                return with(path) {
                    when {
                        startsWith(animalsEndpointPath) -> {
                            MockResponse()
                                .setResponseCode(200)
                                .setBody(getJson("${responsesBasePath}animals.json"))
                        }
                        else -> notFoundResponse
                    }
                }
            }
        }
    }

    private fun getJson(path: String): String = try {
        val context = InstrumentationRegistry.getInstrumentation().context
        val jsonStream = context.assets.open(path)
        String(jsonStream.readBytes())
    } catch (e: IOException) {
        Logger.e(e, "Error reading network response json asset.")
        throw e
    }
}