package ru.aasmc.petfinderapp.search.presentation

import ru.aasmc.petfinderapp.common.presentation.Event
import ru.aasmc.petfinderapp.common.presentation.model.UIAnimal

data class SearchViewState(
    val noSearchQuery: Boolean = true,
    val searchResults: List<UIAnimal> = emptyList(),
    // The filters are modeled as Event's for performance reasons.
    // AutoCompleteTextView uses an Adapter - not the same Adapter we use with
    // RecyclerView - to display items. The simplest way to update that Adapter
    // is to create a new one with the updated data. Once we set the filters, the data
    // they display doesn't change. However, creating a new Adapter on each state
    // update is a waste of resources. Using the Event wrapper class, we ensure we only
    // create one Adapter for each filter.
    val ageFilterValues: Event<List<String>> = Event(emptyList()),
    val typeFilterValues: Event<List<String>> = Event(emptyList()),
    val searchingRemotely: Boolean = false,
    val noRemoteResults: Boolean = false,
    val failure: Event<Throwable>? = null
) {
    fun updateToReadyToSearch(ages: List<String>, types: List<String>): SearchViewState {
        return copy(
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types)
        )
    }

    fun updateToNoSearchQuery(): SearchViewState {
        return copy(
            noSearchQuery = true,
            searchResults = emptyList(),
            noRemoteResults = false
        )
    }

    fun updateToSearching(): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToSearchingRemotely(): SearchViewState {
        return copy(
            searchingRemotely = true,
            searchResults = emptyList()
        )
    }

    fun updateToHasSearchResults(animals: List<UIAnimal>): SearchViewState {
        return copy(
            noSearchQuery = false,
            searchResults = animals,
            searchingRemotely = false,
            noRemoteResults = false
        )
    }

    fun updateToNoResultsAvailable(): SearchViewState {
        return copy(
            searchingRemotely = false,
            noRemoteResults = true
        )
    }

    fun updateToHasFailure(throwable: Throwable): SearchViewState {
        return copy(failure = Event(throwable))
    }

    fun isInNoSearchResultsState(): Boolean {
        return noRemoteResults
    }
}