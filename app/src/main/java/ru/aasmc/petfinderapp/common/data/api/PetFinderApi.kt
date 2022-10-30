package ru.aasmc.petfinderapp.common.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.aasmc.petfinderapp.common.data.api.model.ApiPaginatedAnimals

interface PetFinderApi {
    @GET(ApiConstants.ANIMALS_ENDPOINT)
    suspend fun getNearbyAnimals(
        @Query(ApiParameters.PAGE) pageToLoad: Int,
        @Query(ApiParameters.LIMIT) pageSize: Int,
        @Query(ApiParameters.LOCATION) postcode: String,
        @Query(ApiParameters.DISTANCE) maxDistance: Int
    ): ApiPaginatedAnimals

    @GET(ApiConstants.ANIMALS_ENDPOINT)
    suspend fun searchAnimalsBy(
        @Query(ApiParameters.NAME) name: String,
        @Query(ApiParameters.AGE) age: String,
        @Query(ApiParameters.TYPE) type: String,
        @Query(ApiParameters.PAGE) pageToLoad: Int,
        @Query(ApiParameters.LIMIT) pageSize: Int,
        @Query(ApiParameters.LOCATION) postcode: String,
        @Query(ApiParameters.DISTANCE) maxDistance: Int
    ): ApiPaginatedAnimals
}