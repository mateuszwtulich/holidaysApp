package com.example.programowanieaplikacjimultimedialnych.repository

import android.net.Uri
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.programowanieaplikacjimultimedialnych.room_database.dao.MultimediaPathDao
import com.example.programowanieaplikacjimultimedialnych.room_database.dao.PostDao
import com.example.programowanieaplikacjimultimedialnych.room_database.model.MultimediaPath
import com.example.programowanieaplikacjimultimedialnych.room_database.model.Post
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.Location
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoInput
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HolidayRepository(private val postDao: PostDao, private val multimediaPathDao: MultimediaPathDao) {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.


    fun getPosts(): LiveData<List<PostDtoOutput>> = Transformations.map(postDao.getPosts()) { postsList ->
        postsList.map { post ->
            PostDtoOutput(
                post.id,
                post.title,
                post.text,
                Location(post.latitude, post.longitude),
                LocalDate.parse(post.date, formatter),
                runBlocking {
                    multimediaPathDao.getMultimediaPaths(post.id)
                        .map { multimediaPath -> Uri.parse(multimediaPath.path) }
                })
        }
    }

    fun getPost(postId: Int): LiveData<PostDtoOutput> = Transformations.map(postDao.getPost(postId)) { post ->
        PostDtoOutput(
            post.id,
            post.title,
            post.text,
            Location(post.latitude, post.longitude),
            LocalDate.parse(post.date, formatter),
            runBlocking {
                multimediaPathDao.getMultimediaPaths(post.id).map { multimediaPath -> Uri.parse(multimediaPath.path) }
            })
    }

    fun insertPost(postDto: PostDtoInput) = runBlocking {
        val post = Post(
            0,
            postDto.title,
            postDto.text,
            postDto.date.format(formatter),
            postDto.location.latitude,
            postDto.location.longitude
        )
        val id = postDao.insert(post).toInt()
        postDto.uriList.forEach { path -> multimediaPathDao.insert(MultimediaPath(0, path, id)) }
    }

    suspend fun deletePost(post: PostDtoOutput) {
        postDao.deletePost(
            Post(
                post.id,
                post.title,
                post.text,
                post.date.format(formatter),
                post.location.latitude,
                post.location.longitude
            )
        )
    }

    suspend fun update(post: PostDtoInput){

        postDao.updatePost(
            Post(
                post.id,
                post.title,
                post.text,
                post.date.format(formatter),
                post.location.latitude,
                post.location.longitude
            )
        )

        val pathList = multimediaPathDao.getMultimediaPaths(post.id)

        pathList.forEach { path -> multimediaPathDao.deletePath(path)}

        post.uriList.forEach { path -> multimediaPathDao.insert(MultimediaPath(0, path, post.id)) }

    }

    suspend fun deleteAllPost() {
        postDao.deleteAllPosts()
    }
}