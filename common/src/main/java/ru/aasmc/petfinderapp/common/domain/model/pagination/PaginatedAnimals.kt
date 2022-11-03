package ru.aasmc.petfinderapp.common.domain.model.pagination

import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails

data class PaginatedAnimals(
    val animals: List<AnimalWithDetails>,
    val pagination: Pagination
)
