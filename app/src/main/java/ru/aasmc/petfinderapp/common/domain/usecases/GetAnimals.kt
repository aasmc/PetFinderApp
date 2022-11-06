package ru.aasmc.petfinderapp.common.domain.usecases

import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    operator fun invoke() = animalRepository.getAnimals()
        .filter { it.isNotEmpty() }
}