package com.example.programowanieaplikacjimultimedialnych.repository

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.example.programowanieaplikacjimultimedialnych.controller_ui.MainActivity
import com.example.programowanieaplikacjimultimedialnych.room_database.HolidayRoomDatabase
import com.example.programowanieaplikacjimultimedialnych.room_database.model.MultimediaPath
import com.example.programowanieaplikacjimultimedialnych.room_database.model.Post
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.Location
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoInput
import com.example.programowanieaplikacjimultimedialnych.view_model.dto.PostDtoOutput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RunWith(AndroidJUnit4::class)
class HolidayRepositoryTest {

    lateinit var date: LocalDate
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    @Before
    fun initTest(){
        date = LocalDate.now()
    }

    @Test
    fun testTransformInsert(){
        val postInput = PostDtoInput(
            0,
            "Grzybobranie",
            "Przykladowy tekst dla unit testow",
            Location(2.0,2.0),
            date,
            emptyList())


        val postInputAssert = Post(0,"Grzybobranie",
            "Przykladowy tekst dla unit testow",
            date.format(formatter),
            2.0,
            2.0)

         val postDataBase = Post(
            postInput.id,
            postInput.title,
            postInput.text,
            postInput.date.format(formatter),
            postInput.location.latitude,
            postInput.location.longitude
        )

        assertTrue(postInputAssert == postDataBase)
    }

    @Test
    fun testTransformGetPost(){
        val postAssertDtoOutput = PostDtoOutput(0,
            "Grzybobranie",
            "Przykladowy tekst dla unit testow",
            Location(2.0,2.0),
            date,
            emptyList())

        val postInputAssert = Post(0,"Grzybobranie",
            "Przykladowy tekst dla unit testow",
            date.format(formatter),
            2.0,
            2.0)

        val postDtoOutput = PostDtoOutput(
            postInputAssert.id,
            postInputAssert.title,
            postInputAssert.text,
            Location(postInputAssert.latitude, postInputAssert.longitude),
            LocalDate.parse(postInputAssert.date, formatter),
            emptyList())

        assertTrue(postDtoOutput == postAssertDtoOutput)
    }

    @Test
    fun uriListTrasform(){

        val listMultimediaPath = listOf(MultimediaPath(0,"firstElem",1),
            MultimediaPath(1,"secondElem",1),
            MultimediaPath(3,"thirdElem",4),
            MultimediaPath(2,"fourthElem",2)
        )
        val uriListAssert = listOf(Uri.parse("firstElem"), Uri.parse("secondElem"), Uri.parse("thirdElem") ,
            Uri.parse("fourthElem"))

        val uriList = listMultimediaPath.map { multimediaPath -> Uri.parse(multimediaPath.path) }

        var bool = true
        for (i in uriList.indices){
            if(uriList[i] != uriListAssert[i])
                bool = false
        }

        assertTrue(bool)
    }

}

