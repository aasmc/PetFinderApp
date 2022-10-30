package ru.aasmc.petfinderapp.animalsnearyou.presentation

sealed class AnimalsNearYouEvent {
    object RequestInitialAnimalsList: AnimalsNearYouEvent()
    object RequestMoreAnimals: AnimalsNearYouEvent()
}
