package com.example.searchdata


import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraViewModel: ViewModel() {
    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()
    private val _launchAppSettings = MutableStateFlow(false)
    val launchAppSettings = _launchAppSettings.asStateFlow()
    fun updateShowDialog(show:Boolean){
        _showDialog.update { show }
    }
    fun updateLaunchAppSettings(show: Boolean){
        _launchAppSettings.update { show }
    }
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var _textScanned = MutableStateFlow("")
    var textScanned = _textScanned.asStateFlow()
    fun updateScannedText(newText: String) {
        _textScanned.value = newText
    }

}