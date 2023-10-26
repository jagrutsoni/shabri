package com.sor.shabri

object Constants {

    const val TAG = "ShabriLog"

    const val FIRESTORE_CAPTIONS_COLLECTION_NAME = "captions"
    const val FIRESTORE_HASHTAGS_COLLECTION_NAME = "hashtags"

    const val EXCEL_LANGUAGE_KEY = "language"
    const val EXCEL_COPIED_KEY = "copied"

    const val FIRESTORE_DOCUMENT_ERROR_MESSAGE = "error in download storage"

    const val DEFAULT = "default"

    const val ENGLISH = "English"
    const val HINDI = "हिन्दी"
    const val GUJARATI = "ગુજરાતી"
    const val ENGLISH_SYMBOL = "A"
    const val HINDI_SYMBOL = "क"
    const val GUJARATI_SYMBOL = "ક"

    val MAP_LANGUAGE_INDEX: Map<String, MutableList<Int>> = mapOf(
        Pair(ENGLISH, mutableListOf(0)),
        Pair(HINDI, mutableListOf(0, 3)), Pair(GUJARATI, mutableListOf(0, 1))
    )

    val defaultHashtag = Hashtag(0, DEFAULT, listOf(DEFAULT))
}