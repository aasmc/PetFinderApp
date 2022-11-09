package ru.aasmc.petfinderapp.details.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiAnimalDetailsMapper
import ru.aasmc.petfinderapp.details.domain.usecases.AnimalDetails
import java.sql.Time
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AnimalDetailsViewModel @Inject constructor(
    private val uiAnimalDetailsMapper: UiAnimalDetailsMapper,
    private val animalDetails: AnimalDetails,
    private val compositeDisposable: CompositeDisposable
) : ViewModel() {
    private val _state: MutableStateFlow<AnimalDetailsViewState> = MutableStateFlow(
        AnimalDetailsViewState.Loading
    )
    val state: StateFlow<AnimalDetailsViewState> = _state.asStateFlow()

    fun onEvent(event: AnimalDetailsEvent) {
        when (event) {
            is AnimalDetailsEvent.LoadAnimalDetails -> subscribeToAnimalDetails(event.animalId)
            AnimalDetailsEvent.AdoptAnimal -> adoptAnimal()
        }
    }

    private fun subscribeToAnimalDetails(animalId: Long) {
        animalDetails(animalId)
            .delay(2L, TimeUnit.SECONDS) // for educational purposes
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onAnimalDetails(it) },
                { onFailure(it) }
            ).addTo(compositeDisposable)
    }

    private fun onAnimalDetails(animal: AnimalWithDetails) {
        val animalDetails = uiAnimalDetailsMapper.mapToView(animal)
        _state.update {
            AnimalDetailsViewState.AnimalDetails(animalDetails)
        }
    }

    private fun adoptAnimal() {
        compositeDisposable.add(
            Observable.timer(2L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    _state.update { oldState ->
                        (oldState as? AnimalDetailsViewState.AnimalDetails)?.copy(adopted = true)
                            ?: oldState
                    }
                }
        )
    }

    private fun onFailure(failure: Throwable) {
        _state.update {
            AnimalDetailsViewState.Failure
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}