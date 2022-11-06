package ru.aasmc.petfinderapp.details.presentation

sealed class AnimalDetailsEvent {
    data class LoadAnimalDetails(val animalId: Long): AnimalDetailsEvent()
}