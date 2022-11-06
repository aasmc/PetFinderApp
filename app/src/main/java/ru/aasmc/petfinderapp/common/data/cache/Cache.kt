package ru.aasmc.petfinderapp.common.data.cache

import io.reactivex.Flowable
import io.reactivex.Single
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {

    fun getNearbyAnimals(): Flowable<List<CachedAnimalAggregate>>

    suspend fun storeNearbyAnimals(animals: List<CachedAnimalAggregate>)

    fun storeOrganizations(organizations: List<CachedOrganization>)

    fun getOrganization(organizationId: String): Single<CachedOrganization>

    suspend fun getAllTypes(): List<String>

    fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>>

    fun getAnimal(animalId: Long): Single<CachedAnimalAggregate>
}