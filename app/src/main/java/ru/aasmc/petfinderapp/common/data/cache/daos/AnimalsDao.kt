package ru.aasmc.petfinderapp.common.data.cache.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedAnimalWithDetails
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedPhoto
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedTag
import ru.aasmc.petfinderapp.common.data.cache.model.cachedanimal.CachedVideo

@Dao
abstract class AnimalsDao {
    /**
     * Room uses a buffer for table row data, CursorWindow. If a query result is too large
     * this buffer can overflow, resulting in corrupted data. Using @Transaction avoids
     * this. It also ensures you get consistent results when you query different
     * tables for a single result.
     *
     * @return a [Flowable] of a list of [CachedAnimalAggregate]. This is a stream
     *         that will infinitely emit new updates.
     */
    @Transaction
    @Query("SELECT * FROM animals")
    abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

    /**
     * No need to add @Transaction here, because [Room] performs inserts in transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAnimalAggregate(
        animal: CachedAnimalWithDetails,
        photos: List<CachedPhoto>,
        videos: List<CachedVideo>,
        tags: List<CachedTag>
    )

    suspend fun insertAnimalsWithDetails(
        animalAggregates: List<CachedAnimalAggregate>
    ) {
        // Each iteration of the loop will trigger the Flowable from getAllAnimals()
        // In the worst case it can cause some backpressure in the stream. This isn't
        // a problem - Room's backpressure strategy keeps only the latest event
        // which is what we want in the end.
        for (animalAggregate in animalAggregates) {
            insertAnimalAggregate(
                animalAggregate.animal,
                animalAggregate.photos,
                animalAggregate.videos,
                animalAggregate.tags
            )
        }
    }

}

















