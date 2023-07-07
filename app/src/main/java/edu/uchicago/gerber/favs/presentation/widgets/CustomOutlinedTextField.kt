package edu.uchicago.gerber.favs.presentation.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    title: String,
    placeHolder: String,
    textState: String,
    onTextChange: (String) -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onSearchDone: (() -> Unit)?,
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        placeholder = { Text(text = placeHolder) },
        value = textState,
        onValueChange = { onTextChange(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType, imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (onSearchDone != null) {
                    onSearchDone()
                }
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        label = { Text(title) },
        modifier = Modifier.padding(10.dp, 0.dp),
        textStyle = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp, color = Color.Black,
        ),

        )
}