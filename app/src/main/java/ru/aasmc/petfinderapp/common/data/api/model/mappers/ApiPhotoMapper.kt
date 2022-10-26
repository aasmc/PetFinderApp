package ru.aasmc.petfinderapp.common.data.api.model.mappers

import ru.aasmc.petfinderapp.common.data.api.model.ApiPhotoSizes
import ru.aasmc.petfinderapp.common.domain.model.animal.Media
import javax.inject.Inject

class ApiPhotoMapper @Inject constructor(): ApiMapper<ApiPhotoSizes?, Media.Photo> {

    override fun mapToDomain(apiEntity: ApiPhotoSizes?): Media.Photo {
        return Media.Photo(
            medium = apiEntity?.medium.orEmpty(),
            full = apiEntity?.full.orEmpty()
        )
    }
}
