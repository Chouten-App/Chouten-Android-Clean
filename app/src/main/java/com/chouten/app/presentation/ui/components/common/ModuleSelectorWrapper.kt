package com.chouten.app.presentation.ui.components.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chouten.app.R
import com.chouten.app.common.UiText
import com.chouten.app.domain.model.ModuleModel
import com.chouten.app.domain.model.SnackbarModel
import com.chouten.app.domain.proto.ModulePreferences
import com.chouten.app.domain.proto.appearanceDatastore
import com.chouten.app.domain.proto.moduleDatastore
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Wraps the content within a [BottomSheetScaffold] which displays a [ModuleSelector] in the sheet.
 * A [FloatingActionButton] is also displayed above the wrapped content which opens the sheet on
 * click.
 * @param viewModel The [ChoutenAppViewModel] which contains the [StateFlow] of [ModuleModel]s.
 * @param snackbarLambda The lambda used for emitting [SnackbarModel]s to the main Scaffold's
 * [SnackbarHost].
 * @param content The content to be wrapped within the [BottomSheetScaffold].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleSelectorWrapper(
    viewModel: ChoutenAppViewModel,
    snackbarLambda: (SnackbarModel) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = SheetValue.Hidden,
            skipPartiallyExpanded = false,
        )
    )

    val context = LocalContext.current
    var selectedModule by rememberSaveable {
        mutableStateOf(
            UiText.StringRes(R.string.no_module_selected).string(context)
        )
    }

    val modulePreferences by context.moduleDatastore.data.collectAsState(initial = ModulePreferences.DEFAULT)

    LaunchedEffect(modulePreferences) {
        viewModel.modules.collectLatest {
            it.firstOrNull { module ->
                module.id == modulePreferences.selectedModuleId
            }?.name?.let { moduleName -> selectedModule = moduleName } ?: run {
                selectedModule = UiText.StringRes(R.string.no_module_selected).string(context)
            }
        }
    }


    BottomSheetScaffold(scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetTonalElevation = 3.dp, // Match the elevation of the navbar
        sheetContent = {
            ModuleSelector(modules = viewModel.modules, onModuleSelected = {
                coroutineScope.launch {
                    context.moduleDatastore.updateData { preferences ->
                        preferences.copy(selectedModuleId = it.id)
                    }
                    scaffoldState.bottomSheetState.hide()
                }
            })
            Divider(
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }) {
        content()
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = selectedModule,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


/**
 * Displays a list of [ModuleModel]s in a [LazyColumn] with a [ListItem] for each module.
 * @param modules The [StateFlow] of [ModuleModel]s to be displayed.
 * @param onModuleSelected The lambda to be called when a module is selected (clicked).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModuleSelector(
    modules: StateFlow<List<ModuleModel>>, onModuleSelected: (ModuleModel) -> Unit = {}
) {
    val collectedModules by modules.collectAsState()
    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = UiText.StringRes(R.string.module_selection).string(),
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = UiText.StringRes(R.string.module_bottomsheet_description).string(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
        items(collectedModules, key = { it.id }) { module ->
            ModuleItem(
                modifier = Modifier.animateItemPlacement(),
                module = module,
                onModuleSelected = onModuleSelected
            )
        }
    }
}

/**
 * Displays a [ModuleModel] in a [ListItem].
 * @param modifier The [Modifier] to be applied to the [ListItem].
 * @param module The [ModuleModel] to be displayed.
 * @param onModuleSelected The lambda to be called when the [ListItem] is selected (clicked).
 */
@Composable
fun ModuleItem(
    modifier: Modifier = Modifier, module: ModuleModel, onModuleSelected: (ModuleModel) -> Unit = {}
) {
    // Get the appearance preferences from the datastore
    // so we can determine if the user wants to use the module's custom
    // colors
    val appearancePreferences by LocalContext.current.appearanceDatastore.data.collectAsState(
        null
    )

    // Convert the hex color strings to Color objects
    // e.g #FFFFFFF => 0xFFFFFFFF
    // If the color string is invalid, use the default color
    // for the ListItem

    val surfaceColor = try {
        Color("FF${module.metadata.backgroundColor.removePrefix("#")}".toLong(16))
    } catch (e: Exception) {
        ListItemDefaults.containerColor
    }

    val onSurfaceColor = try {
        Color("FF${module.metadata.foregroundColor.removePrefix("#")}".toLong(16))
    } catch (e: Exception) {
        ListItemDefaults.contentColor
    }

    // If the user wants to use the module's custom colors,
    // use the module's colors for the ListItem
    // Otherwise, use the default colors
    val colors = if (appearancePreferences?.useModuleColors == true) ListItemDefaults.colors(
        containerColor = surfaceColor,
        headlineColor = onSurfaceColor,
        supportingColor = onSurfaceColor,
    ) else ListItemDefaults.colors()

    ListItem(colors = colors,
        headlineContent = { Text(module.name, fontWeight = FontWeight.SemiBold) },
        tonalElevation = 5.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(65.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable {
                onModuleSelected(module)
            },
        leadingContent = {
            AsyncImage(
                model = module.metadata.icon,
                contentDescription = "${module.name} icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .requiredSize(40.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
        },
        supportingContent = {
            Text(
                text = "${module.metadata.author} v${module.version}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        })
}