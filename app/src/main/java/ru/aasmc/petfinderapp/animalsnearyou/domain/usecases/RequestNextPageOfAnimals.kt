package ru.aasmc.petfinderapp.animalsnearyou.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinderapp.common.domain.model.pagination.Pagination
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import javax.inject.Inject

class RequestNextPageOfAnimals @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    suspend operator fun invoke(
        pageToLoad: Int,
        pageSize: Int = Pagination.DEFAULT_PAGE_SIZE
    ): Pagination {
        return withContext(dispatchersProvider.io()) {
            val (animals, pagination) =
                animalRepository.requestMoreAnimals(pageToLoad, pageSize)
            if (animals.isEmpty()) {
                throw NoMoreAnimalsException("No more animals nearby :(")
            }
            animalRepository.storeAnimals(animals)
            return@withContext pagination
        }
    }
}