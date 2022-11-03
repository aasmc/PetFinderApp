package ru.aasmc.petfinderapp.common.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.testing.TestInstallIn
import io.reactivex.disposables.CompositeDisposable
import ru.aasmc.petfinderapp.common.data.FakeRepository
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.CoroutineDispatchersProvider
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [ActivityRetainedModule::class]
)
abstract class TestActivityRetainedModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindAnimalRepository(repository: FakeRepository): AnimalRepository

    @Binds
    abstract fun bindDispatchersProvider(dispatchersProvider: CoroutineDispatchersProvider): DispatchersProvider

    companion object {
        @Provides
        fun provideCompositeDisposable() = CompositeDisposable()
    }
}