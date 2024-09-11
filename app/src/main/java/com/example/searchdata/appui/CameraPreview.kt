package com.example.searchdata.appui

import androidx.compose.runtime.Composable
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier){
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {context -> PreviewView(context).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
        } },
        modifier = modifier
    )
}