package com.example.programowanieaplikacjimultimedialnych.view_model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoInput
import com.example.programowanieaplikacjimultimedialnych.repository.HolidayRepository
import com.example.programowanieaplikacjimultimedialnych.room_database.HolidayRoomDatabase
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput

// Class extends AndroidViewModel and requires application as a parameter.
class HolidayViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: HolidayRepository
    val allPosts: LiveData<List<PostDtoOutput>>

    init {
        // Gets reference to WordDao from HolidayRoomDatabase to construct
        // the correct WordRepository.
        val postDao = HolidayRoomDatabase.getDatabase(application).postDao()
        val multimediaPathDao = HolidayRoomDatabase.getDatabase(application).multimediaPathDao()
        repository = HolidayRepository(postDao, multimediaPathDao)
        allPosts = getPosts()
    }

    fun getPost(postId: Int): LiveData<PostDtoOutput> = repository.getPost(postId)

    fun getPosts(): LiveData<List<PostDtoOutput>> = repository.getPosts()

    suspend fun insert(postDto: PostDtoInput) = repository.insertPost(postDto)

    suspend fun deletePost(post: PostDtoOutput) = repository.deletePost(post)

    suspend fun deleteAllPosts() = repository.deleteAllPost()

    suspend fun update(post: PostDtoInput) = repository.update(post)
}
