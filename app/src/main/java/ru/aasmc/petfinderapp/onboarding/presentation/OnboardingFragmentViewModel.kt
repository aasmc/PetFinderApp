package ru.aasmc.petfinderapp.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.utils.createExceptionHandler
import ru.aasmc.petfinderapp.onboarding.domain.usecases.StoreOnboardingData
import javax.inject.Inject

@HiltViewModel
class OnboardingFragmentViewModel @Inject constructor(
    private val storeOnboardingData: StoreOnboardingData
) : ViewModel() {
    companion object {
        private const val MAX_POSTCODE_LENGTH = 5
    }

    private val _viewState = MutableStateFlow(OnboardingViewState())
    val viewState: StateFlow<OnboardingViewState> = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<OnboardingViewEffect>()
    val viewEffects: SharedFlow<OnboardingViewEffect> = _viewEffects.asSharedFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.DistanceChanged -> validateNewDistanceValue(event.newDistance)
            is OnboardingEvent.PostcodeChanged -> validateNewPostcodeValue(event.newPostcode)
            OnboardingEvent.SubmitButtonClicked -> wrapUpOnBoarding()
        }
    }

    private fun validateNewPostcodeValue(newPostcode: String) {
        val validPostcode = newPostcode.length == MAX_POSTCODE_LENGTH

        val postcodeError = if (validPostcode || newPostcode.isEmpty()) {
            R.string.no_error
        } else {
            R.string.postcode_error
        }

        _viewState.update { oldState ->
            oldState.copy(
                postcode = newPostcode,
                postcodeError = postcodeError
            )
        }
    }

    private fun validateNewDistanceValue(newDistance: String) {
        val distanceError = when {
            newDistance.isNotEmpty() && newDistance.toInt() > 500 -> {
                R.string.distance_error
            }
            newDistance.isNotEmpty() && newDistance.toInt() == 0 -> {
                R.string.distance_error_cannot_be_zero
            }
            else -> {
                R.string.no_error
            }
        }

        _viewState.update { oldState ->
            oldState.copy(
                distance = newDistance,
                distanceError = distanceError
            )
        }
    }

    private fun wrapUpOnBoarding() {
        val errorMessage = "Failed to store onboarding data"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
            onFailure(it)
        }
        val (postcode, distance) = viewState.value
        viewModelScope.launch(exceptionHandler) {
            storeOnboardingData(postcode, distance)
            _viewEffects.emit(OnboardingViewEffect.NavigateToAnimalsNearYou)
        }
    }

    private fun onFailure(throwable: Throwable) {
        // TODO handle failures
    }
}