package ru.aasmc.petfinderapp.common.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import ru.aasmc.petfinderapp.common.data.api.PetFinderApi
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiAnimalMapper
import ru.aasmc.petfinderapp.common.data.api.model.mappers.ApiPaginationMapper
import ru.aasmc.petfinderapp.common.data.api.utils.FakeServer
import ru.aasmc.petfinderapp.common.data.cache.Cache
import ru.aasmc.petfinderapp.common.data.cache.PetSaveDatabase
import ru.aasmc.petfinderapp.common.data.cache.RoomCache
import ru.aasmc.petfinderapp.common.data.di.CacheModule
import ru.aasmc.petfinderapp.common.data.preferences.Preferences
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(
    CacheModule::class,
)
class PetFinderAnimalRepositoryTest {
    private val fakeServer = FakeServer()
    private lateinit var repository: AnimalRepository
    private lateinit var api: PetFinderApi
    private lateinit var cache: Cache

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: PetSaveDatabase

    @Inject
    lateinit var retrofitBuilder: Retrofit.Builder

    @Inject
    lateinit var apiAnimalMapper: ApiAnimalMapper

    @Inject
    lateinit var apiPaginationMapper: ApiPaginationMapper

    @Inject
    lateinit var preferences: Preferences

    @Before
    fun setup() {
        fakeServer.start()
        hiltRule.inject()

        preferences.deleteTokenInfo()
        preferences.putToken("validToken")
        preferences.putTokenExpirationTime(Instant.now().plusSeconds(3600).epochSecond)
        preferences.putTokenType("Bearer")
        preferences.putPostcode("09097")
        preferences.putMaxDistanceAllowedToGetAnimals(100)

        api = retrofitBuilder
            .baseUrl(fakeServer.baseEndpoint)
            .build()
            .create(PetFinderApi::class.java)

        cache = RoomCache(database.animalsDao(), database.organizationsDao())

        repository = PetFinderAnimalRepository(
            api,
            cache,
            preferences,
            apiAnimalMapper,
            apiPaginationMapper
        )
    }

    @After
    fun tearDown() {
        fakeServer.shutdown()
    }

    @Test
    fun requestMoreAnimals_success() = runTest {
        // Given
        val expectedAnimalId = 124L
        fakeServer.setHappyPathDispatcher()

        // When
        val paginatedAnimals =
            repository.requestMoreAnimals(pageToLoad = 1, numberOfItems = 100)

        // Then
        val animal = paginatedAnimals.animals.first()
        Truth.assertThat(animal.id).isEqualTo(expectedAnimalId)
    }

    @Test
    fun insertAnimals_success() = runTest {
        // Given
        val expectedAnimalId = 124L
        fakeServer.setHappyPathDispatcher()
        val paginatedAnimals = repository.requestMoreAnimals(
            pageToLoad = 1,
            numberOfItems = 100
        )
        val animal = paginatedAnimals.animals.first()
        // When
        repository.storeAnimals(listOf(animal))
        advanceUntilIdle()
        // Then
        val testObserver = repository.getAnimals().test()

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        testObserver.assertValue { it.first().id == expectedAnimalId }
    }
}
