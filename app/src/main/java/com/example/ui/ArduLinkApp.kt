package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.resolveLocale
import com.example.data.withAppLanguage
import com.example.model.ConnectionState
import com.example.ui.components.BluetoothConnectionSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TopStatusBar
import com.example.ui.screens.CarControlScreen
import com.example.ui.screens.DevicesControlScreen
import com.example.ui.screens.GamepadScreen
import com.example.ui.screens.Keypad4x4Screen
import com.example.ui.screens.MultiSwitchScreen
import com.example.ui.screens.SensorsTelemetryScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.VoiceTerminalScreen
import com.example.ui.theme.ArduLinkTheme
import com.example.ui.theme.LocalTelemetryColors

object ArduRoutes {
    const val CAR = "car_control"
    const val CONTROL = "multi_control"
    const val CONTROLLER = "gamepad_keypad"
    const val SENSORS = "sensors_telemetry"
    const val TERMINAL = "serial_voice_terminal"
}

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navItems = listOf(
    NavDestinationItem(
        route = ArduRoutes.CAR,
        labelRes = R.string.nav_car,
        selectedIcon = Icons.Rounded.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        testTag = "nav_tab_car"
    ),
    NavDestinationItem(
        route = ArduRoutes.CONTROL,
        labelRes = R.string.nav_control,
        selectedIcon = Icons.Rounded.ToggleOn,
        unselectedIcon = Icons.Outlined.ToggleOn,
        testTag = "nav_tab_control"
    ),
    NavDestinationItem(
        route = ArduRoutes.CONTROLLER,
        labelRes = R.string.nav_controller,
        selectedIcon = Icons.Rounded.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        testTag = "nav_tab_controller"
    ),
    NavDestinationItem(
        route = ArduRoutes.SENSORS,
        labelRes = R.string.nav_sensors,
        selectedIcon = Icons.Rounded.ShowChart,
        unselectedIcon = Icons.Outlined.ShowChart,
        testTag = "nav_tab_sensors"
    ),
    NavDestinationItem(
        route = ArduRoutes.TERMINAL,
        labelRes = R.string.nav_terminal,
        selectedIcon = Icons.Rounded.Terminal,
        unselectedIcon = Icons.Outlined.Terminal,
        testTag = "nav_tab_terminal"
    )
)

