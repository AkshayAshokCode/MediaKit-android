package com.akshayashokcode.sample_app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun CompressScreen(contentPadding: PaddingValues = PaddingValues()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val layoutDirection = LocalLayoutDirection.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Apply top + horizontal insets here; bottom is passed to children
            .padding(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection)
            )
            .consumeWindowInsets(contentPadding)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                text = { Text("Image") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                text = { Text("Video") })
        }

        val innerPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())
        when (selectedTab) {
            0 -> ImageCompressorScreen(contentPadding = innerPadding)
            1 -> VideoCompressorScreen(contentPadding = innerPadding)
        }
    }
}
