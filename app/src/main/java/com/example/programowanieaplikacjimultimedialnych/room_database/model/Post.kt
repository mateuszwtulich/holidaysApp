package com.example.programowanieaplikacjimultimedialnych.room_database.model

import androidx.room.*


@Entity(tableName = "post_table")
class Post(@PrimaryKey(autoGenerate = true) val id: Int,
           val title : String,
           val text : String,
           val date : String,
           val latitude : Double,
           val longitude : Double){

    override fun equals(other: Any?): Boolean {
        other as Post
        return this.title == other.title && this.text == other.text
                && this.date == other.date && this.latitude == other.latitude
                && this.longitude == other.longitude
    }
}

