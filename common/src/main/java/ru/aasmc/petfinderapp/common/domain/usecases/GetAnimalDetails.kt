package ru.aasmc.petfinderapp.common.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import javax.inject.Inject

class GetAnimalDetails @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    suspend operator fun invoke(animalId: Long): AnimalWithDetails =
        withContext(dispatchersProvider.io()) {
            animalRepository.getAnimal(animalId)
        }
}