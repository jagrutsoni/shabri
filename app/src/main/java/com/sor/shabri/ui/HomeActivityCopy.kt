package com.sor.shabri.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.random.Random
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ListItem
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.google.firebase.firestore.Query
import com.sor.shabri.Caption
import com.sor.shabri.Constants
import com.sor.shabri.Constants.DEFAULT
import com.sor.shabri.Constants.ENGLISH
import com.sor.shabri.Constants.ENGLISH_SYMBOL
import com.sor.shabri.Constants.EXCEL_COPIED_KEY
import com.sor.shabri.Constants.EXCEL_LANGUAGE_KEY
import com.sor.shabri.Constants.FIRESTORE_CAPTIONS_COLLECTION_NAME
import com.sor.shabri.Constants.FIRESTORE_DOCUMENT_ERROR_MESSAGE
import com.sor.shabri.Constants.GUJARATI
import com.sor.shabri.Constants.GUJARATI_SYMBOL
import com.sor.shabri.Constants.HINDI
import com.sor.shabri.Constants.HINDI_SYMBOL
import com.sor.shabri.Constants.MAP_LANGUAGE_INDEX
import com.sor.shabri.Constants.TAG
import com.sor.shabri.Hashtag
import com.sor.shabri.Language
import com.sor.shabri.R
import com.sor.shabri.ui.theme.ShabriTheme
import com.sor.shabri.ui.theme.Typography

class HomeActivityCopy : ComponentActivity() {
    private lateinit var db: FirebaseFirestore
    private var captionsOriginal: MutableList<Caption> = mutableStateListOf()
    private var captions: MutableList<Caption> = mutableStateListOf()
    private var hashtagsOriginal: MutableList<Hashtag> = mutableStateListOf()
    private var hashtags: MutableList<Hashtag> = mutableStateListOf()
    private var queryStringCopy: String = ""
    private var languagePref: MutableList<Language> = mutableListOf()
    private var selectedLanguageSymbol: MutableList<String> = mutableStateListOf()
    private var selectedLanguageName: MutableList<String> = mutableListOf()
    private val defaultHashtag = Constants.defaultHashtag
    private var selectedHashtagSynonym: String = DEFAULT
    private lateinit var gradientColors: List<Color>

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.firestore

