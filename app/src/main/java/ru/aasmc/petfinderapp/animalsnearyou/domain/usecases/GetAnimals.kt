package ru.aasmc.petfinderapp.animalsnearyou.domain.usecases

import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {
    operator fun invoke() = animalRepository.getAnimals()
        .filter { it.isNotEmpty() }
}