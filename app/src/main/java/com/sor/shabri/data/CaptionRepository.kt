package com.sor.shabri.data

import com.sor.shabri.Caption
interface CaptionRepository {

   suspend fun getCaptionsFromFirestore()
  suspend  fun getHashtagsFromFirestore()
   suspend fun copied(caption: Caption)
}