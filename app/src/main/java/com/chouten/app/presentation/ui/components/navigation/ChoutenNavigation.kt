package com.chouten.app.presentation.ui.components.navigation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoutenNavigation(
    navigator: NavController, navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    NavigationBar {
        val activeDestination by remember { navigationViewModel.getActiveDestination() }

        navigationViewModel.bottomDestinations.forEach { destination ->
            Log.d(
                "Navigation",
                "destination: ${destination.route}. activeDestination: $activeDestination"
            )
            val isSelected = destination.route == activeDestination
            val icon: @Composable () -> Unit = {
                Icon(
                    imageVector = if (isSelected) destination.icon else destination.inactiveIcon,
                    contentDescription = destination.label.string(),
                    tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // TODO: Implement badge count
            val badgeCount = 0
            NavigationBarItem(selected = isSelected, alwaysShowLabel = true, onClick = {
                navigator.navigate(destination.direction).also {
                    navigationViewModel.setActiveDestination(destination.route)
                }
            }, icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (badgeCount > 0) {
                        BadgedBox(badge = {
                            Badge(
                                modifier = Modifier.offset((-2).dp, 2.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                val count = if (badgeCount > 99) "99+" else badgeCount.toString()
                                Text(text = count,
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.semantics {
                                        contentDescription = "$count new notifications"
                                    })
                            }
                        }) {
                            icon()
                        }
                    } else {
                        icon()
                    }
                }
            }, label = {
                Text(
                    text = destination.label.string(),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            })
        }
    }
}
