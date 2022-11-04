package ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails

sealed class AnimalDetailsEvent {
    data class LoadAnimalDetails(val animalId: Long) : AnimalDetailsEvent()
}