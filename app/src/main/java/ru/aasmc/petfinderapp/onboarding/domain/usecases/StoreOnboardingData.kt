package ru.aasmc.petfinderapp.onboarding.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import javax.inject.Inject

class StoreOnboardingData @Inject constructor(
    private val repository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {

    suspend operator fun invoke(postcode: String, distance: String) {
        withContext(dispatchersProvider.io()) {
            repository.storeOnboardingData(postcode, distance.toInt())
        }
    }
}