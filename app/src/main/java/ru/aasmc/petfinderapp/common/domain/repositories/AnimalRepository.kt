package ru.aasmc.petfinderapp.common.domain.repositories

import io.reactivex.Flowable
import ru.aasmc.petfinderapp.common.domain.model.animal.Animal
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Age
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinderapp.search.domain.model.SearchParameters
import ru.aasmc.petfinderapp.search.domain.model.SearchResults

interface AnimalRepository {

    fun getAnimals(): Flowable<List<Animal>>

    suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals

    suspend fun storeAnimals(animals: List<AnimalWithDetails>)

    suspend fun getAnimalTypes(): List<String>

    fun getAnimalAges(): List<Age>

    fun searchCachedAnimalsBy(searchParameters: SearchParameters): Flowable<SearchResults>

    suspend fun searchAnimalsRemotely(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        numberOfItems: Int
    ): PaginatedAnimals

}