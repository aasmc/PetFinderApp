package ru.aasmc.petfinderapp.animalsnearyou.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.animalsnearyou.domain.usecases.GetAnimals
import ru.aasmc.petfinderapp.animalsnearyou.domain.usecases.RequestNextPageOfAnimals
import ru.aasmc.petfinderapp.common.domain.model.NetworkException
import ru.aasmc.petfinderapp.common.domain.model.NetworkUnavailableException
import ru.aasmc.petfinderapp.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinderapp.common.domain.model.animal.Animal
import ru.aasmc.petfinderapp.common.domain.model.pagination.Pagination
import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimal
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinderapp.common.utils.createExceptionHandler
import ru.aasmc.petfinderapp.logging.Logger
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val getAnimals: GetAnimals,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val uiAnimalMapper: UiAnimalMapper,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

    companion object {
        const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
    }

    private val _state = MutableStateFlow(AnimalsNearYouViewState())
    private var currentPage = 0

    val state: StateFlow<AnimalsNearYouViewState> = _state.asStateFlow()
    var isLoadingMoreAnimals: Boolean = false
    var isLastPage = false

    init {
        subscribeToAnimalUpdates()
    }

    fun onEvent(event: AnimalsNearYouEvent) {
        when(event) {
            is AnimalsNearYouEvent.RequestMoreAnimals -> loadNextAnimalPage()
        }
    }

    private fun subscribeToAnimalUpdates() {
        getAnimals()
            .doOnNext { if (hasNoAnimalsStoredButCanLoadMore(it)) loadNextAnimalPage() }
            .map { animals -> animals.map { uiAnimalMapper.mapToView(it) } }
            .filter { it.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onNewAnimalList(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun hasNoAnimalsStoredButCanLoadMore(animals: List<Animal>): Boolean {
        return animals.isEmpty() && !state.value.noMoreAnimalsNearby
    }

    private fun onNewAnimalList(animals: List<UIAnimal>) {
        Logger.d("Got more animals!")

        // the API returns unordered pages. The item with ID 79 can appear on page 12,
        // while the item with ID 1000 can show up on the first page. Room returns the
        // elements ordered by their IDs. This means that on each update, we can have new
        // elements appearing amid old ones. This will cause some weird UI animations,
        // with items appearing out or nowhere. To work around it, you concatenate the
        // new list to the end of the current one, and convert the whole thing to a Set.
        // By definition, sets can't have repeated elements. This way, you'll get a nice
        // animation where new items appear below the old ones. Another possible fix
        // is to locally add something like an updatedAt field for each item, and use it
        // to order the list.
        val updatedAnimalSet = (state.value.animals + animals).toSet()

        _state.update { oldState ->
            oldState.copy(loading = false, animals = updatedAnimalSet.toList())
        }
    }

    private fun loadNextAnimalPage() {
        isLoadingMoreAnimals = true

        val errorMessage = "Failed to fetch nearby animals"
        val exceptionHandler = viewModelScope.createExceptionHandler(errorMessage) { onFailure(it) }

        viewModelScope.launch(exceptionHandler) {
            Logger.d("Requesting more animals.")
            val pagination = requestNextPageOfAnimals(++currentPage)

            onPaginationInfoObtained(pagination)
            isLoadingMoreAnimals = false
        }
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
        isLastPage = !pagination.canLoadMore
    }

    private fun onFailure(failure: Throwable) {
        when (failure) {
            is NetworkException,
            is NetworkUnavailableException -> {
                _state.update { oldState ->
                    oldState.copy(loading = false, failure = Event(failure))
                }
            }
            is NoMoreAnimalsException -> {
                _state.update { oldState ->
                    oldState.copy(noMoreAnimalsNearby = true, failure = Event(failure))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}