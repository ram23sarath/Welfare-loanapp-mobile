package com.ijreddy.loanapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.ijreddy.loanapp.data.local.entity.CustomerEntity

/**
 * A search field for finding and selecting customers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<CustomerEntity>,
    onCustomerSelected: (CustomerEntity) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Search by name or phone"
) {
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Expand dropdown when query changes and we have results
    // But only if we are not "searching" (loading) yet? 
    // Actually, we want to show results as they come in.
    
    // We need to manage `expanded` state carefully.
    // If user types, we expand.
    // If user selects, we collapse.
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                if (it.isNotEmpty()) expanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Customer") },
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        onQueryChange("")
                        expanded = false
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
            singleLine = true
        )

        if (searchResults.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                searchResults.forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${customer.name} (${customer.phone})",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onCustomerSelected(customer)
                            expanded = false
                            keyboardController?.hide()
                        }
                    )
                }
            }
        }
    }
}
