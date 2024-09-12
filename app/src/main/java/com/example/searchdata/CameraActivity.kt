package com.example.searchdata

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.searchdata.ui.theme.SearchDataTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.searchdata.access.DrugState
import com.example.searchdata.gui.CameraPreview
import com.example.searchdata.gui.DrugScreen


@AndroidEntryPoint
class CameraActivity: ComponentActivity() {

    val cameraViewModel by viewModels<CameraViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scannedText by cameraViewModel.textScanned.collectAsState()
            val controller = remember {
                LifecycleCameraController(applicationContext).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            }

            SearchDataTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box{
                            CameraPreview(controller = controller, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ){ Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(35.dp)
                        ){
                            Text(
                                text = scannedText,
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                            IconButton(onClick = { takePhoto(controller = controller) }){
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Take photo")
                            }
                            Button(onClick = {
                                mainViewModel.separateAndFilter(scannedText)
                            }){
                                Text("GO! =>")
                            }
                            DisplaySearchQuery(mainViewModel = mainViewModel)
                        }
                    }
                }
            }
        }
    }
    private fun takePhoto(controller: LifecycleCameraController ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                @androidx.annotation.OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val mediaImage = image.image //image.toImage() #java
                    if (mediaImage != null) {
                        val matrix = Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }
                        val rotatedBitmap = Bitmap.createBitmap( // rotatedBitmap = image.toBitmap
                            image.toBitmap(), 0, 0, image.width, image.height, matrix, true
                        )
                        val imageName = InputImage.fromBitmap(rotatedBitmap, 0)
                        cameraViewModel.recognizer.process(imageName).addOnSuccessListener {
                            cameraViewModel.updateScannedText( it.text )
                        }.addOnFailureListener { Log.e("TXT_REC", it.message.toString()) }
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )
    }
}

@Composable
fun DisplaySearchQuery(mainViewModel: MainViewModel) {
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    Text(text = searchQuery)
}