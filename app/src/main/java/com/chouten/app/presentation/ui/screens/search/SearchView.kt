package com.chouten.app.presentation.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import com.chouten.app.R
import com.chouten.app.common.LocalAppPadding
import com.chouten.app.common.Navigation
import com.chouten.app.common.Resource
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.common.ModuleSelectorWrapper
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.internal.managers.FragmentComponentManager.findActivity
import java.net.URLEncoder

@Composable
@Destination(
    route = Navigation.SearchRoute
)
fun SearchView(
    navigator: DestinationsNavigator,
    appViewModel: ChoutenAppViewModel,
    snackbarLambda: (SnackbarModel) -> Unit,
) {

    val lazygridState = rememberLazyGridState()
    val context = LocalContext.current
    val moduleStore by context.moduleDatastore.data.collectAsState(null)

    // Persist the ViewModel during recompositions
    val searchViewModel: SearchViewModel = hiltViewModel(
        findActivity(context) as ViewModelStoreOwner
    )

    val previousSearchQuery by searchViewModel.savedStateHandle.getStateFlow("lastSearchQuery", "")
        .collectAsState()
    val currentSearchQuery by searchViewModel.savedStateHandle.getStateFlow("searchQuery", "")
        .collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState(Resource.Uninitialized())

    // Update the search results when the search query or the selected module changes
    LaunchedEffect(currentSearchQuery, moduleStore?.selectedModuleId) {
        if (previousSearchQuery != currentSearchQuery || moduleStore?.selectedModuleId != searchViewModel.lastUsedModule) {
            searchViewModel.search()
        }
    }

    ModuleSelectorWrapper(viewModel = appViewModel) {
        AnimatedVisibility(
            visible = moduleStore?.selectedModuleId?.isNotBlank() == true,
            enter = fadeIn(),
            modifier = Modifier.padding(LocalAppPadding.current)
        ) {
            Column {
                SearchTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 16.dp, bottom = 8.dp, end = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), CircleShape
                        ), viewModel = searchViewModel
                )

                when (searchResults) {
                    is Resource.Success -> {
                        if(!searchResults.data.isNullOrEmpty()){
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(100.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                state = lazygridState
                            ) {
                                items(items = searchResults.data ?: listOf()) {
                                    SearchResultItem(
                                        item = it, onClick = { _, _ -> }
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "(×﹏×)", fontSize = MaterialTheme.typography.headlineLarge.fontSize
                                )
                                Text(
                                    UiText.StringRes(R.string.search_no_results).string(),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    is Resource.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }

                    is Resource.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "(×﹏×)", fontSize = MaterialTheme.typography.headlineLarge.fontSize
                            )
                            Text(
                                UiText.StringRes(R.string.search_error).string(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "(・・ ) ?",
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize
                            )
                            Text(
                                UiText.StringRes(R.string.search_page_no_query).string(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Search Text Field
 * Search field for querying the [SearchViewModel]
 * @param modifier Modifier
 * @param viewModel SearchViewModel
 */
@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current

    // Local state for the query
    // which is unaffected by the debounce & distinctUntilChanged
    var localSearchQuery by rememberSaveable { mutableStateOf(viewModel.searchQuery) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextField(
            value = localSearchQuery,
            onValueChange = { localSearchQuery = it; viewModel.searchQuery = it },
            placeholder = {
                Text(
                    text = UiText.StringRes(R.string.search).string(),
                )
            },
            leadingIcon = {
                IconButton(modifier = Modifier
                    .padding(start = 8.dp)
                    .requiredWidth(IntrinsicSize.Max),
                    onClick = {}) {
                    Icon(
                        Icons.Default.Search,
                        UiText.StringRes(R.string.search).string(),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }
}

/**
 * Search Result Item
 * @param modifier Modifier
 * @param item SearchResult
 * @param onClick (title: String, url: String) -> Unit
 * [onClick] is a lambda that takes a URL Encoded Title & URL  of the [SearchResult]
*/
@Composable
fun SearchResultItem(
    modifier: Modifier = Modifier, item: SearchResult, onClick: (title: String, url: String) -> Unit
) {
    Column(
        modifier
            .padding(vertical = 6.dp)
            .clickable {
                val title = URLEncoder.encode(item.title, "UTF-8")
                val url = URLEncoder.encode(item.url, "UTF-8")
                onClick(title, url)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .heightIn(100.dp, 160.dp)
                .clip(MaterialTheme.shapes.small)
        ) {
            item.indicatorText?.let {
                if (it.isBlank()) return@let
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.small.copy(
                                topStart = ZeroCornerSize,
                                topEnd = ZeroCornerSize,
                                bottomEnd = ZeroCornerSize
                            )
                        )
                        .padding(8.dp, 4.dp)
                        .zIndex(2F)
                )
            }
            AsyncImage(
                model = item.img,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.FillBounds
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            item.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .fillMaxWidth(0.9F)
                .wrapContentWidth(Alignment.Start)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9F)
                .wrapContentWidth(Alignment.End)
        ) {
            Text(
                text = item.currentCount?.toString() ?: "~",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
            Text(
                " | ",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
            Text(
                text = item.totalCount?.toString() ?: "~",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
            )
        }
    }
}