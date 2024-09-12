package com.example.searchdata.access

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.searchdata.CameraViewModel

class PermissionHandler {

    private val requiredPermission = arrayOf(Manifest.permission.CAMERA)

    @Composable
    fun RequirePermission() {
        val context = LocalContext.current as ComponentActivity
        val cameraViewModel = context.viewModels<CameraViewModel>().value

        val permissionResultActivityLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { result ->
                requiredPermission.forEach { permission ->
                    if (result[permission] == false) { // Permission denied
                        if (!context.shouldShowRequestPermissionRationale(permission)) {
                            cameraViewModel.updateLaunchAppSettings(true) // Open app settings
                        }
                        cameraViewModel.updateShowDialog(true) // Show AlertDialog for denied permission
                    }
                }
            }
        )

        val showDialog by cameraViewModel.showDialog.collectAsState()
        val launchAppSettings by cameraViewModel.launchAppSettings.collectAsState()

        LaunchedEffect(Unit) {
            requiredPermission.forEach { permission ->
                val isGranted = checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                if (!isGranted) {
                    if (shouldShowRequestPermissionRationale(context, permission)) {
                        cameraViewModel.updateShowDialog(true) // Show rationale dialog
                    } else {
                        permissionResultActivityLauncher.launch(requiredPermission) // Request permissions
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
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent) // Use context to start activity
                        cameraViewModel.updateLaunchAppSettings(false)
                    } else {
                        // Retry permission request
                        permissionResultActivityLauncher.launch(requiredPermission)
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