package ru.aasmc.petfinderapp.common.domain.model.animal

data class Media(
    val photos: List<Photo>,
    val videos: List<Video>
) {
    companion object {
        const val EMPTY_MEDIA = ""
    }

    fun getFirstSmallestAvailablePhoto(): String =
        if (photos.isEmpty()) {
            EMPTY_MEDIA
        } else {
            photos.first().getSmallestAvailablePhoto()
        }

    data class Photo(
        val medium: String,
        val full: String
    ) {
        companion object {
            const val EMPTY_PHOTO = ""
        }

        fun getSmallestAvailablePhoto(): String = when {
            isValidPhoto(medium) -> medium
            isValidPhoto(full) -> full
            else -> EMPTY_PHOTO
        }

        private fun isValidPhoto(photo: String): Boolean {
            return photo.isNotEmpty()
        }
    }

    data class Video(val video: String)
}
