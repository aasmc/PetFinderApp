package ru.aasmc.petfinderapp.common.domain.model.search

import ru.aasmc.petfinderapp.common.domain.model.animal.Animal

data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)
