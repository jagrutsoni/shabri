package com.sor.sabari.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sor.sabari.Caption
import com.sor.sabari.Constants
import com.sor.sabari.Hashtag

class CaptionRepositoryImpl : CaptionRepository {

    private var db: FirebaseFirestore = Firebase.firestore
    override fun getCaptionsFromFirestore() {
        TODO("Not yet implemented")
    }

    override fun getHashtagsFromFirestore() {
        TODO("Not yet implemented")
    }

    override fun copied(caption: Caption) {

        db.collection(Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME)
            .document(caption.id.toString())
            .update(Constants.EXCEL_COPIED_KEY, caption.copied)
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