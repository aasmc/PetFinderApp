package ru.aasmc.petfinderapp.sharing.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_GETTER
)
/**
 * This annotation allows us to create a Key out of each ViewModel.
 */
annotation class ViewModelKey(
    val value: KClass<out ViewModel>
)
