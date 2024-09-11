package com.example.searchdata.appui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchdata.MainViewModel
import com.example.searchdata.ui.theme.SearchDataTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FarmGroupActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainViewModel by viewModels<MainViewModel>()
        setContent {
            SearchDataTheme {
                FarmGroupActivityScreen(
                    farmGroups = mainViewModel.farmGroups
                )
            }
        }
    }
}

@Composable
fun FarmGroupActivityScreen(farmGroups: List<String>) {
    LazyColumn {
        items(farmGroups) { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow icon"
                )
            }
        }
    }
}