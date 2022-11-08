package ru.aasmc.petfinderapp.search.domain.usecases

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.search.domain.model.SearchParameters
import ru.aasmc.petfinderapp.search.domain.model.SearchResults
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchAnimals @Inject constructor(
    private val animalRepository: AnimalRepository
) {

    companion object {
        private const val UI_EMPTY_VALUE = "Any"
    }
    /**
     * The resulting [Flowable] emits new values every time one of the
     * [BehaviorSubject]s emits something new.
     */
    operator fun invoke(
        querySubject: BehaviorSubject<String>,
        ageSubject: BehaviorSubject<String>,
        typeSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {
        val query = querySubject
            // helps avoid reacting to every little change in the query.
            .debounce(500L, TimeUnit.MILLISECONDS)
            .map { it.trim() }
            .filter { it.length >= 2 }
        val age = ageSubject.replaceUIEmptyValue()
        val type = typeSubject.replaceUIEmptyValue()

        return Observable.combineLatest(query, age, type, combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .filter { it.name.isNotEmpty() }
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }


    private val combiningFunction: Function3<String, String, String, SearchParameters>
        get() = Function3 { query, search, type ->
            SearchParameters(query, search, type)
        }

    private fun BehaviorSubject<String>.replaceUIEmptyValue(): Observable<String> {
        return map { if (it == UI_EMPTY_VALUE) "" else it }
    }
}