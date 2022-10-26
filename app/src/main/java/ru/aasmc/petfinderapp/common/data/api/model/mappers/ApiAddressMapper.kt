package ru.aasmc.petfinderapp.common.data.api.model.mappers

import ru.aasmc.petfinderapp.common.data.api.model.ApiAddress
import ru.aasmc.petfinderapp.common.domain.model.organizaton.Organization
import javax.inject.Inject

class ApiAddressMapper @Inject constructor()
    : ApiMapper<ApiAddress?, Organization.Address>{
    override fun mapToDomain(apiEntity: ApiAddress?): Organization.Address {
        return Organization.Address(
            address1 = apiEntity?.address1.orEmpty(),
            address2 = apiEntity?.address2.orEmpty(),
            city = apiEntity?.city.orEmpty(),
            state = apiEntity?.state.orEmpty(),
            postcode = apiEntity?.postcode.orEmpty(),
            country = apiEntity?.country.orEmpty()
        )
    }
}