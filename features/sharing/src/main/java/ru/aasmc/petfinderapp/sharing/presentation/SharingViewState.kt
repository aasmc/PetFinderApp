package ru.aasmc.petfinderapp.sharing.presentation

import ru.aasmc.petfinderapp.sharing.presentation.model.UIAnimalToShare

data class SharingViewState(
    val animalToShare: UIAnimalToShare = UIAnimalToShare(image = "", defaultMessage = "")
)