package ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.aasmc.petfinderapp.common.domain.model.organizaton.Organization

@Entity(tableName = "organizations")
data class CachedOrganization(
    @PrimaryKey(autoGenerate = false)
    val organizationId: String,
    val email: String,
    val phone: String,
    val address1: String,
    val address2: String,
    val city: String,
    val state: String,
    val postcode: String,
    val country: String,
    val distance: Float
) {
    companion object {
        fun fromDomain(domainModel: Organization): CachedOrganization {
            val contact = domainModel.contact
            val address = contact.address

            return CachedOrganization(
                organizationId = domainModel.id,
                email = contact.email,
                phone = contact.phone,
                address1 = address.address1,
                address2 = address.address2,
                city = address.city,
                state = address.state,
                postcode = address.postcode,
                country = address.country,
                distance = domainModel.distance
            )
        }
    }

    fun toDomain(): Organization {
        return Organization(
            organizationId,
            Organization.Contact(
                email,
                phone,
                Organization.Address(address1, address2, city, state, postcode, country)
            ),
            distance
        )
    }
}
