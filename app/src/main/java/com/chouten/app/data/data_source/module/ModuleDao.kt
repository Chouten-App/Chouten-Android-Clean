package com.chouten.app.data.data_source.module

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chouten.app.domain.model.ModuleModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM ModuleModel")
    fun getModules(): Flow<List<ModuleModel>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addModule(moduleModel: ModuleModel)

    @Update
    suspend fun updateModule(moduleModel: ModuleModel)

    @Query("DELETE FROM ModuleModel WHERE id = :moduleId")
    suspend fun removeModule(moduleId: String)

    @Delete
    suspend fun removeModule(moduleModel: ModuleModel)
}