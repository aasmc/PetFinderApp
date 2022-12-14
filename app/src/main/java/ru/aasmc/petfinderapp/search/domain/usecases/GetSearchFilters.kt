package ru.aasmc.petfinderapp.search.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Age
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import ru.aasmc.petfinderapp.search.domain.model.SearchFilters
import java.util.*
import javax.inject.Inject

class GetSearchFilters @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    companion object {
        /**
         * Default value for all [SearchFilters].
         */
        const val NO_FILTER_SELECTED = "Any"
    }

    suspend operator fun invoke(): SearchFilters {
        return withContext(dispatchersProvider.io()) {
            val unknown = Age.UNKNOWN.name
            val types = listOf(NO_FILTER_SELECTED) + animalRepository.getAnimalTypes()

            val ages = animalRepository.getAnimalAges()
                .map { age ->
                    if (age.name == unknown) {
                        NO_FILTER_SELECTED
                    } else {
                        age.name
                            .uppercase()
                            .replaceFirstChar { firstChar ->
                                if (firstChar.isLowerCase()) {
                                    firstChar.titlecase(Locale.ROOT)
                                } else {
                                    firstChar.toString()
                                }
                            }
                    }
                }
            return@withContext SearchFilters(ages, types)
        }
    }
}