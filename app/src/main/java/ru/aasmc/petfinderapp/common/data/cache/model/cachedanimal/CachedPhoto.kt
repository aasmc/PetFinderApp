package ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.aasmc.petfinderapp.common.domain.model.animal.Media

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = CachedAnimalWithDetails::class,
            parentColumns = ["animalId"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // if we don't set a foreign key as an index, changing the parent table might
    // trigger an unneeded full table scan on the child table, which slows the app down.
    // Fortunately, Room throws a compile-time warning if we don't index the key.
    // Having indices speeds up SELECT queries. On the other hand, it slows down
    // INSERTs and UPDATEs. This app will mostly read from the database.
    indices = [Index("animalId")]
)
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

            return CachedPhoto(animalId = animalId, medium = medium, full = full)
        }
    }

    fun toDomain(): Media.Photo = Media.Photo(medium, full)
}