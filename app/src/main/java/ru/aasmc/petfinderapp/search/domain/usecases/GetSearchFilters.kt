package ru.aasmc.petfinderapp.search.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.model.MenuValueException
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Age
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.domain.model.animal.details.Details
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
        const val DEFAULT_VALUE = "Any"
        private const val DEFAULT_VALUE_LOWERCASE = "any"

    }

    suspend operator fun invoke(): SearchFilters {
        return withContext(dispatchersProvider.io()) {
            val types = animalRepository.getAnimalTypes()
            val filteringValues =
                if (types.any { it.lowercase(Locale.ROOT) == DEFAULT_VALUE_LOWERCASE }) {
                    types
                } else {
                    listOf(DEFAULT_VALUE) + types
                }
            if (types.isEmpty()) throw MenuValueException("No animal types")

            val ages = animalRepository.getAnimalAges()
                .map { it.name }
                .replace(Age.UNKNOWN.name, DEFAULT_VALUE)
                .map {
                    it.lowercase(Locale.ROOT).replaceFirstChar { ch ->
                        if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
                    }
                }

            return@withContext SearchFilters(ages, types)
        }
    }

    private fun List<String>.replace(old: String, new: String): List<String> {
        return map { if (it == old) new else it }
    }
}