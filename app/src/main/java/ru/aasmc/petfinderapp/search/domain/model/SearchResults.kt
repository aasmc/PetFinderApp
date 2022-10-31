package ru.aasmc.petfinderapp.search.domain.model

import ru.aasmc.petfinderapp.common.domain.model.animal.Animal

data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)
