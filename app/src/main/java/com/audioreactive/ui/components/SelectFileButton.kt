package com.audioreactive.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SelectFileButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Text("Select File")
//        TODO("Change this text to an xml string")
    }
}

@Composable
@Preview
private fun PreviewSelectFileButton() {
    Scaffold { innerPadding ->
        StartAudioCaptureButton {  }
    }
}