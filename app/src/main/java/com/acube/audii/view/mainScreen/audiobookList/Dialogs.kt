package com.acube.audii.view.mainScreen.audiobookList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acube.audii.ADD_TYPE
import com.acube.audii.R

@Composable
fun AddAudiobookDialog(
    onDismiss: () -> Unit,
    onAddTypeSelected: (ADD_TYPE) -> Unit
){
    var selectedOption by remember { mutableStateOf(ADD_TYPE.ONE_FROM_FILE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_audiobook_dialog_title)) },
        text = {
            Column {
                ADD_TYPE.entries.forEach { addType ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selectedOption = addType }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = (addType == selectedOption), onClick = { selectedOption = addType })
                        Text(text = stringResource(id = when(addType){
                            ADD_TYPE.ONE_FROM_FILE -> R.string.add_one_from_file
                            ADD_TYPE.ONE_FROM_FOLDER -> R.string.add_one_from_folder
                            ADD_TYPE.MULTIPLE_FROM_FOLDER -> R.string.add_multiple_from_folder
                        }), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAddTypeSelected(selectedOption)
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}