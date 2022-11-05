package ru.aasmc.petfinderapp.sharing.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.aasmc.petfinderapp.di.SharingModuleDependencies
import ru.aasmc.petfinderapp.sharing.presentation.SharingFragment

/**
 * Passing SharingModuleDependencies as the dependency of component
 * lets us connect it to Hilt's dependency graph.
 */
@Component(
    dependencies = [SharingModuleDependencies::class],
    modules = [SharingModule::class]
)
interface SharingComponent {
    fun inject(fragment: SharingFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun moduleDependencies(sharingModuleDependencies: SharingModuleDependencies): Builder
        fun build(): SharingComponent
    }
}