package ru.aasmc.petfinderapp.common.data.api.model.mappers

import ru.aasmc.petfinderapp.common.data.api.model.ApiContact
import ru.aasmc.petfinderapp.common.domain.model.organizaton.Organization
import javax.inject.Inject

class ApiContactMapper @Inject constructor(
    private val apiAddressMapper: ApiAddressMapper
): ApiMapper<ApiContact?, Organization.Contact> {

    override fun mapToDomain(apiEntity: ApiContact?): Organization.Contact {
        return Organization.Contact(
            email = apiEntity?.email.orEmpty(),
            phone = apiEntity?.phone.orEmpty(),
            address = apiAddressMapper.mapToDomain(apiEntity?.address)
        )
    }
}