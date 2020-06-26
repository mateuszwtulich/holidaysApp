package com.example.programowanieaplikacjimultimedialnych.view_model.dto

import java.time.LocalDate
import java.time.Month

class PostDtoInput (
    val id : Int,
    val title : String,
    var text : String,
    val location : Location,
    val date  : LocalDate,
    val uriList: List<String>
)