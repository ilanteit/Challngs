package com.example.myapp.models

data class MarkerModel(

    var chall_name: String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var top_score: Int? = null,
    var personal_score: Int?=null,
    var starting_time: Int?=null,
    var time_to_live: Int?=null
)
