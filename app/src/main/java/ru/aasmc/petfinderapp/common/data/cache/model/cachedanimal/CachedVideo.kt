package ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.aasmc.petfinderapp.common.domain.model.animal.Media

@Entity(tableName = "videos")
data class CachedVideo(
    @PrimaryKey(autoGenerate = true)
    val videoId: Long = 0,
    val animalId: Long,
    val video: String
) {
    companion object {
        fun fromDomain(animalId: Long, video: Media.Video): CachedVideo {
            return CachedVideo(animalId = animalId, video = video.video)
        }
    }

    fun toDomain(): Media.Video = Media.Video(video)
}
