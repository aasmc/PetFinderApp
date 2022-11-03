package ru.aasmc.petfinderapp.search.domain.usecases

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import ru.aasmc.petfinderapp.common.domain.model.search.SearchParameters
import ru.aasmc.petfinderapp.common.domain.model.search.SearchResults
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.functions.Function3
import java.util.*


class SearchAnimals @Inject constructor(private val animalRepository: AnimalRepository) {

    private val combiningFunction: Function3<String, String, String, SearchParameters>
        get() = Function3 { query, age, type ->
            SearchParameters(query, age, type)
        }

    operator fun invoke(
        querySubject: BehaviorSubject<String>,
        ageSubject: BehaviorSubject<String>,
        typeSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {

        val query = querySubject
            .debounce(500L, TimeUnit.MILLISECONDS)
            .map { it.trim() }
            .filter { it.length >= 2 }

        val age = ageSubject.replaceUIEmptyValue().map {
            it.lowercase()
                .replaceFirstChar { firstChar ->
                    if (firstChar.isLowerCase()) {
                        firstChar.titlecase(Locale.ROOT)
                    } else {
                        firstChar.toString()
                    }
                }
        }
        val type = typeSubject.replaceUIEmptyValue()

        return Observable.combineLatest(query, age, type, combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private fun BehaviorSubject<String>.replaceUIEmptyValue() = map {
        if (it == GetSearchFilters.NO_FILTER_SELECTED) "" else it
    }
}