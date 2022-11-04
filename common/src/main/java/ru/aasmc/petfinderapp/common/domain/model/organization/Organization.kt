package ru.aasmc.petfinderapp.common.domain.model.organization

import java.lang.StringBuilder

data class Organization(
    val id: String,
    val contact: Contact,
    val distance: Float
) {

    data class Contact(
        val email: String,
        val phone: String,
        val address: Address
    ) {
        val formattedAddress = address.createFormattedAddress()
        val formattedContactInfo: String = createFormattedContactInfo()

        private fun createFormattedContactInfo(): String {
            val builder = StringBuilder()
            if (email.isNotEmpty()) {
                builder.append("Email: ").append(email)
            }
            if (phone.isNotEmpty()) {
                if (email.isNotEmpty()) {
                    builder.append("\n")
                }

                builder.append("Phone: ").append(phone)
            }

            return builder.toString()
        }
    }

    data class Address(
        val address1: String,
        val address2: String,
        val city: String,
        val state: String,
        val postcode: String,
        val country: String
    ) {
        fun createFormattedAddress(): String {
            val detailsSeparator = " "
            val newLineSeparator = "\n"
            val builder = StringBuilder()

            if (address1.isNotEmpty()) builder.append(address1)

            if (address2.isNotEmpty()) {
                if (address1.isNotEmpty()) {
                    builder.append(newLineSeparator)
                }

                builder.append(address2)
            }

            builder
                .append(city)
                .append(detailsSeparator)
                .append(state)
                .append(detailsSeparator)
                .append(postcode)
                .append(newLineSeparator)
                .append(country)

            return builder.toString()
        }
    }
}
