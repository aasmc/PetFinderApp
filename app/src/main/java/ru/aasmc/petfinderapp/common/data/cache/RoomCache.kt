package ru.aasmc.petfinderapp.common.data.cache

import ru.aasmc.petfinderapp.common.data.cache.daos.OrganizationsDao
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization
import javax.inject.Inject

class RoomCache @Inject constructor(
    private val organizationsDao: OrganizationsDao
) : Cache {
    override fun storeOrganizations(organizations: List<CachedOrganization>) {
        organizationsDao.insert(organizations)
    }
}