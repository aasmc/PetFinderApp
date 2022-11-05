package ru.aasmc.petfinderapp.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.cache.Cache
import ru.aasmc.petfinderapp.common.data.preferences.Preferences

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharingModuleDependencies {
    fun petFinderApi(): PetFinderApi
    fun cache(): Cache
    fun preferences(): Preferences
}