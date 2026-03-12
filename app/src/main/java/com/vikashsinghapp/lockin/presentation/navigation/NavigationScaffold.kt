package com.vikashsinghapp.lockin.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScaffold(
    navController: NavHostController,
    shouldShowPermissionRationale: (String) -> Boolean,
    drawerViewModel: DrawerViewModel = hiltViewModel(),
) {
    val isDrawerOpen by drawerViewModel.isDrawerOpen.collectAsState()
    val drawerState = rememberDrawerState(
        if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.TomorrowFocusScreen.route

    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) drawerState.open() else drawerState.close()
    }
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen && !isDrawerOpen) {
            drawerViewModel.openDrawer()
        } else if (!drawerState.isOpen && isDrawerOpen) {
            drawerViewModel.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                onDestinationClicked = { screen ->
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    drawerViewModel.closeDrawer()
                }
            )
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            // systemBarsPadding() <-- if not apply then the input bar is like 20.dp away from bottom when keyboard appear
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(), // Use fillMaxSize to own the window space => the system status bar also
            containerColor = BackgroundDark,
//            topBar = {
//                // This Top App Bar includes the system status bar also.
//                // If I set the background color to red, the system status bar background color changes to red
//                TopAppBar(
//                    title = {
//                        Text(
//                            Constants.ALL_SCREENS.find { it.route == currentRoute }?.screenName
//                                ?: "Lock-In", color = Color.White, fontSize = 18.sp
//                        )
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = { drawerViewModel.toggleDrawer() }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
//                        }
//                    },
//                    actions = {
//                        // ONLY show these actions if we are currently on the Journal Screen
//                        if (currentRoute == Screen.JournalScreen.route) {
//
//                            // 1. Grab the ViewModel scoped specifically to the Journal Screen's backstack entry
//                            val journalEntry = remember(navBackStackEntry) {
//                                navController.getBackStackEntry(Screen.JournalScreen.route)
//                            }
//                            val journalViewModel: com.vikashsinghapp.lockin.presentation.journal.JournalViewModel =
//                                hiltViewModel(journalEntry)
//
//                            // 2. Read the active category state
//                            val activeCategory by journalViewModel.selectedCategory.collectAsState()
//
//                            val context = LocalContext.current
//                            var showExportMenu by remember { mutableStateOf(false) }
//
//                            // 3. Draw the Export UI directly in the global Top Bar
//                            Row(
//                                modifier = Modifier.padding(end = 8.dp),
//                                horizontalArrangement = Arrangement.End,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                IconButton(onClick = { showExportMenu = true }) {
//                                    Icon(
//                                        Icons.Default.Share,
//                                        contentDescription = "Export",
//                                        tint = Color.White
//                                    )
//                                }
//                                DropdownMenu(
//                                    expanded = showExportMenu,
//                                    onDismissRequest = { showExportMenu = false },
//                                ) {
//                                    DropdownMenuItem(
//                                        text = {
//                                            Text(
//                                                "Export All",
//                                                color = MaterialTheme.colorScheme.onBackground
//                                            )
//                                        },
//                                        onClick = {
//                                            showExportMenu = false
//                                            journalViewModel.exportJournals(
//                                                context,
//                                                exportAll = true
//                                            )
//                                        }
//                                    )
//                                    if (activeCategory != "All") {
//                                        DropdownMenuItem(
//                                            text = {
//                                                Text(
//                                                    "Export '${activeCategory}'",
//                                                    color = MaterialTheme.colorScheme.onBackground
//                                                )
//                                            },
//                                            onClick = {
//                                                showExportMenu = false
//                                                journalViewModel.exportJournals(
//                                                    context,
//                                                    exportAll = false
//                                                )
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
//                )
//            },
            snackbarHost = {
                // reuse default SnackbarHost to have default animation and timing handling
                SnackbarHost(snackbarHostState) { data ->
                    // custom snackbar with the custom colors
                    Snackbar(
                        actionColor = MaterialTheme.colorScheme.onBackground,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        snackbarData = data,
                        dismissActionContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
        ) { innerPadding ->
            Navigation(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                shouldShowPermissionRationale = shouldShowPermissionRationale,
                snackbarHostState = snackbarHostState,
                onOpenDrawer = { drawerViewModel.openDrawer() }
            )
        }
    }
}
