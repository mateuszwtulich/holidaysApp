package com.example.programowanieaplikacjimultimedialnych.view_model.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Location(
    var latitude: Double,
    var longitude: Double
) : Parcelable