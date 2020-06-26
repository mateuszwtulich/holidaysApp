package com.example.programowanieaplikacjimultimedialnych.view_model.dto

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.Month

@Parcelize
class PostDtoOutput(
    val id : Int,
    val title : String,
    val text : String,
    val location : Location,
    val date  : LocalDate,
    val uriList: List<Uri>
) : Parcelable{



    override fun equals(other: Any?): Boolean {
        other as PostDtoOutput

        return  this.id == other.id &&  this.title == other.title && this.text == other.text
                && this.location.latitude ==  other.location.latitude
                && this.location.longitude ==  other.location.longitude
                && this.date.equals(date)
                && this.uriList.equals(other.uriList)
    }
}
