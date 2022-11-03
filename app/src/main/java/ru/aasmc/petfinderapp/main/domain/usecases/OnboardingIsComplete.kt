package ru.aasmc.petfinderapp.main.domain.usecases

import kotlinx.coroutines.withContext
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import javax.inject.Inject

class OnboardingIsComplete @Inject constructor(
    private val repository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {
    suspend operator fun invoke() = withContext(dispatchersProvider.io()) {
        repository.onboardingIsComplete()
    }
}