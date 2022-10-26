package ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.aasmc.petfinderapp.common.domain.model.animal.Media

@Entity(tableName = "photos")
data class CachedPhoto(
    @PrimaryKey(autoGenerate = true)
    val photoId: Long = 0,
    val animalId: Long,
    val medium: String,
    val full: String
) {
    companion object {
        fun fromDomain(animalId: Long, photo: Media.Photo): CachedPhoto {
            val (medium, full) = photo

            return CachedPhoto(animalId, animalId, medium, full)
        }
    }

    fun toDomain(): Media.Photo = Media.Photo(medium, full)
}