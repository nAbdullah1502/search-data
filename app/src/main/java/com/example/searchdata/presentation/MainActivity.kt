package com.example.searchdata.presentation


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.searchdata.presentation.appui.CustomModalNavigationDrawer
import com.example.searchdata.presentation.gui.DrugScreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import com.example.searchdata.R
import com.example.searchdata.util.PermissionHandler
import com.example.searchdata.presentation.viewmodel.SearchViewModel
import com.example.searchdata.presentation.appui.CameraActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val searchViewModel by viewModels<SearchViewModel>()
    private val permissionHandler = PermissionHandler()
    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val foundMatches = result.data?.getStringExtra("FOUND_MATCHES")
            foundMatches?.let {
                searchViewModel.onSearchQueryChange(it) // Update searchQuery with the found matches
            }
        }
    }
    fun launchCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val isGranted = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        if (isGranted) {
            val intent =Intent(this, CameraActivity::class.java)
            cameraResultLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel.autoUpsertDrugs()
        enableEdgeToEdge()
        setContent {
            SearchDataTheme {
                MainScreen(viewModel = searchViewModel, permissionHandler=permissionHandler, onCameraIconClick = {launchCamera()})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SearchViewModel, permissionHandler: PermissionHandler, onCameraIconClick: ()-> Unit) {
    permissionHandler.RequirePermission()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val state by viewModel.state.collectAsState()
    val showSearchBar by viewModel.showSearchBar.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val searching by viewModel.searching.collectAsState()
    val filteredDrugs by viewModel.filterDrugs.collectAsState()

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
                            IconButton(onClick = onCameraIconClick) {
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
                            DrugScreen(state = state.copy(allDrugs=filteredDrugs), modifier=Modifier.padding(padding))
                        }
                    }
                }
            )
        }
    )
}