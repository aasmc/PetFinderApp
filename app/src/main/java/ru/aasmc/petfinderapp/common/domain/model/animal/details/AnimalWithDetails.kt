package ru.aasmc.petfinderapp.common.domain.model.animal.details

import ru.aasmc.petfinderapp.common.domain.model.animal.AdoptionStatus
import ru.aasmc.petfinderapp.common.domain.model.animal.Animal
import ru.aasmc.petfinderapp.common.domain.model.animal.Media
import java.time.LocalDateTime

data class AnimalWithDetails(
    val id: Long,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
) {
    fun withNoDetails(): Animal {
        return Animal(id, name, type, media, tags, adoptionStatus, publishedAt)
    }
}
