package ru.aasmc.petfinderapp.common.domain.model.animal.details

import ru.aasmc.petfinderapp.common.domain.model.organization.Organization

data class Details(
    val description: String,
    val age: Age,
    val species: String,
    val breed: Breed,
    val colors: Colors,
    val gender: Gender,
    val size: Size,
    val coat: Coat,
    val healthDetails: HealthDetails,
    val habitatAdaptation: HabitatAdaptation,
    val organization: Organization
) {
    val organizationContact: Organization.Contact = organization.contact
}