@Composable
fun ArduLinkApp(viewModel: ArduLinkViewModel) {
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val lineEnding by viewModel.lineEnding.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val hasPermissions by viewModel.hasBluetoothPermissions.collectAsStateWithLifecycle()
    val isBtEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val pairedDevices by viewModel.pairedDevices.collectAsStateWithLifecycle()
    val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()

    val carState by viewModel.carState.collectAsStateWithLifecycle()
    val circuitDevices by viewModel.circuitDevices.collectAsStateWithLifecycle()
    val multiSwitches by viewModel.multiSwitches.collectAsStateWithLifecycle()
    val voiceRules by viewModel.voiceRules.collectAsStateWithLifecycle()
    val sendUnmatchedVoice by viewModel.sendUnmatchedVoice.collectAsStateWithLifecycle()
    val lastVoiceMatch by viewModel.lastVoiceMatch.collectAsStateWithLifecycle()
    val lastGamepadTx by viewModel.lastGamepadTx.collectAsStateWithLifecycle()
    val telemetryState by viewModel.telemetryState.collectAsStateWithLifecycle()
    val serialLogs by viewModel.serialLogs.collectAsStateWithLifecycle()

    var showBluetoothSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var controlSubTab by rememberSaveable { mutableIntStateOf(0) }
    var controllerSubTab by rememberSaveable { mutableIntStateOf(0) }
    var terminalSubTab by rememberSaveable { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val baseContext = LocalContext.current
    val baseConfig = LocalConfiguration.current
    val localizedContext = remember(baseContext, appLanguage, baseConfig) {
        baseContext.withAppLanguage(appLanguage)
    }
    val localizedConfig = remember(localizedContext) {
        localizedContext.resources.configuration
    }
    val speechLocaleTag = remember(appLanguage) {
        val loc = resolveLocale(appLanguage)
        if (loc.language.equals("tr", ignoreCase = true)) "tr-TR" else "en-US"
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig
    ) {
        ArduLinkTheme(themeMode = themeMode) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val telemetryColors = LocalTelemetryColors.current

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val useNavRail = maxWidth >= 640.dp

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        Column {
                            TopStatusBar(
                                connectionState = connectionState,
                                themeMode = themeMode,
                                onOpenBluetoothSheet = {
                                    viewModel.refreshBluetoothEnvironment()
                                    showBluetoothSheet = true
                                },
                                onToggleTheme = viewModel::cycleDarkLightTheme,
                                onOpenSettings = { showSettingsSheet = true }
                            )

                            AnimatedVisibility(visible = connectionState is ConnectionState.SimulationMode) {
                                Surface(
                                    color = telemetryColors.warningContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showBluetoothSheet = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Science,
                                            contentDescription = null,
                                            tint = telemetryColors.warning,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.sim_banner_active),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = telemetryColors.warning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (!useNavRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                navItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(item.labelRes)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(item.labelRes),
                                                maxLines = 1
                                            )
                                        },
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .navigationBarsPadding()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    )
                            ) {
                                Spacer(modifier = Modifier.weight(0.15f))
                                navItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = stringResource(item.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(item.labelRes)) },
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = ArduRoutes.CAR,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            composable(ArduRoutes.CAR) {
                                CarControlScreen(
                                    carState = carState,
                                    onSetControlMode = viewModel::setCarControlMode,
                                    onTwoHandThrottle = viewModel::setTwoHandThrottle,
                                    onTwoHandSteer = viewModel::setTwoHandSteer,
                                    onJoystickMove = viewModel::updateCarJoystick,
                                    onDirectionPress = viewModel::setCarDirection,
                                    onSpeedPreview = viewModel::updateCarSpeedPreview,
                                    onSpeedCommit = viewModel::commitCarSpeed,
                                    onEmergencyStop = viewModel::emergencyStop,
                                    onToggleHeadlights = viewModel::toggleHeadlights,
                                    onToggleHazards = viewModel::toggleHazards,
                                    onHornChange = viewModel::setHornActive,
                                    onToggleAutonomous = viewModel::toggleAutonomousMode
                                )
                            }

                            composable(ArduRoutes.CONTROL) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SubTabStrip(
                                        options = listOf(
                                            stringResource(R.string.tab_multi_switch) to Icons.Rounded.GridView,
                                            stringResource(R.string.tab_devices) to Icons.Rounded.Tune
                                        ),
                                        selectedIndex = controlSubTab,
                                        onSelect = { controlSubTab = it },
                                        testTagPrefix = "subtab_control"
                                    )
                                    if (controlSubTab == 0) {
                                        MultiSwitchScreen(
                                            switches = multiSwitches,
                                            onToggleSwitch = viewModel::toggleMultiSwitch,
                                            onUpdateSwitchConfig = viewModel::updateMultiSwitchConfig,
                                            onSetAll = viewModel::setAllMultiSwitches,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        DevicesControlScreen(
                                            devices = circuitDevices,
                                            onToggleDevice = viewModel::toggleCircuitDevice,
                                            onTriggerMomentary = viewModel::triggerMomentaryDevice,
                                            onSliderPreview = viewModel::updateDeviceSliderPreview,
                                            onSliderCommit = viewModel::commitDeviceSlider,
                                            onAllOff = viewModel::allDevicesOff,
                                            onAddDevice = viewModel::addCustomDevice,
                                            onDeleteDevice = viewModel::deleteDevice,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            composable(ArduRoutes.CONTROLLER) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SubTabStrip(
                                        options = listOf(
                                            stringResource(R.string.tab_gamepad) to Icons.Rounded.SportsEsports,
                                            stringResource(R.string.tab_keypad) to Icons.Rounded.Dialpad
                                        ),
                                        selectedIndex = controllerSubTab,
                                        onSelect = { controllerSubTab = it },
                                        testTagPrefix = "subtab_controller"
                                    )
                                    if (controllerSubTab == 0) {
                                        GamepadScreen(
                                            lastTx = lastGamepadTx,
                                            onSendCommand = viewModel::sendGamepadCommand,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Keypad4x4Screen(
                                            onSendKey = viewModel::sendKeypadCommand,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            composable(ArduRoutes.SENSORS) {
                                SensorsTelemetryScreen(
                                    telemetryState = telemetryState,
                                    onSelectChannel = viewModel::selectSensorChannel,
                                    onTogglePause = viewModel::toggleTelemetryPause,
                                    onClearHistory = viewModel::clearTelemetryHistory
                                )
                            }

                            composable(ArduRoutes.TERMINAL) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SubTabStrip(
                                        options = listOf(
                                            stringResource(R.string.tab_serial) to Icons.Rounded.Terminal,
                                            stringResource(R.string.tab_voice) to Icons.Rounded.Mic
                                        ),
                                        selectedIndex = terminalSubTab,
                                        onSelect = { terminalSubTab = it },
                                        testTagPrefix = "subtab_terminal"
                                    )
                                    if (terminalSubTab == 0) {
                                        TerminalScreen(
                                            logs = serialLogs,
                                            onSendCommand = viewModel::sendManualSerialCommand,
                                            onClearLogs = viewModel::clearSerialLogs,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        VoiceTerminalScreen(
                                            speechLocaleTag = speechLocaleTag,
                                            rules = voiceRules,
                                            sendUnmatchedRaw = sendUnmatchedVoice,
                                            lastVoiceMatch = lastVoiceMatch,
                                            onVoiceTranscript = viewModel::processVoiceTranscript,
                                            onAddRule = viewModel::addVoiceRule,
                                            onDeleteRule = viewModel::deleteVoiceRule,
                                            onToggleSendUnmatched = viewModel::setSendUnmatchedVoice,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showBluetoothSheet) {
                    BluetoothConnectionSheet(
                        connectionState = connectionState,
                        hasPermissions = hasPermissions,
                        isBluetoothSupported = viewModel.isBluetoothSupported(),
                        isBluetoothEnabled = isBtEnabled,
                        isScanning = isScanning,
                        pairedDevices = pairedDevices,
                        discoveredDevices = discoveredDevices,
                        requiredPermissions = viewModel.requiredBluetoothPermissions(),
                        onPermissionsUpdated = viewModel::refreshBluetoothEnvironment,
                        onRefreshEnvironment = viewModel::refreshBluetoothEnvironment,
                        onStartScan = viewModel::startDiscovery,
                        onStopScan = viewModel::stopDiscovery,
                        onConnectDevice = viewModel::connectToDevice,
                        onDisconnect = viewModel::disconnect,
                        onStartSimulation = viewModel::startSimulationMode,
                        onDismiss = {
                            viewModel.stopDiscovery()
                            showBluetoothSheet = false
                        }
                    )
                }

                if (showSettingsSheet) {
                    SettingsSheet(
                        appLanguage = appLanguage,
                        themeMode = themeMode,
                        lineEnding = lineEnding,
                        onSelectLanguage = viewModel::setAppLanguage,
                        onSelectTheme = viewModel::setThemeMode,
                        onSelectLineEnding = viewModel::setLineEnding,
                        onDismiss = { showSettingsSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubTabStrip(
    options: List<Pair<String, ImageVector>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    testTagPrefix: String
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        options.forEachIndexed { index, (label, icon) ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("${testTagPrefix}_$index")
            ) {
                Text(label, maxLines = 1)
            }
        }
    }
}
