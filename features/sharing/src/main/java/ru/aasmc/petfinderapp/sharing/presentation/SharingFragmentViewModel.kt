package ru.aasmc.petfinderapp.sharing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.common.domain.usecases.GetAnimalDetails
import ru.aasmc.petfinderapp.sharing.presentation.model.mappers.UiAnimalToShareMapper
import javax.inject.Inject

class SharingFragmentViewModel @Inject constructor(
    private val getAnimalDetails: GetAnimalDetails,
    private val uiAnimalToShareMapper: UiAnimalToShareMapper
) : ViewModel() {
    private val _viewState = MutableStateFlow(SharingViewState())
    val viewState: StateFlow<SharingViewState> = _viewState.asStateFlow()

    fun onEvent(event: SharingEvent) {
        when (event) {
            is SharingEvent.GetAnimalToShare -> getAnimalToShare(event.animalId)
        }
    }

    private fun getAnimalToShare(animalId: Long) {
        viewModelScope.launch {
            val animal = getAnimalDetails(animalId)
            _viewState.update { oldState ->
                oldState.copy(
                    animalToShare = uiAnimalToShareMapper.mapToView(animal)
                )
            }
        }
    }
}