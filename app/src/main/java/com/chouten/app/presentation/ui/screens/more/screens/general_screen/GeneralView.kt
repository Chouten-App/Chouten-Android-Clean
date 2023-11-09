package com.chouten.app.presentation.ui.screens.more.screens.general_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chouten.app.R
import com.chouten.app.common.MoreNavGraph
import com.chouten.app.common.UiText
import com.chouten.app.domain.proto.crashReportStore
import com.chouten.app.presentation.ui.ChoutenAppViewModel
import com.chouten.app.presentation.ui.components.preferences.PreferenceToggle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@MoreNavGraph()
@Destination()
@Composable
fun GeneralView(
    appViewModel: ChoutenAppViewModel, navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val crashReportStore by context.crashReportStore.data.collectAsState(null)

    Scaffold(topBar = {
        TopAppBar(title = { Text(UiText.StringRes(R.string.general).string()) }, navigationIcon = {
            Icon(imageVector = Icons.Filled.ArrowBack,
                contentDescription = UiText.StringRes(R.string.back).string(),
                modifier = Modifier
                    .clickable {
                        navigator.popBackStack()
                    }
                    .clip(MaterialTheme.shapes.small)
                    .padding(
                        horizontal = 12.dp
                    ))
        })
    }) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            PreferenceToggle(
                headlineContent = {
                    Text(
                        UiText.StringRes(R.string.crash_reports_preference_title).string()
                    )
                },
                supportingContent = {
                    Text(
                        UiText.StringRes(R.string.crash_reports_preference_description).string()
                    )
                },
                onToggle = { isEnabled ->
                    appViewModel.runAsync {
                        context.crashReportStore.updateData { preferences ->
                            preferences.copy(enabled = isEnabled)
                        }
                    }
                },
                initial = crashReportStore?.enabled ?: true,
            )
        }
    }
}