package ru.aasmc.petfinderapp.details.presentation

import ru.aasmc.petfinderapp.common.presentation.model.UIAnimalDetailed

sealed class AnimalDetailsViewState {
    object Loading : AnimalDetailsViewState()

    data class AnimalDetails(
        val animal: UIAnimalDetailed
    ) : AnimalDetailsViewState()

    object Failure : AnimalDetailsViewState()
}
