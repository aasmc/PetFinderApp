package ru.aasmc.petfinderapp.common.data.cache.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.aasmc.petfinderapp.common.data.cache.model.cachedorganization.CachedOrganization

@Dao
interface OrganizationsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(organizations: List<CachedOrganization>)

    @Query("SELECT * FROM organizations WHERE organizationId IS :organizationId")
    suspend fun getOrganization(organizationId: String): CachedOrganization
}