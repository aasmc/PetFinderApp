package ru.aasmc.petfinderapp.common.data.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.aasmc.petfinderapp.common.data.cache.Cache
import ru.aasmc.petfinderapp.common.data.cache.PetSaveDatabase
import ru.aasmc.petfinderapp.common.data.cache.RoomCache
import ru.aasmc.petfinderapp.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinderapp.common.data.cache.daos.OrganizationsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    abstract fun bindCache(cache: RoomCache): Cache

    companion object {

        @Singleton
        @Provides
        fun provideDatabase(@ApplicationContext context: Context): PetSaveDatabase {
            return Room.databaseBuilder(
                context,
                PetSaveDatabase::class.java,
                "petsave.db"
            )
                .build()
        }

        @Provides
        fun provideOrganizationsDao(petSaveDatabase: PetSaveDatabase): OrganizationsDao =
            petSaveDatabase.organizationsDao()

        @Provides
        fun provideAnimalsDao(petSaveDatabase: PetSaveDatabase): AnimalsDao =
            petSaveDatabase.animalsDao()
    }

}