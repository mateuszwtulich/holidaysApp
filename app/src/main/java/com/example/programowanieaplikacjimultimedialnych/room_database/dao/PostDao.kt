package com.example.programowanieaplikacjimultimedialnych.room_database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.programowanieaplikacjimultimedialnych.room_database.model.Post

@Dao
interface PostDao {

    @Query("Select * from post_table")
    fun getPosts(): LiveData<List<Post>>

    @Query("Select * from post_table where id=:postId")
    fun getPost(postId: Int): LiveData<Post>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(post: Post):Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("DELETE FROM post_table")
    suspend fun deleteAllPosts()
}