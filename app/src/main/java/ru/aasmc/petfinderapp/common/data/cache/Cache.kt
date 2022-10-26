package ru.aasmc.petfinderapp.common.data.cache

import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization

interface Cache {
    fun storeOrganizations(organizations: List<CachedOrganization>)
}