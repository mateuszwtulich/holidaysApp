package com.example.programowanieaplikacjimultimedialnych.room_database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(tableName = "multimediaPath_table",foreignKeys = arrayOf(ForeignKey(entity = Post::class,
                                            parentColumns = arrayOf("id"),
                                            childColumns =  arrayOf("post_id"),
                                            onDelete = ForeignKey.CASCADE)))
class MultimediaPath(@PrimaryKey(autoGenerate = true) val id: Int,
                     val path : String,
                     @ColumnInfo(name ="post_id", index = true) val postId : Int)

