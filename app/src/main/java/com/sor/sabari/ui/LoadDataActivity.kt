package com.sor.sabari.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sor.sabari.Constants.CAPTIONS_JSON_FILE_NAME
import com.sor.sabari.Constants.EXCEL_AUTHOR_KEY
import com.sor.sabari.Constants.EXCEL_ID_KEY
import com.sor.sabari.Constants.EXCEL_COPIED_KEY
import com.sor.sabari.Constants.EXCEL_HASHTAGS_KEY
import com.sor.sabari.Constants.EXCEL_HASHTAG_SYNONYMS_KEY
import com.sor.sabari.Constants.EXCEL_LANGUAGE_KEY
import com.sor.sabari.Constants.EXCEL_NAME_KEY
import com.sor.sabari.Constants.EXCEL_TEXT_KEY
import com.sor.sabari.Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME
import com.sor.sabari.Constants.FIRESTORE_COLLECTION_ERROR_MESSAGE
import com.sor.sabari.Constants.FIRESTORE_HASHTAGS_COLLECTION_NAME
import com.sor.sabari.Constants.HASHTAGS_JSON_FILE_NAME
import com.sor.sabari.Constants.TAG
import com.sor.sabari.ui.theme.AppTheme
import org.json.JSONArray

class LoadDataActivity : ComponentActivity(){

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {

                }
            }
        }
        db = Firebase.firestore
        writeCaptionsJsonToFireStore()
        writeHashtagsJsonToFirestore()
    }
    private fun writeCaptionsJsonToFireStore() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        var islandRef = storageRef.child(CAPTIONS_JSON_FILE_NAME)

        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val s = String(it)
            val jsonArray = JSONArray(s)

            for (i in 0 until jsonArray.length()) {
                Log.d("test1",jsonArray.getJSONObject(i).toString())
                val jsonObject = jsonArray.getJSONObject(i)
                val array: List<String> = jsonObject.getString(EXCEL_HASHTAGS_KEY).split(',')
                val newArray: MutableList<String> = mutableListOf()
               /* val prefix = "$EXCEL_HASHTAG_KEY/"
                for (obj in array) {
                    obj.trimStart()
                    .trimEnd()
                        .lowercase()
                    newArray.add(prefix + obj)
                }*/
                for (obj in array) {
                    newArray.add(obj.trimStart()
                        .trimEnd()
                        .lowercase())
                }
                val id = jsonObject.getInt(EXCEL_ID_KEY)
                val captionHash = hashMapOf(
                    EXCEL_ID_KEY to id,
                    EXCEL_TEXT_KEY to jsonObject.getString(EXCEL_TEXT_KEY).trimStart().trimEnd(),
                    EXCEL_AUTHOR_KEY to jsonObject.getString(EXCEL_AUTHOR_KEY),
                    EXCEL_LANGUAGE_KEY to jsonObject.getString(EXCEL_LANGUAGE_KEY),
                    EXCEL_COPIED_KEY to jsonObject.getInt(EXCEL_COPIED_KEY),
                    EXCEL_HASHTAGS_KEY to newArray
                   // EXCEL_HASHTAG_DOC_KEY to newArray
                )

                db.collection(FIRESTORE_CAPTIONS_COLLECTION_NAME).document(id.toString()).set(captionHash)
            }
        }.addOnFailureListener {
            Log.d(TAG, FIRESTORE_COLLECTION_ERROR_MESSAGE)

        }
    }

    private fun writeHashtagsJsonToFirestore() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        var islandRef = storageRef.child(HASHTAGS_JSON_FILE_NAME)

        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val s = String(it)
            val jsonArray = JSONArray(s)

            for (i in 0 until jsonArray.length()) {
                if(jsonArray.isNull(i)){
                    Log.d("test1",""+jsonArray.isNull(i))
                    continue
                }
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d("test1",jsonObject.toString())
                val synonyms: List<String> = jsonObject.getString(EXCEL_HASHTAG_SYNONYMS_KEY).split(',')
                synonyms.forEach {synonym ->
                  
                    synonym.trimEnd()
                    .trimStart()
                        .lowercase()
                }

                val hashtagMap = hashMapOf(
                    EXCEL_ID_KEY to jsonObject.getInt(EXCEL_ID_KEY),
                    EXCEL_NAME_KEY to jsonObject.getString(EXCEL_NAME_KEY).trimStart().trimEnd().lowercase(),
                    EXCEL_HASHTAG_SYNONYMS_KEY to synonyms
                )
                db.collection(FIRESTORE_HASHTAGS_COLLECTION_NAME).document("${jsonObject.getInt(EXCEL_ID_KEY)}").set(hashtagMap)
            }
        }.addOnFailureListener {
            Log.d(TAG, FIRESTORE_COLLECTION_ERROR_MESSAGE)

        }
    }
}