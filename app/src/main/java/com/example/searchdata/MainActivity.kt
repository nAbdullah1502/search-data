package com.example.searchdata


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.searchdata.ui.theme.SearchDataTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.searchdata.appui.CustomModalNavigationDrawer
import com.example.searchdata.appui.DrugScreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.searchdata.appui.CameraActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()
    private val cameraViewModel by viewModels<CameraViewModel>()
    private val permissions = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.autoUpsertDrugs()
        enableEdgeToEdge()
        setContent {
            SearchDataTheme {
                MainScreen(viewModel = mainViewModel, cameraViewModel=cameraViewModel)
            }
        }
    }

    @Composable
    private fun RequirePermission() {
        val showDialog by cameraViewModel.showDialog.collectAsState() // collector to pop up ShowAlertDialog
        val launchAppSettings by cameraViewModel.launchAppSettings.collectAsState() // collector to launch the settings

        val permissionResultActivityLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { result ->
                permissions.forEach { permission ->
                    if (result[permission] == false) { // if denied permission
                        if (!shouldShowRequestPermissionRationale(permission)) {
                            // If permission denied and should not show rationale
                            cameraViewModel.updateLaunchAppSettings(true)
                        }
                        cameraViewModel.updateShowDialog(true) // Show AlertDialog
                    }
                }
            }
        )

        LaunchedEffect(Unit) {
            permissions.forEach { permission ->
                val isGranted = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                if (!isGranted) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        cameraViewModel.updateShowDialog(true) // Show rationale dialog
                    } else {
                        permissionResultActivityLauncher.launch(permissions) // Request permissions
                    }
                }
            }
        }

        if (showDialog) {
            ShowAlertDialog(
                onDismiss = { cameraViewModel.updateShowDialog(false) },
                onConfirm = {
                    cameraViewModel.updateShowDialog(false)
                    if (launchAppSettings) {
                        // Launch app settings for permissions
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null)
                        )
                        startActivity(intent)
                        cameraViewModel.updateLaunchAppSettings(false)
                    } else {
                        // Retry permission request
                        permissionResultActivityLauncher.launch(permissions)
                    }
                }
            )
        }
    }

    @Composable
    fun ShowAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            modifier = Modifier.fillMaxSize(),
            onDismissRequest = onDismiss,
            confirmButton = { Button(onClick = onConfirm) { Text(text = "OK") } },
            title = { Text(text = "Permission Required") },
            text = { Text(text = "Please grant the necessary permissions.") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, cameraViewModel: CameraViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val state by viewModel.state.collectAsState()
    val showSearchBar by viewModel.showSearchBar.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchWord by viewModel.searchWord.collectAsState()

    val searching by viewModel.searching.collectAsState()
    val filter by viewModel.filter.collectAsState()

    // Reference to permission handler
    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val context = LocalContext.current as ComponentActivity
    val permissionResultActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            permissions.forEach { permission ->
                if (result[permission] == false) { // Permission denied
                    if (!context.shouldShowRequestPermissionRationale(permission)) {
                        cameraViewModel.updateLaunchAppSettings(true) // Open app settings
                    }
                    cameraViewModel.updateShowDialog(true) // Show AlertDialog for denied permission
                }
            }
        }
    )
    fun handleCameraIconClick() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val isGranted = permissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        if (isGranted) {
            // If permissions are already granted, proceed with the camera functionality
            // Start the camera or perform the desired action
            val intent =Intent(context, CameraActivity::class.java)
            context.startActivity(intent)
        } else {
            // Request permissions
            permissionResultActivityLauncher.launch(permissions)
        }
    }

    CustomModalNavigationDrawer(
        drawerState = drawerState,
        viewModel = viewModel,
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            if (showSearchBar) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = {viewModel.onSearchQueryChange(it)},
                                    label = { Text("Search") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    "Drug List",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                handleCameraIconClick()

                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = "Camera")
                            }
                        },
                        actions = {
                            if (!showSearchBar) {
                                IconButton(onClick = { viewModel.toggleSearchBar() }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search")
                                }
                            } else {
                                IconButton(onClick = { viewModel.toggleSearchBar() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        }
                    )
                },
                content = { padding ->
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)){
                        if(searching) {
                            Box(modifier=Modifier.fillMaxSize()){
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }else{
                            DrugScreen(state = state.copy(allDrugs=filter), modifier = Modifier.padding(padding))
                        }
                    }
                }
            )
        }
    )
}