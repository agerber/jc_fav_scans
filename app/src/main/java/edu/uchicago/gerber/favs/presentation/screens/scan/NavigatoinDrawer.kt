package edu.uchicago.gerber.favs.presentation.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NavigationDrawer(
    confidence: Int,
    inferenceTime: Long,
    delegate: Int = 0,
    onAddCon: () -> Unit,
    onSubtractCon: () -> Unit,
    onDelegateChange: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Inference Time:")
            Text(text = "$inferenceTime ms")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Confidence Threshold:",
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "-",
                    modifier = Modifier
                        .clickable { onSubtractCon() }
                        .background(Color.LightGray)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                Text(text = "${confidence}%")
                Text(
                    text = "+",
                    modifier = Modifier
                        .clickable { onAddCon() }
                        .background(Color.LightGray)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            val delegates = listOf("CPU", "GPU", "NNAPI")
            Text(
                text = "Delegate:",
                modifier = Modifier.weight(1f)
            )
            DropdownMenuItem(
                onClick = {
                    expanded = true
                },
                text = {
                    Text(text = delegates[delegate])
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.weight(1f)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.weight(1f)
            ) {
                delegates.forEachIndexed { index, delegate ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onDelegateChange(index)
                        },
                        text = {
                            Text(text = delegate)
                        }
                    )
                }
            }
        }
    }
}