package ru.aasmc.petfinderapp.details.presentation

import ru.aasmc.petfinderapp.common.presentation.model.UIAnimalDetailed

sealed class AnimalDetailsViewState {
    object Loading : AnimalDetailsViewState()

    data class AnimalDetails(
        val animal: UIAnimalDetailed,
        val adopted: Boolean = false
    ) : AnimalDetailsViewState()

    object Failure : AnimalDetailsViewState()
}
