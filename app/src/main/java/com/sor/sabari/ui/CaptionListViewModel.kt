package com.sor.sabari.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sor.sabari.Caption
import com.sor.sabari.Constants
import com.sor.sabari.Constants.DEFAULT
import com.sor.sabari.Constants.ENGLISH
import com.sor.sabari.Constants.ENGLISH_SYMBOL
import com.sor.sabari.Constants.FIRESTORE_DOCUMENT_ERROR_MESSAGE
import com.sor.sabari.Constants.GUJARATI
import com.sor.sabari.Constants.GUJARATI_SYMBOL
import com.sor.sabari.Constants.HINDI
import com.sor.sabari.Constants.HINDI_SYMBOL
import com.sor.sabari.Constants.TAG
import com.sor.sabari.Constants.defaultHashtag
import com.sor.sabari.Hashtag
import com.sor.sabari.Language
import com.sor.sabari.data.CaptionRepository
import com.sor.sabari.data.CaptionRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CaptionListViewModel : ViewModel() {

    private val captionRepository: CaptionRepository = CaptionRepositoryImpl()
    private var captionsOriginal: MutableList<Caption> = mutableListOf()
    private var captionsByLanguage: MutableList<Caption> = mutableListOf()
    private var hashtagsOriginal: MutableList<Hashtag> = mutableListOf()
    private var hashtagsByLanguage: MutableList<Hashtag> = mutableListOf()

    private val db: FirebaseFirestore = Firebase.firestore

    data class CaptionListUiState(
        val captions: List<Caption> = listOf(),
        val hashtags: List<Hashtag> = listOf(),
        val queryString: String = "",
        val selectedHashtag: Hashtag = defaultHashtag,
        val selectedHashtagSynonym: String = DEFAULT,
        val selectedLanguages: MutableSet<Language> = mutableSetOf(
            Language(
                ENGLISH_SYMBOL,
                ENGLISH,
                true
            ),
            Language(HINDI_SYMBOL, HINDI, true),
            Language(GUJARATI_SYMBOL, GUJARATI, true)
        ),
    )

    private val _uiState = MutableStateFlow(CaptionListUiState())
    val uiState = _uiState.asStateFlow()

    private var state: CaptionListUiState
        get() = _uiState.value
        set(newState) {
            _uiState.update { newState }
        }


    init {
        Log.d("test1", "init called")

        getCaptionsFromFirestore()
        getHashtagsFromFirestore()
    }

    fun onCheckedChange(isChecked: Boolean, language: Language) {

        _uiState.value.selectedLanguages.elementAt(_uiState.value.selectedLanguages.indexOf(language)).isChecked =
            isChecked
        state = state.copy(

            selectedHashtagSynonym = DEFAULT,
            selectedHashtag = defaultHashtag
        )

        val filteredCaptions = filteredCaptionsByLanguage(getSelectedLanguageNames())

        state = state.copy(
            captions = filteredCaptions,
            hashtags = filterHashtagsFromCaptions(filteredCaptions)
        )
    }

    fun onQueryChange(queryString: String) {

        val filteredHashtag = filterHashtagsByQuery(queryString)
        var filteredCaption = filterCaptionsByQuery(queryString)
        if (filteredHashtag.size == 1 && filteredCaption.isEmpty()) {
            state = state.copy(
                queryString = queryString,
                hashtags = filteredHashtag,
                captions = filterCaptionsByHashtag(filteredHashtag[0]),
                selectedHashtag = filteredHashtag[0],
                selectedHashtagSynonym = filteredHashtag[0].synonyms[0],
            )
        } else {
            state = state.copy(
                queryString = queryString,
                hashtags = filteredHashtag,
                captions = filteredCaption,
                selectedHashtag = defaultHashtag,
            )
        }
    }

    fun onClear() {

        state = state.copy(
            selectedHashtag = defaultHashtag,
            queryString = "",
            captions = captionsByLanguage,
            hashtags = hashtagsByLanguage
        )
    }

    fun onCopy(caption: Caption) {

        ++caption.copied

        state = state.copy(
            captions = _uiState.value.captions
        )

        captionRepository.copied(caption)
    }

    private fun getCaptionsFromFirestore() {

        captionsByLanguage.clear()
        val captions: MutableList<Caption> = mutableListOf()

        db.collection(Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME)
            .orderBy(Constants.EXCEL_COPIED_KEY, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val caption: Caption = document.toObject(Caption::class.java)
                    captions.add(caption)
                }

                state = state.copy(
                    captions = captions
                )
                captionsOriginal.addAll(captions)
                captionsByLanguage.addAll(captions)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, FIRESTORE_DOCUMENT_ERROR_MESSAGE, exception)
            }
    }

    private fun getHashtagsFromFirestore() {

        val hashtags: MutableList<Hashtag> = mutableListOf()

        db.collection(Constants.FIRESTORE_HASHTAGS_COLLECTION_NAME)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val hashtag: Hashtag = document.toObject(Hashtag::class.java)
                    hashtags.add(hashtag)
                }

                hashtags.shuffle()
                state = state.copy(
                    hashtags = hashtags
                )
                hashtagsOriginal.addAll(hashtags)
                hashtagsByLanguage.addAll(hashtags)

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, FIRESTORE_DOCUMENT_ERROR_MESSAGE, exception)
            }
    }


    private fun filteredCaptionsByLanguage(selectedLanguageNames: List<String>): List<Caption> {
        captionsByLanguage =
            captionsOriginal.filter { caption -> selectedLanguageNames.contains(caption.language) } as MutableList<Caption>
        return captionsByLanguage
    }

    private fun filterCaptionsByHashtag(selectedHashtag: Hashtag): List<Caption> {

        val filterByHashtag: MutableList<Caption> = mutableListOf()
        captionsByLanguage.forEach { caption ->

            if (caption.hashtags.contains(selectedHashtag.name)) {
                filterByHashtag.add(caption)
            }
        }
        return filterByHashtag
    }

    private fun filterCaptionsByQuery(queryString: String): List<Caption> {

        val filterByQuery: MutableList<Caption> = mutableListOf()
        captionsByLanguage.forEach { caption ->

            if (caption.text.contains(queryString)) {
                filterByQuery.add(caption)
            }
        }

        return filterByQuery
    }


    private fun filterHashtagsFromCaptions(captions: List<Caption>): List<Hashtag> {

        hashtagsByLanguage.clear()
        val hashtags: MutableList<Hashtag> = mutableListOf()
        captions.forEach { caption ->

            caption.hashtags.forEach { hashtagName ->
                Log.d("test1", caption.toString())
                Log.d("test1", hashtagName)
                val hashtag: Hashtag =
                    hashtagsOriginal.single { hashtag -> hashtag.name.equals(hashtagName, true) }
                hashtags.add(hashtag)

            }
        }

        hashtagsByLanguage.addAll(hashtags.distinct())
        return hashtags.distinct()
    }

    private fun filterHashtagsByQuery(searchString: String): MutableList<Hashtag> {

        val filterHashtagsByQuery = mutableListOf<Hashtag>()
        for (hashtag in hashtagsByLanguage) {

            for (synonym in hashtag.synonyms) {

                //    if (synonym.contains(searchString)) {
                if (synonym.startsWith(searchString)) {

                    filterHashtagsByQuery.add(hashtag)
                    break
                }
            }
        }

        return filterHashtagsByQuery
    }

    fun onHashtagClick(clickedHashtag: Hashtag, selectedHashtagSynonym: String) {

        val selectedHashtag =
            if (_uiState.value.selectedHashtag == clickedHashtag) defaultHashtag else clickedHashtag

        val filteredCaption = if (selectedHashtag == defaultHashtag) {

            filterCaptionsByQuery(_uiState.value.queryString)
        } else {

            filterCaptionsByHashtag(selectedHashtag)
        }

        state = state.copy(
            captions = filteredCaption,
            selectedHashtag = selectedHashtag,
            selectedHashtagSynonym = selectedHashtagSynonym
        )

    }


    private fun getSelectedLanguageNames(): List<String> {
        val selectedLanguageNames: MutableList<String> = mutableListOf()

        _uiState.value.selectedLanguages.forEach { languagePref ->
            if (languagePref.isChecked) {
                selectedLanguageNames.add(languagePref.name)
            }
        }

        return selectedLanguageNames
    }

}
