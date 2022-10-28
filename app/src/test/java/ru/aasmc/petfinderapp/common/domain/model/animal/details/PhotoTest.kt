package ru.aasmc.petfinderapp.common.domain.model.animal.details

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.aasmc.petfinderapp.common.domain.model.animal.Media

class PhotoTest {
    private val mediumPhoto = "mediumPhoto"
    private val fullPhoto = "fullPhoto"
    private val invalidPhoto = ""

    @Test
    fun photo_getSmallestAvailablePhoto_hasMediumPhoto() {
        // given
        val photo = Media.Photo(mediumPhoto, fullPhoto)
        val expected = mediumPhoto

        // when
        val actual = photo.getSmallestAvailablePhoto()

        // then
        assertEquals(expected, actual)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noMediumPhoto_hasFullPhoto() {
        val photo = Media.Photo(invalidPhoto, fullPhoto)
        val expected = fullPhoto

        val actual = photo.getSmallestAvailablePhoto()

        assertEquals(expected, actual)
    }

    @Test
    fun photo_getSmallestAvailablePhoto_noPhotos() {
        val photo = Media.Photo(invalidPhoto, invalidPhoto)
        val expected = Media.Photo.EMPTY_PHOTO

        val actual = photo.getSmallestAvailablePhoto()

        assertEquals(expected, actual)
    }
}