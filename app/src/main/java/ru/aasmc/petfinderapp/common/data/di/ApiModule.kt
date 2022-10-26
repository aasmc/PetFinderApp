package ru.aasmc.petfinderapp.common.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.aasmc.petfinderapp.common.data.api.ApiConstants
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.api.interceptors.LoggingInterceptor
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideApi(okHttpClient: OkHttpClient): PetFinderApi {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PetFinderApi::class.java)
    }

    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    fun provideHttpLoggingInterceptor(loggingInterceptor: LoggingInterceptor): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(loggingInterceptor)
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}