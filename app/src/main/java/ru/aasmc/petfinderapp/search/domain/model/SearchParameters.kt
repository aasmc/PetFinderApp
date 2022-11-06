package ru.aasmc.petfinderapp.search.domain.model

import java.util.*

/**
 * Value object that models the search parameters.
 * It is used to search the cache and to propagate the search
 * parameters to a remote search, in case nothing in the cache
 * matches.
 */
data class SearchParameters(
    val name: String,
    val age: String,
    val type: String
) {
    val uppercaseName get() = name.uppercase(Locale.ROOT)
    val uppercaseAge get() = age.uppercase(Locale.ROOT)
    val uppercaseType get() = type.uppercase(Locale.ROOT)
}
