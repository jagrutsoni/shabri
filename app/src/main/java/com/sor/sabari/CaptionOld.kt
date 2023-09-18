package com.sor.sabari

import com.google.firebase.firestore.DocumentReference

data class CaptionOld (

    val text : String ="",
    val vote : Int =1,
    val hashtags : List<DocumentReference> = listOf()

)

