package ru.aasmc.petfinderapp.animalsnearyou.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.aasmc.petfinderapp.common.domain.usecases.GetAnimals
import ru.aasmc.petfinderapp.common.domain.usecases.RequestNextPageOfAnimals
import ru.aasmc.petfinderapp.common.domain.model.NetworkException
import ru.aasmc.petfinderapp.common.domain.model.NetworkUnavailableException
import ru.aasmc.petfinderapp.common.domain.model.NoMoreAnimalsException
import ru.aasmc.petfinderapp.common.domain.model.pagination.Pagination
import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimal
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import ru.aasmc.petfinderapp.common.utils.createExceptionHandler
import ru.aasmc.petfinderapp.logging.Logger
import javax.inject.Inject

@HiltViewModel
class AnimalsNearYouFragmentViewModel @Inject constructor(
    private val uiAnimalMapper: UiAnimalMapper,
    private val compositeDisposable: CompositeDisposable,
    private val requestNextPageOfAnimals: RequestNextPageOfAnimals,
    private val getAnimals: GetAnimals,
) : ViewModel() {

    companion object {
        const val UI_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE
    }

    private val _state =
        MutableStateFlow(AnimalsNearYouViewState())
    val state: StateFlow<AnimalsNearYouViewState> = _state.asStateFlow()

    val isLastPage: Boolean
        get() = state.value.noMoreAnimalsNearby

    var isLoadingMoreAnimals: Boolean = false
        private set

    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setIsLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.update { loggedIn }
    }

    private var currentPage = 0

    init {
        subscribeToAnimalUpdates()
    }

    fun onEvent(event: AnimalsNearYouEvent) {
        when (event) {
            AnimalsNearYouEvent.LoadAnimals -> loadAnimals()
        }
    }

    private fun subscribeToAnimalUpdates() {
        getAnimals()
            .map { animals ->
                animals.map {
                    uiAnimalMapper.mapToView(it)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onNewAnimalList(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun onNewAnimalList(animals: List<UIAnimal>) {
        Logger.d("Got more animals! $animals")

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
            oldState.copy(
                loading = false,
                animals = updatedAnimalSet.toList()
            )
        }
    }

    private fun loadAnimals() {
        // checks if the state already has animals. Fragment will send the RequestInitialAnimalsList
        // event every time it's created. Without this condition, we'd make a request
        // every time the configuration changes. This way we avoid making unnecessary
        // API requests.
        if (state.value.animals.isEmpty()) {
            loadNextAnimalPage()
        }
    }

    private fun loadNextAnimalPage() {
        isLoadingMoreAnimals = true
        val errorMessage = "Failed to fetch nearby animals"
        val exceptionHandler =
            viewModelScope.createExceptionHandler(errorMessage) {
                onFailure(it)
            }
        viewModelScope.launch(exceptionHandler) {
            Logger.d("Requesting more animals.")
            val pagination = requestNextPageOfAnimals(++currentPage)

            onPaginationInfoObtained(pagination)
            isLoadingMoreAnimals = false
        }
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onFailure(failure: Throwable) {
        when (failure) {
            is NetworkException,
            is NetworkUnavailableException -> {
                // update is a thread-safe method that updates the state
                // of the StateFlow using CAS operation
                _state.update { oldState ->
                    oldState.copy(
                        loading = false,
                        failure = Event(failure)
                    )
                }
            }
            is NoMoreAnimalsException -> {
                _state.update { oldState ->
                    oldState.copy(
                        noMoreAnimalsNearby = true,
                        failure = Event(failure)
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}