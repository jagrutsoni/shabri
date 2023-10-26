package com.sor.shabri

import com.sor.shabri.Constants.DEFAULT
import com.sor.shabri.Constants.ENGLISH
import com.sor.shabri.Constants.ENGLISH_SYMBOL
import com.sor.shabri.Constants.GUJARATI
import com.sor.shabri.Constants.GUJARATI_SYMBOL
import com.sor.shabri.Constants.HINDI
import com.sor.shabri.Constants.HINDI_SYMBOL
import com.sor.shabri.Constants.defaultHashtag

data class HomeUiState(
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
        val showLoader: Boolean = false,

    )
