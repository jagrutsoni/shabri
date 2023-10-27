package com.sor.shabri.ui

import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.sor.shabri.Caption
import com.sor.shabri.Constants
import com.sor.shabri.Constants.ENGLISH
import com.sor.shabri.Constants.GUJARATI
import com.sor.shabri.Constants.HINDI
import com.sor.shabri.Constants.defaultHashtag
import com.sor.shabri.Hashtag
import com.sor.shabri.Language
import com.sor.shabri.R
import com.sor.shabri.ui.theme.Typography
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.sor.shabri.HomeUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
@ExperimentalMaterial3Api
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val viewState: HomeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    //homeViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.padding_medium))
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        Log.d("test1", "caption cache app called")
        ShowDocketSearchBar(viewState, homeViewModel, listState)
        ShowHorizontalGrid(viewState, homeViewModel, listState)
        showProgressBar(viewState)
        ShowLazyColumn(viewState, homeViewModel, listState)
    }


}

@OptIn(ExperimentalTextApi::class)
@Composable
fun showProgressBar(viewState: HomeUiState) {
    var gradientColors: List<Color> = if (isSystemInDarkTheme()) {
        listOf(Color.Blue, Color.Black, Color.Red)

    } else {
        listOf(MaterialTheme.colorScheme.primary, Color.Blue, Color.Red)

    }
    if (viewState.showLoader) {
        Text(buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                )
            ) {
                append("Fetching Captions...")
            }
        }, fontStyle = FontStyle.Italic, style = Typography.titleMedium)
        LinearProgressIndicator(modifier = Modifier.fillMaxSize())

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDocketSearchBar(
    captionUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    listState: LazyListState
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var active by rememberSaveable { mutableStateOf(false) }
    val onClear: () -> Unit = {
        focusManager.clearFocus()
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
        (homeViewModel::onClear)()
    }
    BackHandler(captionUiState.queryString.isNotEmpty()) {
        onClear()
    }

    DockedSearchBar(
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth(),
        query = captionUiState.queryString,
        onQueryChange = homeViewModel::onQueryChange,
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
            ShowDropDown(captionUiState, homeViewModel, listState)
        },
        trailingIcon = {
            if (captionUiState.queryString.isNotEmpty()) {
                Icon(
                    modifier = Modifier.clickable {
                        onClear()
                    },
                    imageVector = Icons.Rounded.Clear,
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    contentDescription = "Clear Icon"
                )
            } else {
                ShowSearchIcon()
            }
        }
    ) {}
}

@Composable
private fun ShowSearchIcon() {
    Icon(
        imageVector = Icons.Rounded.Search,
        tint = MaterialTheme.colorScheme.surfaceTint,
        contentDescription = "Search Icon"
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ShowDropDown(
    captionUiState: HomeUiState,
    homeViewModel: HomeViewModel,
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
                        (homeViewModel::onCheckedChange)(isChecked, it)
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
                onClick = {
                    focusManager.clearFocus()
                    expanded = false
                },
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
private fun ShowHorizontalGrid(
    captionUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    listState: LazyListState
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

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
        items(captionUiState.hashtags, key = { it.id }) { hashtag ->

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
                    (homeViewModel::onHashtagClick)(hashtag, hashtagItem)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
@Composable
private fun ShowLazyColumn(
    captionUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    listState: LazyListState
) {
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
        items(captionUiState.captions, key = { it.id })
        { caption ->
            ShowCaption(caption, captionUiState, homeViewModel)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ShowCaption(
    caption: Caption,
    captionUiState: HomeUiState,
    homeViewModel: HomeViewModel
) {
    val context = LocalContext.current
    var gradientColors: List<Color> =
        listOf(
            MaterialTheme.colorScheme.onPrimaryContainer,
            Color.Red,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    val clipboardManager: androidx.compose.ui.platform.ClipboardManager =
        LocalClipboardManager.current

    val onCopy: (Caption) -> Unit = { caption ->
        (homeViewModel::onCopy)(caption)

        Toast.makeText(context, "Caption Copied", Toast.LENGTH_SHORT).show()
        clipboardManager.setText(AnnotatedString(caption.text + "\n- " + caption.author))
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1.0f)
    }

    Card(
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(2.dp),
    ) {
        ListItem(
            modifier = Modifier.clickable {

                onCopy(caption)
            },
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
                    onCopy(caption)

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
    captionUiState: HomeUiState,
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

    val symbols = StringBuilder("[")

    selectedLanguages.forEach { languagePref ->
        if (languagePref.isChecked) {

            symbols.append(" " + languagePref.symbol)
        }
    }
    symbols.append(" ]")
    return symbols.toString()
}

