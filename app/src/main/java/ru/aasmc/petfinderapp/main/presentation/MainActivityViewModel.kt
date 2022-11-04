package ru.aasmc.petfinderapp.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.R
import ru.aasmc.petfinderapp.common.utils.createExceptionHandler
import ru.aasmc.petfinderapp.main.domain.usecases.OnboardingIsComplete
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val onboardingIsComplete: OnboardingIsComplete
) : ViewModel() {
    private val _viewEffect = MutableSharedFlow<MainActivityViewEffect>()
    val viewEffect: SharedFlow<MainActivityViewEffect> = _viewEffect.asSharedFlow()

    fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.DefineStartDestination -> defineStartDestination()
        }
    }

    private fun defineStartDestination() {
        val errorMessage = "Failed to check if onboarding is complete"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) {
            onFailure(it)
        }
        viewModelScope.launch(exceptionHandler) {
            val destination = if (onboardingIsComplete()) {
                ru.aasmc.petfinderapp.animalsnearyou.R.id.nav_animalsnearyou
            } else {
                ru.aasmc.petfinderapp.onboarding.R.id.nav_onboarding
            }
            _viewEffect.emit(MainActivityViewEffect.SetStartDestination(destination))
        }
    }

    private fun onFailure(throwable: Throwable) {
        // TODO handle failures
    }
}