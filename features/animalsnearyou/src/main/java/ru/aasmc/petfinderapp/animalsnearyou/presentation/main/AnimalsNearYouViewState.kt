package ru.aasmc.petfinderapp.animalsnearyou.presentation.main

import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimal

data class AnimalsNearYouViewState(
    val loading: Boolean = true,
    val animals: List<UIAnimal> = emptyList(),
    val noMoreAnimalsNearby: Boolean = false,
    val failure: Event<Throwable>? = null
)