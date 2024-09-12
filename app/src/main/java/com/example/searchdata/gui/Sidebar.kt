package com.example.searchdata.gui


import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.searchdata.MainViewModel
import com.example.searchdata.access.DrugEvent
import com.example.searchdata.access.SortType


@Composable
fun CustomModalNavigationDrawer(
    drawerState: DrawerState,
    viewModel: MainViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Button(onClick = {
                    val intent =Intent(context, FarmGroupActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Text("Farm TOPAR")
                }
                Text(text="Sort by: ")
                Spacer(modifier = Modifier.padding(16.dp))
                SortType.values().forEach { sortType ->
                    Text(
                        text = sortType.name,
                        modifier = Modifier
                            .clickable {
                                viewModel.onEvent(DrugEvent.SortDrugs(sortType))
                            }
                            .padding(16.dp)
                    )
                }

            }
        },
        content = {content(PaddingValues(0.dp))}
    )
}

