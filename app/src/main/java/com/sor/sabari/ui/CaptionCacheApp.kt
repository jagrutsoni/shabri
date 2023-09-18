package com.sor.sabari.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sor.sabari.Caption
import com.sor.sabari.Constants
import com.sor.sabari.Constants.ENGLISH
import com.sor.sabari.Constants.GUJARATI
import com.sor.sabari.Constants.HINDI
import com.sor.sabari.Constants.defaultHashtag
import com.sor.sabari.Hashtag
import com.sor.sabari.Language
import com.sor.sabari.R
import com.sor.sabari.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
@ExperimentalMaterial3Api
fun CaptionCacheApp(captionListViewModel: CaptionListViewModel) {
    val captionUiState: CaptionListViewModel.CaptionListUiState by captionListViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        Log.d("test1", "caption cache app called")
        showDocketSearchBar(captionUiState, captionListViewModel,listState)
        showHorizontalGrid(captionUiState, captionListViewModel,listState)
        showLazyColumn(captionUiState, captionListViewModel,listState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun showDocketSearchBar(
    captionUiState: CaptionListViewModel.CaptionListUiState,
    captionListViewModel: CaptionListViewModel,
    listState: LazyListState
) {
    Log.d("test1", "showDocketSearchBar called")
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var active by rememberSaveable { mutableStateOf(false) }

    DockedSearchBar(
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth(),
        query = captionUiState.queryString,
        onQueryChange = captionListViewModel::onQueryChange,
        onSearch = { active = false },
        active = false,
        onActiveChange = { active = it },
        placeholder = {
            Text(
                stringResource(R.string.search_hint),
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        leadingIcon = {
            showDropDown(captionUiState, captionListViewModel,listState)
        },
        trailingIcon = {
            if (captionUiState.queryString.isNotEmpty()) {
                Icon(
                    modifier = Modifier.clickable {

                        focusManager.clearFocus()
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                        (captionListViewModel::onClear)()
                    },
                    imageVector = Icons.Rounded.Clear,
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    contentDescription = "Clear Icon"
                )
            } else {
                showSearchIcon()
            }
        }
    ) {}
}

@Composable
private fun showSearchIcon() {
    Icon(
        imageVector = Icons.Rounded.Search,
        tint = MaterialTheme.colorScheme.surfaceTint,
        contentDescription = "Search Icon"
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun showDropDown(
    captionUiState: CaptionListViewModel.CaptionListUiState,
    captionListViewModel: CaptionListViewModel,
    listState: LazyListState
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var gradientColors: List<Color> = if (isSystemInDarkTheme()) {
        listOf(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.surfaceTint,
            Color.Red
        )

    } else {
        listOf(MaterialTheme.colorScheme.primary, Color.Blue, Color.Red)

    }
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .padding(end = dimensionResource(R.dimen.padding_small))
    ) {

        TextButton(onClick = {
            focusManager.clearFocus()
            expanded = true
        }) {
            Text(
                text = getSymbols(captionUiState.selectedLanguages),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = Typography.labelSmall
            )
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = "icon to open/close the dropdown language list",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },

            ) {
            captionUiState.selectedLanguages.forEach {
                var checkedState by remember { mutableStateOf(it.isChecked) }

                val onCheckedChange: (Boolean) -> Unit = { isChecked ->
                    if (captionUiState.selectedLanguages.filter { it.isChecked }.size == 1 && !isChecked) {

                    } else {
                        checkedState = isChecked
                        (captionListViewModel::onCheckedChange)(isChecked, it)
                    }

                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = it.symbol + " - " + it.name,
                                style = Typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = checkedState,
                                onCheckedChange = onCheckedChange
                            )
                        }
                    },
                    onClick = {
                        focusManager.clearFocus()
                        val isChecked = !it.isChecked
                        onCheckedChange(isChecked)
                    },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.surfaceTint),
                )

            }
            Button(
                onClick = { focusManager.clearFocus()
                    expanded = false },
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.padding_medium),
                        end = dimensionResource(R.dimen.padding_medium)
                    ),
                enabled = true,
                shape = ButtonDefaults.textShape,
                colors = ButtonDefaults.textButtonColors(),
                elevation = null,
                border = null,
                interactionSource = remember { MutableInteractionSource() },
            )
            {
                Text(buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            brush = Brush.linearGradient(
                                colors = gradientColors
                            )
                        )
                    ) {
                        append("Irshad")
                    }
                }, fontStyle = FontStyle.Italic, style = Typography.titleMedium)
            }

        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
