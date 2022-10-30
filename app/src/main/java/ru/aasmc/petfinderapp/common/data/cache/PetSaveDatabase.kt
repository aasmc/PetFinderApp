package ru.aasmc.petfinderapp.common.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.aasmc.petfinderapp.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinderapp.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalTagCrossRef
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalWithDetails
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedPhoto
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedTag
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedVideo
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization

@Database(
    entities = [
        CachedPhoto::class,
        CachedVideo::class,
        CachedTag::class,
        CachedAnimalTagCrossRef::class,
        CachedOrganization::class,
        CachedAnimalWithDetails::class
    ],
    version = 1
)
abstract class PetSaveDatabase : RoomDatabase() {
    abstract fun organizationsDao(): OrganizationsDao
    abstract fun animalsDao(): AnimalsDao
}