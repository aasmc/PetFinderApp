package ru.aasmc.petfinderapp.common.data

import io.reactivex.Flowable
import retrofit2.HttpException
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinderapp.common.data.cache.Cache
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import ru.aasmc.petfinderapp.common.domain.model.NetworkException
import ru.aasmc.petfinderapp.common.domain.model.animal.Animal
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Age
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinderapp.common.domain.model.search.SearchParameters
import ru.aasmc.petfinderapp.common.domain.model.search.SearchResults
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val preferences: Preferences,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
) : AnimalRepository {


    override fun getAnimals(): Flowable<List<Animal>> {
        return cache.getNearbyAnimals()
            .distinctUntilChanged()
            .map { animalList ->
                animalList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
            }
    }

    override suspend fun requestMoreAnimals(
        pageToLoad: Int,
        numberOfItems: Int
    ): PaginatedAnimals {
        val postcode = preferences.getPostcode()
        val maxDistanceMiles = preferences.getMaxDistanceAllowedToGetAnimals()

        try {
            val (apiAnimals, apiPagination) = api.getNearbyAnimals(
                pageToLoad,
                numberOfItems,
                postcode,
                maxDistanceMiles
            )

            return PaginatedAnimals(
                apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
                apiPaginationMapper.mapToDomain(apiPagination)
            )
        } catch (exception: HttpException) {
            throw NetworkException(exception.message ?: "Code ${exception.code()}")
        }
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        // Organizations have a 1-to-many relation with animals, so we need to insert them first in
        // order for Room not to complain about foreign keys being invalid (since we have the
        // organizationId as a foreign key in the animals table)
        val organizations =
            animals.map { CachedOrganization.fromDomain(it.details.organization) }

        cache.storeOrganizations(organizations)
        cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }

    override suspend fun getAnimalTypes(): List<String> {
        return cache.getAllTypes()
    }

    override suspend fun getAnimal(animalId: Long): AnimalWithDetails {
        val (animal, photos, videos, tags) =
            cache.getAnimal(animalId)
        val organization = cache.getOrganization(animal.organizationId)
        return animal.toDomain(photos, videos, tags, organization)
    }

    override fun getAnimalAges(): List<Age> {
        return Age.values().toList()
    }

    override fun searchCachedAnimalsBy(searchParameters: SearchParameters): Flowable<SearchResults> {
        val (name, age, type) = searchParameters

        return cache.searchAnimalsBy(name, age, type)
            .distinctUntilChanged().map { animalList ->
                animalList.map { it.animal.toAnimalDomain(it.photos, it.videos, it.tags) }
            }
            .map { SearchResults(it, searchParameters) }
    }

    override suspend fun searchAnimalsRemotely(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        numberOfItems: Int
    ): PaginatedAnimals {

        val postcode = preferences.getPostcode()
        val maxDistanceMiles = preferences.getMaxDistanceAllowedToGetAnimals()

        val (apiAnimals, apiPagination) = api.searchAnimalsBy(
            searchParameters.name,
            searchParameters.age,
            searchParameters.type,
            pageToLoad,
            numberOfItems,
            postcode,
            maxDistanceMiles
        )

        return PaginatedAnimals(
            apiAnimals?.map { apiAnimalMapper.mapToDomain(it) }.orEmpty(),
            apiPaginationMapper.mapToDomain(apiPagination)
        )
    }

    override suspend fun storeOnboardingData(postcode: String, distance: Int) {
        with(preferences) {
            putPostcode(postcode)
            putMaxDistanceAllowedToGetAnimals(distance)
        }
    }

    override suspend fun onboardingIsComplete(): Boolean {
        return preferences.getPostcode().isNotEmpty() &&
                preferences.getMaxDistanceAllowedToGetAnimals() > 0
    }
}