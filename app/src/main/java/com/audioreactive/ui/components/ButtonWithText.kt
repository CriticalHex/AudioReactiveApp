package com.audioreactive.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ButtonWithText(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Button(modifier = modifier, onClick = onClick) {
        Text(text = text)
    }
}

@Composable
@Preview
private fun PreviewButtonWithText() {
    Scaffold { innerPadding ->
        ButtonWithText(text = "Test Text") {  }
    }
}