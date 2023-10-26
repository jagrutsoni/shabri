package com.sor.shabri.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sor.shabri.Caption
import com.sor.shabri.Constants
import com.sor.shabri.Hashtag
import com.sor.shabri.ui.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CaptionRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
): CaptionRepository {

    override suspend fun getCaptionsFromFirestore() {
        withContext(Dispatchers.IO){

            firebaseService.getCaptionsFromFirestore()
        }
    }

    override suspend fun getHashtagsFromFirestore() {
        withContext(Dispatchers.IO) {
            firebaseService.getHashtagsFromFirestore()
        }
    }

    override suspend fun copied(caption: Caption) {
        withContext(Dispatchers.IO) {

            firebaseService.copied(caption)
        }
    }

    private fun printCaption(caption: Caption) {

        Log.d(Constants.TAG, caption.text)
        Log.d(Constants.TAG, caption.author)
        Log.d(Constants.TAG, caption.copied.toString())
        Log.d(Constants.TAG, caption.language)
        for (hashtag in caption.hashtags) {

            Log.d(Constants.TAG, hashtag)
        }
    }

    private fun printHashtag(hashtag: Hashtag) {

        Log.d(Constants.TAG, hashtag.synonyms.toString())
    }
}