package ru.aasmc.petfinderapp.animalsnearyou.presentation

import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimal

/**
 * Represents view state for AnimalsNearYouFragment.
 * Default values represent initial state.
 */
data class AnimalsNearYouViewState(
    val loading: Boolean = true,
    val animals: List<UIAnimal> = emptyList(),
    val noMoreAnimalsNearby: Boolean = false,
    val failure: Event<Throwable>? = null
)