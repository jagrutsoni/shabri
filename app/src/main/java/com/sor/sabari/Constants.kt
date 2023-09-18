package com.sor.sabari

object Constants {

    const val TAG = "CaptionCacheLog"

    const val FIRESTORE_CAPTIONS_COLLECTION_NAME = "captions"
    const val FIRESTORE_HASHTAGS_COLLECTION_NAME = "hashtags"

    const val CAPTIONS_JSON_FILE_NAME = "Captions.json"
    const val HASHTAGS_JSON_FILE_NAME = "Hashtags.json"

    const val EXCEL_ID_KEY = "id"
    const val EXCEL_TEXT_KEY = "text"
    const val EXCEL_AUTHOR_KEY = "author"
    const val EXCEL_LANGUAGE_KEY = "language"
    const val EXCEL_COPIED_KEY = "copied"
  //  const val EXCEL_HASHTAG_DOC_KEY = "hashtagDocRefs"
    const val EXCEL_HASHTAGS_KEY = "hashtags"

    const val EXCEL_HASHTAG_KEY = "hashtag"
    const val EXCEL_HASHTAG_SYNONYMS_KEY = "synonyms"
    const val EXCEL_NAME_KEY = "name"


    const val FIRESTORE_COLLECTION_ERROR_MESSAGE = "error in download storage"
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