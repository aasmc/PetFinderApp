package ru.aasmc.petfinderapp.common.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import ru.aasmc.petfinderapp.common.data.preferences.FakePreferences
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PreferencesModule::class]
)
abstract class TestPreferencesModule {

    @Binds
    @Singleton
    abstract fun providePreferences(preferences: FakePreferences): Preferences
}