private fun showHorizontalGrid(
    captionUiState: CaptionListViewModel.CaptionListUiState,
    captionListViewModel: CaptionListViewModel,
    listState: LazyListState
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Log.d("test1", "showHorizontalGrid called")
    var gradientColors: List<Color> = if (isSystemInDarkTheme()) {
        listOf(Color.Blue, Color.Black, Color.Red)

    } else {
        listOf(MaterialTheme.colorScheme.primary, Color.Blue, Color.Red)

    }
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
        Log.d("test1", "showHorizontalGrid items called")
        items(captionUiState.hashtags) { hashtag ->

            val indexCalculationInMultiple = calculateIndex(captionUiState, hashtag)
            val hashtagItem = hashtag.synonyms[indexCalculationInMultiple]

            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (captionUiState.selectedHashtag == hashtag) Color.Yellow.copy(
                        alpha = 0.6f
                    ) else Color.LightGray
                ),
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                    (captionListViewModel::onHashtagClick)(hashtag, hashtagItem)
                },
            ) {

                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) Color.Blue else MaterialTheme.colorScheme.primary
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

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun scrollToTop(listState: LazyListState) {

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
private fun showLazyColumn(
    captionUiState: CaptionListViewModel.CaptionListUiState,
    captionListViewModel: CaptionListViewModel,
    listState: LazyListState
) {
    Log.d("test1", "showLazyColumn called")
    val focusManager = LocalFocusManager.current

    LazyColumn(state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Log.d("test1", "showLazyColumn item called")


        if (captionUiState.selectedHashtag != defaultHashtag) {

            stickyHeader {
                Surface(Modifier.fillParentMaxWidth()) {
                    Text(
                        buildAnnotatedString {

                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,

                                    )
                            ) {
                                append("#")
                            }
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onBackground,
                                            Color.Blue, Color.Red
                                        )
                                    ),

                                    )
                            ) {
                                append(captionUiState.selectedHashtagSynonym)
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
        } else if (captionUiState.captions.isEmpty()) {
            stickyHeader {

                Surface(Modifier.fillParentMaxWidth()) {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    MaterialTheme.colorScheme.primary,
                                )
                            ) {
                                append("\"${captionUiState.queryString}\" ")
                            }
                            withStyle(
                                SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onBackground,
                                            Color.Blue, Color.Red
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

        //captions.sortByDescending { it.copied }
        Log.d("test1", "items(captionUiState.captions) called" + captionUiState.captions.size)
        items(captionUiState.captions, key = { it.id })
        { caption ->
            showCaption(caption, captionUiState, captionListViewModel)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun showCaption(
    caption: Caption,
    captionUiState: CaptionListViewModel.CaptionListUiState,
    captionListViewModel: CaptionListViewModel
) {
    Log.d("test1", " text = caption.copied.toString() called")
    val context = LocalContext.current
    var gradientColors: List<Color> =
        listOf(MaterialTheme.colorScheme.onPrimaryContainer,Color.Red,MaterialTheme.colorScheme.onSecondaryContainer)
    val clipboardManager: androidx.compose.ui.platform.ClipboardManager =
        LocalClipboardManager.current

    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(2.dp),
    ) {
        ListItem(
            headlineContent = {
                Text(buildAnnotatedString {
                    val startIndex =
                        caption.text.indexOf(captionUiState.queryString, ignoreCase = true)
                    val endIndex = startIndex + captionUiState.queryString.length

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
                            append(captionUiState.queryString)
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
                    (captionListViewModel::onCopy)(caption)

                    Toast.makeText(context, "Caption Copied", Toast.LENGTH_SHORT).show()
                    clipboardManager.setText(AnnotatedString(caption.text + "\n- " + caption.author))
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,1.0f)

                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = "Localized description",
                    )

                    Text(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        text = caption.copied.toString(),
                        style = Typography.bodySmall
                    )

                }

            },
            colors = ListItemDefaults.colors(
                containerColor = getItemColor(caption),
                //   containerColor = MaterialTheme.colorScheme.surfaceVariant,
                overlineColor = MaterialTheme.colorScheme.error,
                headlineColor = getTextColor(caption),
                //   headlineColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.tertiary
            )
        )
    }

}

fun showSnackbar() {

}

@Composable
fun getTextColor(caption: Caption): Color {

    return when (caption.language) {

        GUJARATI -> MaterialTheme.colorScheme.onPrimaryContainer
        HINDI -> MaterialTheme.colorScheme.onSecondaryContainer
        ENGLISH -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> {
            MaterialTheme.colorScheme.surfaceVariant
        }
    }
}

@Composable
fun getItemColor(caption: Caption): Color {

    return when (caption.language) {

        GUJARATI -> MaterialTheme.colorScheme.primaryContainer
        HINDI -> MaterialTheme.colorScheme.secondaryContainer
        ENGLISH -> MaterialTheme.colorScheme.tertiaryContainer
        else -> {
            MaterialTheme.colorScheme.surfaceVariant
        }
    }
}

@Composable
private fun calculateIndex(
    captionUiState: CaptionListViewModel.CaptionListUiState,
    hashtag: Hashtag
): Int {

    val indexToBeShown: MutableSet<Int> = mutableSetOf()
    captionUiState.selectedLanguages.filter { it.isChecked }.forEach {
        indexToBeShown.addAll(Constants.MAP_LANGUAGE_INDEX[it.name]!!)
    }
    Log.d("hashtag", hashtag.toString())
    Log.d("today index to be shown", indexToBeShown.toString())
    //language wise index's calculation
    var index = captionUiState.hashtags.indexOf(hashtag)
    Log.d("today index", index.toString())

    val quotionet = hashtag.synonyms.size / 5
    Log.d("today quotionet", quotionet.toString())

    val elementOfRandomIndex = indexToBeShown.elementAt(index % indexToBeShown.size)
    Log.d("today elementOfRandomIndex", elementOfRandomIndex.toString())

    val indexCalculationInMultiple =
        (elementOfRandomIndex + (5 * (index % quotionet)))
    Log.d("today indexCalculationInMultiple", indexCalculationInMultiple.toString())

    return indexCalculationInMultiple
}

fun getSymbols(selectedLanguages: Set<Language>): String {

    val symbols = StringBuilder("[");

    selectedLanguages.forEach { languagePref ->
        if (languagePref.isChecked) {

            symbols.append(" " + languagePref.symbol)
        }
    }
    symbols.append(" ]")
    return symbols.toString()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun hideKeyboard() {
    val keyboardController = LocalSoftwareKeyboardController.current
    keyboardController?.hide()
}

