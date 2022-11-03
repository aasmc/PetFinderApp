package ru.aasmc.petfinderapp.common.domain.model.organization

data class Organization(
    val id: String,
    val contact: Contact,
    val distance: Float
) {

    data class Contact(
        val email: String,
        val phone: String,
        val address: Address
    )

    data class Address(
        val address1: String,
        val address2: String,
        val city: String,
        val state: String,
        val postcode: String,
        val country: String
    )
}
