package ru.aasmc.petfinderapp.sharing.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap
import ru.aasmc.petfinderapp.common.data.PetFinderAnimalRepository
import ru.aasmc.petfinderapp.common.domain.repositories.AnimalRepository
import ru.aasmc.petfinderapp.common.utils.CoroutineDispatchersProvider
import ru.aasmc.petfinderapp.common.utils.DispatchersProvider
import ru.aasmc.petfinderapp.sharing.presentation.SharingFragmentViewModel

@Module
@DisableInstallInCheck
abstract class SharingModule {

    @Binds
    abstract fun bindDispatchersProvider(
        dispatchersProvider: CoroutineDispatchersProvider
    ): DispatchersProvider

    @Binds
    abstract fun bindRepository(repository: PetFinderAnimalRepository): AnimalRepository

    @Binds
    @IntoMap
    @ViewModelKey(SharingFragmentViewModel::class)
    abstract fun bindSharingFragmentViewModel(
        sharingFragmentViewModel: SharingFragmentViewModel
    ): ViewModel

    @Binds
    @Reusable
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}