        setContent {
            ShabriTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    initLanguagePref()
                    getCaptions()
                    displayData()
                }
            }
        }
    }

    private fun initLanguagePref() {

        languagePref.add(Language(ENGLISH_SYMBOL, ENGLISH, true))
        languagePref.add(Language(HINDI_SYMBOL, HINDI, true))
        languagePref.add(Language(GUJARATI_SYMBOL, GUJARATI, true))
        languagePref.forEach {
            selectedLanguageSymbol.add(it.symbol)
            selectedLanguageName.add(it.name)
        }
    }

    private fun getCaptions() {
        captions.clear()
        captionsOriginal.clear()
        hashtags.clear()
        hashtagsOriginal.clear()

        db.collection(FIRESTORE_CAPTIONS_COLLECTION_NAME)
            .whereIn(EXCEL_LANGUAGE_KEY, selectedLanguageName)
            .orderBy(EXCEL_COPIED_KEY, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val caption: Caption = document.toObject(Caption::class.java)
                    /*for (hashtagRef in caption.hashtagDocRefs) {

                        db.document(hashtagRef).get().addOnSuccessListener { hashtag ->

                            val hashtag: Hashtag? = hashtag.toObject(Hashtag::class.java)

                            printCaption(caption)
                            caption.hashtags.add(hashtag!!)
                            if (!hashtags.contains(hashtag)) {

                                hashtags.add(hashtag!!)
                                hashtagsOriginal.add(hashtag!!)
                            }
                        }
                    }*/
                    captions.add(caption)
                }
                captionsOriginal.addAll(captions)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, FIRESTORE_DOCUMENT_ERROR_MESSAGE, exception)
            }
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    @ExperimentalMaterial3Api
    fun displayData() {

        gradientColors = listOf(MaterialTheme.colorScheme.primary, Blue, Red)
        val focusManager = LocalFocusManager.current
        var queryString: String by rememberSaveable { mutableStateOf("") }
        var active by rememberSaveable { mutableStateOf(false) }
        var selectedHashtag by remember { mutableStateOf(defaultHashtag) }

        val onQueryChange: (String) -> Unit = {
            selectedHashtag = defaultHashtag
            queryStringCopy = it
            queryString = it
            filterCaptions(queryString, selectedHashtag)
            filterHashtags(queryString)
        }

        val onClear: () -> Unit = {
            if (queryString.isNotEmpty()) {
                queryString = ""
                queryStringCopy = ""
                resetAllList()
            } else {
                active = false
            }
        }

        val onHashtagClick: (Hashtag) -> Unit = {
            focusManager.clearFocus()
            selectedHashtag = if (selectedHashtag == it) defaultHashtag else it
            filterCaptions(queryString, selectedHashtag)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_medium))
                .background(color = MaterialTheme.colorScheme.background)
        ) {

            showDocketSearchBar(
                queryString,
                onQueryChange,
                onClear,
                { active = it },
                { active = false })
            showHorizontalGrid(onQueryChange, onHashtagClick, selectedHashtag)
            showLazyColumn(selectedHashtag)
        }
    }

    private fun filterCaptions(queryString: String, selectedHashtag: Hashtag) {

        captions.clear()
        for (caption in captionsOriginal) {

            /*if (selectedHashtag != defaultHashtag) {
                if (caption.hashtags.contains(selectedHashtag)) {
                    captions.add(caption)
                }
            } else {
                if (caption.text.contains(queryString)) {
                    captions.add(caption)
                }
            }*/
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun showDocketSearchBar(
        queryString: String,
        onQueryChange: (String) -> Unit,
        onClear: () -> Unit,
        onActiveChange: (Boolean) -> Unit,
        onSearch: (String) -> Unit
    ) {

        DockedSearchBar(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
            query = queryString,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            active = false,
            onActiveChange = onActiveChange,
            placeholder = { Text(stringResource(R.string.search_hint), style = Typography.titleMedium) },
            leadingIcon = {
                showDropDown()
            },
            trailingIcon = {
                if (queryString.isNotEmpty()) {
                    Icon(
                        modifier = Modifier.clickable {
                            onClear()
                        },
                        imageVector = Icons.Rounded.Clear,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Clear Icon"
                    )
                } else {
                    showSearchIcon()
                }
            }
        ) {}
    }

    @Composable
    fun showDropDown() {

        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
                .padding(end = dimensionResource(R.dimen.padding_small))
        ) {

            TextButton(onClick = { expanded = true }) {
                Text(
                    selectedLanguageSymbol.toList().toString(),
                    color = MaterialTheme.colorScheme.secondary,
                    style = Typography.labelSmall
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "f",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languagePref.forEach {
                    var checkedState by remember { mutableStateOf(it.isChecked) }
                    val onCheckedChange: (Boolean) -> Unit = { newCheckedState ->

                        if (newCheckedState) {
                            selectedLanguageSymbol.add(it.symbol)
                            selectedLanguageName.add(it.name)
                            expanded = false
                        } else {
                            expanded = selectedLanguageName.size == 1
                            selectedLanguageSymbol.remove(it.symbol)
                            selectedLanguageName.remove(it.name)
                        }
                        checkedState = newCheckedState
                        it.isChecked = newCheckedState

                        resetSelection()
                        getCaptions()
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = it.symbol + " - " + it.name, style = Typography.titleSmall)
                                Checkbox(
                                    checked = checkedState,
                                    onCheckedChange = { newCheckedState ->

                                        onCheckedChange(newCheckedState)
                                    })
                            }
                        },
                        onClick = {
                            val newCheckedState = !it.isChecked
                            onCheckedChange(newCheckedState)
                        },
                    )
                }
            }
        }
    }

    @Composable
    private fun showSearchIcon() {
        Icon(
            imageVector = Icons.Rounded.Search,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = "Search Icon"
        )
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
    @Composable
    private fun showHorizontalGrid(
        onQueryChange: (String) -> Unit,
        onHashtagClick: (Hashtag) -> Unit,
        selectedHashtag: Hashtag
    ) {
        LazyHorizontalStaggeredGrid(
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.background
                )
                .height(80.dp),
            rows = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = dimensionResource(R.dimen.padding_medium),
                bottom = dimensionResource(R.dimen.padding_medium)
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalItemSpacing = 4.dp
        ) {
            items(hashtags) { hashtag ->

                val indexCalculationInMultiple = calculateIndex(hashtag)
                val hashtagItem = hashtag.synonyms[indexCalculationInMultiple]

                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedHashtag == hashtag) Yellow.copy(alpha = 0.6f) else Color.LightGray
                    ),
                    modifier = Modifier.clickable {

                        onHashtagClick(hashtag)
                        selectedHashtagSynonym = hashtagItem
                    },
                ) {

                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                append("#")
                            }
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = gradientColors
                                    )
                                )
                            ) {
                                append("${hashtagItem.trimStart().trimEnd()}")
                            }
                        },
                        modifier = Modifier
                            .padding(dimensionResource(R.dimen.padding_small)),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                }
            }
        }
    }

    private fun calculateIndex(hashtag: Hashtag): Int {

        val indexToBeShown: MutableSet<Int> = mutableSetOf()
        selectedLanguageName.forEach {
            indexToBeShown.addAll(MAP_LANGUAGE_INDEX[it]!!)
        }
        //language wise index's calculation
        var index = hashtags.indexOf(hashtag)
        val quotionet = hashtag.synonyms.size / 5
        val elementOfRandomIndex = indexToBeShown.elementAt(index % indexToBeShown.size)
        val indexCalculationInMultiple =
            (elementOfRandomIndex + (5 * (index % quotionet)))
        return indexCalculationInMultiple
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
    @Composable
    private fun showLazyColumn(selectedHashtag: Hashtag) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {

                        //focusManager.clearFocus()
                    })
                }
        ) {

            if (selectedHashtag != defaultHashtag) {

                stickyHeader {
                    Surface(Modifier.fillParentMaxWidth()) {
                        Text(
                            buildAnnotatedString {

                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,

                                        )
                                ) {
                                    append("#")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.onBackground,
                                                Blue, Red
                                            )
                                        ),

                                        )
                                ) {
                                    append("$selectedHashtagSynonym")
                                }
                            },
                            modifier = Modifier
                                .padding(dimensionResource(R.dimen.padding_small)),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = Typography.titleMedium

                        )
                    }
                }
            } else if (captions.isEmpty()) {
                stickyHeader {

                    Surface(Modifier.fillParentMaxWidth()) {
                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        MaterialTheme.colorScheme.secondary,
                                    )
                                ) {
                                    append("\"${queryStringCopy}\" ")
                                }
                                withStyle(
                                    SpanStyle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.onBackground,
                                                Blue, Red
                                            )
                                        )
                                    )
                                ) {
                                    append("No matches")
                                }
                            },
                            modifier = Modifier
                                .padding(dimensionResource(R.dimen.padding_small)),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = Typography.titleMedium
                        )
                    }
                }
            }


            captions.sortByDescending { it.copied }
            items(items = captions, key = { caption -> caption.id })
            { caption ->
                showCaption(caption,selectedHashtag)
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    fun showCaption(caption: Caption, selectedHashtag: Hashtag) {
        val clipboardManager: androidx.compose.ui.platform.ClipboardManager =
            LocalClipboardManager.current

        Card(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(2.dp),
        ) {
            ListItem(
                headlineContent = {
                    Text(buildAnnotatedString {
                        val startIndex = caption.text.indexOf(queryStringCopy, ignoreCase = true)
                        val endIndex = startIndex + queryStringCopy.length

                        if (startIndex >= 0) {
                            append(caption.text.substring(0, startIndex))
                            withStyle(
                                SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = gradientColors
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(queryStringCopy)
                            }
                            append(caption.text.substring(endIndex, caption.text.length))
                        } else {
                            append(caption.text)
                        }

                    })
                },
                overlineContent = {
                    Text(
                        caption.author,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small)),
                    )
                },
                trailingContent = {

                    Row(modifier = Modifier.clickable {
                        val copied = ++caption.copied
                        db.collection(FIRESTORE_CAPTIONS_COLLECTION_NAME).document(caption.id.toString())
                            .update(EXCEL_COPIED_KEY, copied)
                        caption.copied = copied

                        filterCaptions(queryStringCopy, selectedHashtag)
                        Toast.makeText(applicationContext, "Copied", Toast.LENGTH_SHORT).show()
                        clipboardManager.setText(AnnotatedString(caption.text + "\n- " + caption.author))

                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_content_copy_24),
                            contentDescription = "Localized description",
                        )

                        Text(
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                            color = MaterialTheme.colorScheme.secondary,
                            text = caption.copied.toString(),
                            style = Typography.labelMedium
                        )

                    }

                },
                colors = ListItemDefaults.colors(
                    containerColor = Color(Random.nextLong(0xFFFFFFFF)).copy(alpha = 0.3f),
                    overlineColor = MaterialTheme.colorScheme.primary,
                    headlineColor = Color.DarkGray,
                    trailingIconColor = Color.White
                )
            )
        }

    }

    private fun filterHashtags(searchString: String) {

        hashtags.clear()
        for (hashtag in hashtagsOriginal) {

            for (synonym in hashtag.synonyms) {

                if (synonym.contains(searchString)) {

                    hashtags.add(hashtag)
                    break
                }
            }
        }

    }

    private fun resetAllList() {
        captions.resetCaptions()
        hashtags.resetHashtags()
    }

    private fun resetSelection() {
        selectedHashtagSynonym = DEFAULT
    }

    private fun MutableList<Hashtag>.resetHashtags() {

        clear()
        addAll(hashtagsOriginal)
    }

    private fun MutableList<Caption>.resetCaptions() {

        clear()
        addAll(captionsOriginal)
    }

    private fun printCaption(caption: Caption) {

        Log.d(TAG, caption.text)
        Log.d(TAG, caption.author)
        Log.d(TAG, caption.copied.toString())
        Log.d(TAG, caption.language)
        /*for (hashtag in caption.hashtags) {

            Log.d(TAG, hashtag.synonyms.toString())
        }*/
    }

    private fun printHashtag(hashtag: Hashtag) {

        Log.d(TAG, hashtag.synonyms.toString())
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun hideKeyboard() {
        val keyboardController = LocalSoftwareKeyboardController.current
        keyboardController?.hide()
    }

}
