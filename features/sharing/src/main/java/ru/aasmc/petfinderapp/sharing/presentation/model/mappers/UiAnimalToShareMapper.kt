package ru.aasmc.petfinderapp.sharing.presentation.model.mappers

import ru.aasmc.petfinderapp.common.domain.model.animal.details.AnimalWithDetails
import ru.aasmc.petfinderapp.common.presentation.model.mappers.UiMapper
import ru.aasmc.petfinderapp.sharing.presentation.model.UIAnimalToShare
import javax.inject.Inject

class UiAnimalToShareMapper @Inject constructor() :
    UiMapper<AnimalWithDetails, UIAnimalToShare> {
    override fun mapToView(input: AnimalWithDetails): UIAnimalToShare {
        val message = createMessage(input)

        return UIAnimalToShare(input.media.getFirstSmallestAvailablePhoto(), message)
    }

    private fun createMessage(input: AnimalWithDetails): String {
        val contact = input.organizationContact
        val formattedAddress = contact.formattedAddress
        val formattedContactInfo = contact.formattedContactInfo

        return "${input.description}\n\nOrganization info:\n$formattedAddress\n\n$formattedContactInfo"
    }
}