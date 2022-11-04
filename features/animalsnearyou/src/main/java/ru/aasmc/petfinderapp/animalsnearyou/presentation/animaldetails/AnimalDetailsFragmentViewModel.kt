package ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails.model.mapper.UiAnimalDetailsMapper
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.usecases.GetAnimalDetails
import javax.inject.Inject

@HiltViewModel
class AnimalDetailsFragmentViewModel @Inject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val getAnimalDetails: GetAnimalDetails
) : ViewModel() {
    private val _state: MutableStateFlow<AnimalDetailsViewState> =
        MutableStateFlow(AnimalDetailsViewState.Loading)
    val state: StateFlow<AnimalDetailsViewState> = _state.asStateFlow()

    fun handleEvent(event: AnimalDetailsEvent) {
        when (event) {
            is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
        }
    }

    private fun subscribeToAnimalDetails(animalId: Long) {
        viewModelScope.launch {
            try {
                val animal = getAnimalDetails(animalId)
                onAnimalDetails(animal)
            } catch (t: Throwable) {
                onFailure(t)
            }
        }
    }

    private fun onAnimalDetails(animal: AnimalWithDetails) {
        val animalDetails = uiAnimalDetailsMapper.mapToView(animal)
        _state.update { AnimalDetailsViewState.AnimalDetails(animalDetails) }
    }

    private fun onFailure(failure: Throwable) {
        _state.update { AnimalDetailsViewState.Failure }
    }
}