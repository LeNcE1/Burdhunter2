package com.example.lence.bird_hunter.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Birds {

    @SerializedName("birds")
    @Expose
    var birds: List<String> = arrayListOf()

}