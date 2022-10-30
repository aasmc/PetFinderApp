package ru.aasmc.petfinderapp.common.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import io.reactivex.disposables.CompositeDisposable
import ru.aasmc.petfinderapp.common.data.PetFinderAnimalRepository
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.CoroutineDispatchersProvider
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class ActivityRetainedModule {
    @Binds
    abstract fun bindDispatchersProvider(dispatchersProvider: CoroutineDispatchersProvider):
            DispatchersProvider

    @Binds
    abstract fun bindAnimalRepository(impl: PetFinderAnimalRepository): AnimalRepository

    companion object {
        @Provides
        fun provideCompositeDisposable() = CompositeDisposable()
    }
}