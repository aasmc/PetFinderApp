package ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails.model.mapper

import ru.aasmc.petfinderapp.animalsnearyou.presentation.animaldetails.model.UIAnimalDetailed
import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiMapper
import javax.inject.Inject

class UiAnimalDetailsMapper @Inject constructor() :
    UiMapper<AnimalWithDetails, UIAnimalDetailed> {
    override fun mapToView(input: AnimalWithDetails): UIAnimalDetailed =
        UIAnimalDetailed(
            id = input.id,
            name = input.name,
            photo = input.media.getFirstSmallestAvailablePhoto(),
            description = input.details.description,
            sprayNeutered = input.details.healthDetails.isSpayedOrNeutered,
            specialNeeds = input.details.healthDetails.hasSpecialNeeds,
            tags = input.tags,
            phone = input.details.organization.contact.phone
        )
}