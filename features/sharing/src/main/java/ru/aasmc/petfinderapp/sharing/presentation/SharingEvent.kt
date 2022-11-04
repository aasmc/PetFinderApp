package ru.aasmc.petfinderapp.sharing.presentation

sealed class SharingEvent {
    data class GetAnimalToShare(val animalId: Long): SharingEvent()
}