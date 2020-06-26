package com.example.programowanieaplikacjimultimedialnych.room_database.dao

import androidx.room.*
import com.example.programowanieaplikacjimultimedialnych.room_database.model.MultimediaPath

@Dao
interface MultimediaPathDao {
    @Query("Select * from multimediaPath_table where post_id = :postId")
    suspend fun getMultimediaPaths(postId: Int): List<MultimediaPath>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(path: MultimediaPath)

    @Update
    suspend fun updatePath(path: MultimediaPath)

    @Delete
    suspend fun deletePath(path: MultimediaPath)

    @Query("DELETE FROM multimediaPath_table")
    suspend fun deleteAllPaths()
}