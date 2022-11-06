package ru.aasmc.petfinderapp.common.data

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.HttpException
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinderapp.common.data.cache.Cache
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization
import ru.aasmc.petfinderapp.common.domain.model.NetworkException
import ru.aasmc.petfinderapp.common.domain.model.animal.Animal
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Age
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.model.pagination.PaginatedAnimals
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.search.domain.model.SearchParameters
import ru.aasmc.petfinderapp.search.domain.model.SearchResults
import javax.inject.Inject

class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
) : AnimalRepository {

    /**
     * For now these are temporary placeholders.
     */
    private val postcode = "07097"
    private val maxDistanceMiles = 100

    override fun getAnimals(): Flowable<List<Animal>> =
        cache.getNearbyAnimals()
            // ensures that only events with new information get to the subscriber
            .distinctUntilChanged()
            .map { animalList ->
                animalList.map {
                    it.animal.toAnimalDomain(
                        it.photos,
                        it.videos,
                        it.tags
                    )
                }
            }

    override suspend fun requestMoreAnimals(
        pageToLoad: Int,
        numberOfItems: Int
    ): PaginatedAnimals {
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
        } catch (e: HttpException) {
            throw NetworkException(e.message ?: "Code ${e.code()}")
        }
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        val organizations =
            animals.map { CachedOrganization.fromDomain(it.details.organization) }
        // Organizations have a one-to-many relationship with animals, so we have to
        // insert them before inserting animals. Otherwise Room will complain about not
        // being able to satisfy the foreign key's constraint.
        cache.storeOrganizations(organizations)
        cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }

    override suspend fun getAnimalTypes(): List<String> =
        cache.getAllTypes()

    override fun getAnimalAges(): List<Age> =
        Age.values().toList()

    override fun searchCachedAnimalsBy(
        searchParameters: SearchParameters
    ): Flowable<SearchResults> {
        val (name, age, type) = searchParameters
        return cache.searchAnimalsBy(name, age, type)
            .distinctUntilChanged()
            .map { animalList ->
                animalList.map {
                    it.animal.toAnimalDomain(
                        it.photos,
                        it.videos,
                        it.tags
                    )
                }
            }
            .map {
                SearchResults(it, searchParameters)
            }
    }

    override suspend fun searchAnimalsRemotely(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        numberOfItems: Int
    ): PaginatedAnimals {
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

    override fun getAnimal(animalId: Long): Single<AnimalWithDetails> {
        return cache.getAnimal(animalId)
            .flatMap { animal ->
                cache.getOrganization(animal.animal.organizationId)
                    .map {
                        animal.animal.toDomain(
                            animal.photos,
                            animal.videos,
                            animal.tags,
                            it
                        )
                    }
            }
    }
}
