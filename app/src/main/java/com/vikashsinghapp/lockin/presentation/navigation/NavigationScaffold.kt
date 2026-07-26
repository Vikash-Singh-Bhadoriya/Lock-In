package com.vikashsinghapp.lockin.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vikashsinghapp.lockin.presentation.core.HideNavigationBar
import com.vikashsinghapp.lockin.ui.theme.BackgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScaffold(
    navController: NavHostController,
    startDestination: String,
    onCompleteOnboarding: () -> Unit,
    shouldShowPermissionRationale: (String) -> Boolean,
    drawerViewModel: DrawerViewModel = hiltViewModel(),
) {
    val isDrawerOpen by drawerViewModel.isDrawerOpen.collectAsState()
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val orientation = configuration.orientation

    // By recreating DrawerState on orientation change, we bypass the Compose constraint-snapping bug 
    // and rememberSaveable persistence. The drawer will synchronously render as Closed on rotation.
    val drawerState = remember(orientation) {
        // Also synchronously reset the ViewModel so it doesn't try to animate the drawer back open
        // if the user happened to have it open before they rotated.
        drawerViewModel.closeDrawer()
        androidx.compose.material3.DrawerState(DrawerValue.Closed)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.TomorrowFocusScreen.route

    // Extract the pure base route (strips away ?date= or /taskId)
    val baseRoute = currentRoute.substringBefore("?").substringBefore("/")

    // Hide Android nav bar on active focus screen only.
    HideNavigationBar(
        enabled = baseRoute == Screen.ActiveFocusScreen.route,
    )

    // ViewModel drives drawer open/close — one-way sync
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) drawerState.open() else drawerState.close()
    }
    // Sync gesture-close back to ViewModel (e.g. user swiped drawer shut)
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed && isDrawerOpen) {
            drawerViewModel.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable swipe-to-open on the onboarding screen, active focus screen
        // Screen.ActiveFocusScreen.route + "/{taskId}", the currentRoute at runtime will actually be something like "active_focus_screen/123".
        gesturesEnabled = baseRoute != Screen.OnboardingScreen.route && baseRoute != Screen.ActiveFocusScreen.route,
        drawerContent = {
            DrawerContent(
                currentRoute = baseRoute,
                onDestinationClicked = { screen ->
                    if (screen.route != baseRoute) {
                        navController.navigate(screen.route) {
                            // Use findStartDestination().id to prevent the navigation from being swallowed
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                    drawerViewModel.closeDrawer()
                }
            )
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val insetsSides = if (baseRoute == Screen.ActiveFocusScreen.route) {
            WindowInsetsSides.Top
        } else {
            WindowInsetsSides.Vertical
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.systemBars.only(insetsSides),
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
                startDestination = startDestination,
                onCompleteOnboarding = onCompleteOnboarding,
                shouldShowPermissionRationale = shouldShowPermissionRationale,
                snackbarHostState = snackbarHostState,
                onOpenDrawer = { drawerViewModel.openDrawer() }
            )
        }
    }
}
