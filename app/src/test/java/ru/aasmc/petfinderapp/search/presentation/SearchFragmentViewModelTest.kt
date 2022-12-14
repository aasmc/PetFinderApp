package ru.aasmc.petfinderapp.search.presentation

import com.google.common.truth.Truth.assertThat
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.aasmc.petfinderapp.RxImmediateSchedulerRule
import ru.aasmc.petfinderapp.TestCoroutineRule
import ru.aasmc.petfinderapp.common.data.FakeRepository
import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiAnimalMapper
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import ru.aasmc.petfinderapp.search.domain.usecases.GetSearchFilters
import ru.aasmc.petfinderapp.search.domain.usecases.SearchAnimals
import ru.aasmc.petfinderapp.search.domain.usecases.SearchAnimalsRemotely

class SearchFragmentViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var viewModel: SearchFragmentViewModel
    private lateinit var repository: FakeRepository
    private lateinit var getSearchFilters: GetSearchFilters

    private val uiAnimalsMapper = UiAnimalMapper()

    @Before
    fun setup() {
        val dispatchersProvider = object : DispatchersProvider {
            override fun io(): CoroutineDispatcher = testCoroutineRule.testDispatcher
        }

        repository = FakeRepository()
        getSearchFilters = GetSearchFilters(repository, dispatchersProvider)

        viewModel = SearchFragmentViewModel(
            uiAnimalsMapper,
            getSearchFilters,
            SearchAnimals(repository),
            SearchAnimalsRemotely(repository, dispatchersProvider),
            CompositeDisposable()
        )
    }

    @Test
    fun `SearchFragmentViewModel remote search with success`() = runTest {
        // Given
        val (name, age, type) = repository.remotelySearchableAnimal
        val (ages, types) = getSearchFilters()

        val expectedRemoteAnimals = repository.remoteAnimals.map {
            uiAnimalsMapper.mapToView(it)
        }

        val expectedViewState = SearchViewState(
            noSearchQuery = false,
            searchResults = expectedRemoteAnimals,
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types),
            searchingRemotely = false,
            noRemoteResults = false
        )
        // When
        viewModel.onEvent(SearchEvent.PrepareForSearch)
        viewModel.onEvent(SearchEvent.TypeValueSelected(type))
        viewModel.onEvent(SearchEvent.AgeValueSelected(age))
        viewModel.onEvent(SearchEvent.QueryInput(name))

        // Then
        val viewState = viewModel.state.value
        assertThat(viewState).isEqualTo(expectedViewState)
    }

}

























