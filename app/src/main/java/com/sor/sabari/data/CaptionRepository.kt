package com.sor.sabari.data

import com.sor.sabari.Caption

interface CaptionRepository {

    fun getCaptionsFromFirestore()
    fun getHashtagsFromFirestore()

    fun copied(caption: Caption): Unit
}