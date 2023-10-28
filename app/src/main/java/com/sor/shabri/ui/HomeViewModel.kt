package com.sor.shabri.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sor.shabri.Caption
import com.sor.shabri.Constants
import com.sor.shabri.Constants.DEFAULT
import com.sor.shabri.Constants.ENGLISH
import com.sor.shabri.Constants.ENGLISH_SYMBOL
import com.sor.shabri.Constants.FIRESTORE_DOCUMENT_ERROR_MESSAGE
import com.sor.shabri.Constants.GUJARATI
import com.sor.shabri.Constants.GUJARATI_SYMBOL
import com.sor.shabri.Constants.HINDI
import com.sor.shabri.Constants.HINDI_SYMBOL
import com.sor.shabri.Constants.TAG
import com.sor.shabri.Constants.defaultHashtag
import com.sor.shabri.Hashtag
import com.sor.shabri.HomeUiState
import com.sor.shabri.Language
import com.sor.shabri.data.CaptionRepository
import com.sor.shabri.data.CaptionRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val captionRepository: CaptionRepository
) : ViewModel(){

    private var captionsOriginal: MutableList<Caption> = mutableListOf()
    private var captionsByLanguage: MutableList<Caption> = mutableListOf()
    private var hashtagsOriginal: MutableList<Hashtag> = mutableListOf()
    private var hashtagsByLanguage: MutableList<Hashtag> = mutableListOf()

    private val db: FirebaseFirestore = Firebase.firestore
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var state: HomeUiState
        get() = _uiState.value
        set(newState) {
            _uiState.update { newState }
        }

    init {
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


        viewModelScope.launch {

            withContext(Dispatchers.IO){
                val filteredCaptions = filteredCaptionsByLanguage(getSelectedLanguageNames())
                state = state.copy(
                    captions = filteredCaptions,
                    hashtags = filterHashtagsFromCaptions(filteredCaptions),
                )
            }

        }


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

       /* val captions = _uiState.value.captions..toMutableList()

        val index = captions.indexOfFirst {
            it.id == caption.id
        }

        if (index > -1) {
            caption.copied++

            captions[index] = caption

            state = state.copy(
                captions = captions
            )

//            captionRepository.copied(caption)
        }
*/
        viewModelScope.launch() {

            captionRepository.copied(caption)
        }

        caption.copied++

        //patch
        state = state.copy(
            captions = _uiState.value.captions,
            selectedHashtagSynonym = _uiState.value.selectedHashtagSynonym + " "
        )
    }

    private fun getCaptionsFromFirestore() {

        state = state.copy(showLoader = true)

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
                    captions = captions,
                    showLoader = false
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
            Log.d("caption" ,caption.toString())
            Log.d("hashtags",caption.hashtags.toString())
            caption.hashtags.forEach { hashtagName ->
                Log.d("hashtag name",hashtagName)

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
