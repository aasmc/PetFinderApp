package ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails

import ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed

sealed class AnimalDetailsViewState {
    object Loading : AnimalDetailsViewState()

    data class AnimalDetails(
        val animal: UIAnimalDetailed
    ) : AnimalDetailsViewState()

    object Failure : AnimalDetailsViewState()
}