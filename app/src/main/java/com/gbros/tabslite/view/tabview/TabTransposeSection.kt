package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun TabTransposeSection(currentTransposition: Int, transpose: (halfSteps: Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.tab_transpose, currentTransposition),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(vertical = 12.dp)
        )
        IconButton(  // reset transpose
            onClick = { transpose(-currentTransposition) },
            modifier = Modifier
        ) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Reset Transposition", tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(  // transpose down
            onClick = { transpose(-1) },
            Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_remove), contentDescription = "Transpose down")
        }
        Button(  // transpose up
            onClick = { transpose(1) },
            Modifier
                .padding(horizontal = 8.dp)

        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Transpose up")
        }
    }
}

@Composable @Preview
private fun TabTransposeSectionPreview() {
    AppTheme {
        TabTransposeSection(currentTransposition = 0) { }
    }
}