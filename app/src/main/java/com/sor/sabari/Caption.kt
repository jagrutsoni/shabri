package com.sor.sabari

data class Caption(
    val id:Int = 0,
    val text: String = "",
    val author: String = "",
    val language: String = "",
    var copied: Int = 0,
   // val hashtagDocRefs: MutableList<String> = mutableListOf(),
    val hashtags: MutableList<String> = mutableListOf(),

    //val hashtags: MutableList<Hashtag> = mutableListOf()
)
