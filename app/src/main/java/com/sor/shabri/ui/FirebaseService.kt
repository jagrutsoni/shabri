package com.sor.shabri.ui

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sor.shabri.Caption
import com.sor.shabri.Constants

class FirebaseService {

    private var db: FirebaseFirestore = Firebase.firestore

    fun getCaptionsFromFirestore() {

    }

    fun getHashtagsFromFirestore() {

    }

    fun copied(caption: Caption) {

        db.collection(Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME)
            .document(caption.id.toString())
            .update(Constants.EXCEL_COPIED_KEY, caption.copied+1)

        Log.d("copied",caption.toString())
    }

}