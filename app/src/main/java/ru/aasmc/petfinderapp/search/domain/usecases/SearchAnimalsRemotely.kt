package ru.aasmc.petfinderapp.search.domain.usecases

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinderapp.common.domain.model.pagination.Pagination
import ru.aasmc.petfinderapp.common.domain.model.pagination.Pagination.Companion.DEFAULT_PAGE_SIZE
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import ru.aasmc.petfinderapp.search.domain.model.SearchParameters
import javax.inject.Inject

class SearchAnimalsRemotely @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    suspend operator fun invoke(
        pageToLoad: Int,
        searchParameters: SearchParameters,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Pagination {
        return withContext(dispatchersProvider.io()) {
            val (animals, pagination) =
                animalRepository.searchAnimalsRemotely(pageToLoad, searchParameters, pageSize)
            if (!coroutineContext.isActive) {
                throw CancellationException("Cancelled because new data was requested")
            }
            if (animals.isEmpty()) {
                throw NoMoreAnimalsException("Couldn't find more animals that match the search parameters.")
            }
            animalRepository.storeAnimals(animals)

            return@withContext pagination
        }
    }
}