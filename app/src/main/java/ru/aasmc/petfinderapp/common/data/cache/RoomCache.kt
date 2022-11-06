package ru.aasmc.petfinderapp.common.data.cache

import io.reactivex.Flowable
import io.reactivex.Single
import ru.aasmc.petfinderapp.common.data.cache.daos.AnimalsDao
import ru.aasmc.petfinderapp.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization
import javax.inject.Inject

class RoomCache @Inject constructor(
    private val animalsDao: AnimalsDao,
    private val organizationsDao: OrganizationsDao
) : Cache {

    override fun getNearbyAnimals(): Flowable<List<CachedAnimalAggregate>> =
        animalsDao.getAllAnimals()

    override suspend fun storeNearbyAnimals(animals: List<CachedAnimalAggregate>) {
        animalsDao.insertAnimalsWithDetails(animals)
    }

    override fun storeOrganizations(organizations: List<CachedOrganization>) {
        organizationsDao.insert(organizations)
    }

    override fun getOrganization(organizationId: String): Single<CachedOrganization> {
        return organizationsDao.getOrganization(organizationId)
    }

    override suspend fun getAllTypes(): List<String> =
        animalsDao.getAllTypes()

    override fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>> {
        return animalsDao.searchAnimalsBy(name, age, type)
    }

    override fun getAnimal(animalId: Long): Single<CachedAnimalAggregate> {
        return animalsDao.getAnimal(animalId)
    }
}