package ru.aasmc.petfinderapp.details.domain.usecases

import io.reactivex.Single
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AnimalDetails @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    operator fun invoke(
        animalId: Long
    ): Single<AnimalWithDetails> {
        return animalRepository.getAnimal(animalId)
            .delay(2, TimeUnit.SECONDS)
    }
}