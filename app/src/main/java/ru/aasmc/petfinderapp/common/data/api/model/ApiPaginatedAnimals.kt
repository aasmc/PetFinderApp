package ru.aasmc.petfinderapp.common.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiPaginatedAnimals(
    @field:Json(name = "animals") val animals: List<ApiAnimal>?,
    @field:Json(name = "pagination") val pagination: ApiPagination?
)

@JsonClass(generateAdapter = true)
data class ApiPagination(
    @field:Json(name = "count_per_page") val countPerPage: Int?,
    @field:Json(name = "total_count") val totalCount: Int?,
    @field:Json(name = "current_page") val currentPage: Int?,
    @field:Json(name = "total_pages") val totalPages: Int?
)