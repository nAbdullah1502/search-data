package com.example.searchdata.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchdata.MainViewModel
import com.example.searchdata.access.DrugState

@Composable
fun DrugScreen(state: DrugState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.allDrugs) { drug ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {}, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${drug.drugName} \n * ${drug.farmGroup}",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow icon"
                )

            }
        }
    }
}
