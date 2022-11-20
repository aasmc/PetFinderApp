package ru.aasmc.petfinderapp.common.data.di

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.aasmc.petfinderapp.common.data.api.ApiConstants
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.api.interceptors.AuthenticationInterceptor
import ru.aasmc.petfinderapp.common.data.api.interceptors.LoggingInterceptor
import ru.aasmc.petfinderapp.common.data.api.interceptors.NetworkStatusInterceptor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiModule {

    @Provides
    @Singleton
    fun provideApi(builder: Retrofit.Builder): PetFinderApi {
        return builder
            .build()
            .create(PetFinderApi::class.java)
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
    }

    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkStatusInterceptor: NetworkStatusInterceptor,
        authenticationInterceptor: AuthenticationInterceptor
    ): OkHttpClient {
        val hostname = "**.petfinder.com" //Two asterisks matches any number of subdomains
        val certificatePinner = CertificatePinner.Builder()
            .add(hostname, "sha256/JOKuxI6G3nQJb9wHWcXFZkQR1sEOAPMebWnQ+5AI4/I=")
            .add(hostname, "sha256/JSMzqOOrtyOT1kmau6zKhgT676hGgczD5VMdRMyJZFA=")
            .build()

        val ctInterceptor = certificateTransparencyInterceptor {
            // Enable for the provided hosts
            +"*.petfinder.com" // 1. For subdomains
            +"petfinder.com" // 2. asterisk doesn't cover base domain
            //+"*.*" - this will add all hosts
            //-"legacy.petfinder.com" //3 Exclude specific hosts
        }

        return OkHttpClient.Builder()
            // Order of interceptors matters.
            // First - network availability
            // Second - authentication
            // Third - logging
            .certificatePinner(certificatePinner)
            .addNetworkInterceptor(ctInterceptor)
            .addInterceptor(networkStatusInterceptor)
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            // disable in-memory cache, which is used by the OkHttp
            .cache(null)
            .build()
    }

    @Provides
    fun provideHttpLoggingInterceptor(loggingInterceptor: LoggingInterceptor): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(loggingInterceptor)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}