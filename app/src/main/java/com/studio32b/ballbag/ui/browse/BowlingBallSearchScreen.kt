package com.studio32b.ballbag.ui.browse

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studio32b.ballbag.data.BowlingBall
import com.studio32b.ballbag.data.OptionCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowlingBallSearchScreen(
    viewModel: BowlingBallSearchViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState: BowlingBallSearchUiState by viewModel.uiState.collectAsState()
    val arsenalCounts by viewModel.arsenalCounts.collectAsState()

    // Mapping for release type display
    val releaseTypeDisplayMap = mapOf(
        "US" to "USA",
        "OS" to "Overseas"
    )

    var selectedBall by remember { mutableStateOf<BowlingBall?>(null) }
    var pendingAddBall by remember { mutableStateOf<BowlingBall?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var filterDropdownExpanded by remember { mutableStateOf(false) }
    var hasBeenUsed by remember { mutableStateOf<String?>(null) }
    var showAlreadyOwnedDialog by remember { mutableStateOf(false) }
    var pendingAddConfirmed by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        // Name search and Filters button
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                label = { Text("Search by name") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val isDark = isSystemInDarkTheme()
            Box {
                OutlinedButton(
                    onClick = { filterDropdownExpanded = true },
                    shape = MaterialTheme.shapes.medium, // Match TextField shape
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Filters")
                }
                DropdownMenu(
                    expanded = filterDropdownExpanded,
                    onDismissRequest = { filterDropdownExpanded = false },
                    modifier = Modifier.width(320.dp)
                ) {
                    FilterDropdownContent(
                        uiState = uiState,
                        onToggleBrand = { viewModel.toggleBrand(it) },
                        onToggleCoverstock = { viewModel.toggleCoverstock(it) },
                        onToggleCore = { viewModel.toggleCore(it) },
                        onToggleReleaseType = { viewModel.toggleReleaseType(it) },
                        onToggleProdStatus = { viewModel.toggleProdStatus(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onApply = { filterDropdownExpanded = false },
                        releaseTypeDisplayMap = releaseTypeDisplayMap,
                        closeDropdown = { filterDropdownExpanded = false }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Balls found and Clear Filters button
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${uiState.results.size} balls found", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.clearFilters() }) {
                Text("Clear Filters", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Divider between balls found and header
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        // Header row for search results
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ball / Brand",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Text(
                text = "Released",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
            // Only the Bag label, centered
            Box(
                modifier = Modifier.width(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bag",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(uiState.results) { index, ball ->
                val rowColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant
                BowlingBallRow(
                    ball = ball,
                    inArsenal = uiState.arsenalIds.contains(ball.id),
                    onAddToArsenal = {
                        if ((arsenalCounts[ball.id] ?: 0) > 0) {
                            pendingAddBall = ball
                            showDuplicateDialog = true
                        } else {
                            pendingAddBall = ball
                            showAddDialog = true
                        }
                    },
                    onClick = { selectedBall = ball },
                    backgroundColor = rowColor,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    if (selectedBall != null) {
        BowlingBallDetailsDialog(ball = selectedBall!!, onDismiss = { selectedBall = null })
    }
    if (showAddDialog && pendingAddBall != null) {
        // Ball History state (now in main dialog)
        var gamesUsed by remember { mutableStateOf(0) }
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (currentYear downTo (currentYear - 30)).toList()
        var selectedMonthIndex by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) }
        var selectedYear by remember { mutableStateOf(currentYear) }
        val dateAdded = remember(selectedMonthIndex, selectedYear) {
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, selectedYear)
                set(java.util.Calendar.MONTH, selectedMonthIndex)
                set(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                pendingAddBall = null
                hasBeenUsed = null
                pendingAddConfirmed = false
            },
            title = {
                // Centered, combined title: Add Brand Name Ball Name to Bag
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        "Add ${pendingAddBall!!.brand} ${pendingAddBall!!.ballName} to Bag?",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Has this ball been used before?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        RadioButton(
                            selected = hasBeenUsed == "Yes",
                            onClick = { hasBeenUsed = "Yes" }
                        )
                        Text("Yes", modifier = Modifier
                            .clickable { hasBeenUsed = "Yes" }
                            .padding(end = 24.dp, start = 4.dp))
                        RadioButton(
                            selected = hasBeenUsed == "No",
                            onClick = { hasBeenUsed = "No" }
                        )
                        Text("No", modifier = Modifier
                            .clickable { hasBeenUsed = "No" }
                            .padding(start = 4.dp))
                    }
                    if (hasBeenUsed == "Yes") {
                        // Ball history fields
                        OutlinedTextField(
                            value = gamesUsed.toString(),
                            onValueChange = { value ->
                                gamesUsed = value.toIntOrNull() ?: 0
                            },
                            label = { Text("Approx Games Used") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Approx Date Acquired:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            var monthExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { monthExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(months[selectedMonthIndex])
                                }
                                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                    months.forEachIndexed { idx, month ->
                                        DropdownMenuItem(
                                            text = { Text(month) },
                                            onClick = {
                                                selectedMonthIndex = idx
                                                monthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            var yearExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(onClick = { yearExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(selectedYear.toString())
                                }
                                DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                                    years.forEach { year ->
                                        DropdownMenuItem(
                                            text = { Text(year.toString()) },
                                            onClick = {
                                                selectedYear = year
                                                yearExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ballId = pendingAddBall!!.id
                        if (hasBeenUsed == "Yes") {
                            viewModel.addToArsenal(ballId, dateAdded, gamesUsed)
                        } else if (hasBeenUsed == "No") {
                            val cal = java.util.Calendar.getInstance()
                            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            viewModel.addToArsenal(ballId, cal.timeInMillis, 0)
                        }
                        showAddDialog = false
                        pendingAddBall = null
                        hasBeenUsed = null
                        pendingAddConfirmed = false
                    },
                    enabled = hasBeenUsed != null && (hasBeenUsed == "No" || (hasBeenUsed == "Yes" && gamesUsed >= 0))
                ) {
                    Text(if (hasBeenUsed == "Yes") "Add to Bag" else "Add to Bag")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    pendingAddBall = null
                    hasBeenUsed = null
                    pendingAddConfirmed = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showDuplicateDialog && pendingAddBall != null) {
        AlertDialog(
            onDismissRequest = {
                showDuplicateDialog = false
                pendingAddBall = null
            },
            title = { Text("Add Duplicate Ball?") },
            text = { Text("You already have this ball in your bag. Add another with the same name?") },
            confirmButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    showAddDialog = true
                    hasBeenUsed = null
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDuplicateDialog = false
                    pendingAddBall = null
                }) { Text("No") }
            }
        )
    }
    if (showAlreadyOwnedDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyOwnedDialog = false },
            title = { Text("Already Owned") },
            text = { Text("You already own this ball. Add another?") },
            confirmButton = {
                Button(onClick = {
                    showAlreadyOwnedDialog = false
                    pendingAddConfirmed = true
                }) { Text("Add Another") }
            },
            dismissButton = {
                TextButton(onClick = { showAlreadyOwnedDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BowlingBallRow(
    ball: BowlingBall,
    inArsenal: Boolean,
    onAddToArsenal: (Int) -> Unit,
    onClick: () -> Unit,
    backgroundColor: Color = Color.Unspecified,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageRes = getDrawableIdByName(ball.imageFile)
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = ball.ballName,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(64.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ball.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
                // Format release date as Mon/yy (e.g., Aug/25)
                val relDate = try {
                    val parts = ball.releaseDate.split("-")
                    if (parts.size >= 2) {
                        val year = parts[0].takeLast(2)
                        val monthNum = parts[1].toIntOrNull() ?: 1
                        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val month = months.getOrNull(monthNum - 1) ?: ball.releaseDate
                        "$month/$year"
                    } else {
                        ball.releaseDate
                    }
                } catch (_: Exception) {
                    ball.releaseDate
                }
                Text(
                    relDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                ball.ballName,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = false
            )
        }
        // Bag column (right side)
        Column(
            modifier = Modifier.width(64.dp), // match header width
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onAddToArsenal(ball.id) },
                enabled = true
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Bag",
                    tint = textColor
                )
            }
            if (inArsenal) {
                Text(
                    text = "In Bag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BowlingBallDetailsDialog(ball: BowlingBall, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(ball.ballName, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(ball.brand, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))

            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val imageRes = getDrawableIdByName(ball.imageFile)
                if (imageRes != null) {
                    Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = ball.ballName,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        if (ball.discontinued.equals("TRUE", ignoreCase = true)) {
                            Text(
                                "DISCONTINUED",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Released: ${ball.releaseDate} - ${when (ball.releaseType) { "OS" -> "OVS"; "US" -> "USA"; else -> ball.releaseType }}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(12.dp))
                val indentStyle = MaterialTheme.typography.bodyMedium.copy(
                    textIndent = TextIndent(firstLine = 0.sp, restLine = 16.sp)
                )
                Text("Coverstock: ${ball.coverstock}", style = indentStyle)
                Text("Core: ${ball.core}", style = indentStyle)
                Text("OOB Finish: ${ball.factoryFinish}", style = indentStyle)
                Spacer(modifier = Modifier.height(8.dp))
                Text("RG: ${ball.rg}", style = indentStyle)
                Text("Diff: ${ball.diff}", style = indentStyle)
                val mbDiffStr = ball.mbDiff?.toString() ?: ""
                Text("MB Diff: " + if (mbDiffStr == "") "n/a" else mbDiffStr, style = indentStyle)
            }
        }
    )
}

@Composable
fun getDrawableIdByName(imageFile: String?): Int? {
    val context = androidx.compose.ui.platform.LocalContext.current
    val name = imageFile?.substringBeforeLast('.') ?: return null
    return remember(imageFile) {
        context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
    }
}

@Composable
fun FilterDropdownContent(
    uiState: BowlingBallSearchUiState,
    onToggleBrand: (String) -> Unit,
    onToggleCoverstock: (String) -> Unit,
    onToggleCore: (String) -> Unit,
    onToggleReleaseType: (String) -> Unit,
    onToggleProdStatus: (String) -> Unit = {},
    onClearFilters: () -> Unit,
    onApply: () -> Unit,
    releaseTypeDisplayMap: Map<String, String>,
    closeDropdown: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(min = 280.dp, max = 340.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilterDropdown(
            label = "Brand",
            options = uiState.brands.map { OptionCount(it.brand, it.count) },
            selected = uiState.selectedBrands,
            onToggle = onToggleBrand
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterDropdown(
            label = "Coverstock",
            options = uiState.coverstocks,
            selected = uiState.selectedCoverstocks,
            onToggle = onToggleCoverstock
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterDropdown(
            label = "Core",
            options = uiState.cores,
            selected = uiState.selectedCores,
            onToggle = onToggleCore
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterDropdown(
            label = "Release Location",
            options = uiState.releaseTypes.map {
                OptionCount(releaseTypeDisplayMap[it.value] ?: it.value, it.count)
            },
            selected = uiState.releaseTypes.filter { uiState.selectedReleaseTypes.contains(it.value) }
                .map { releaseTypeDisplayMap[it.value] ?: it.value }.toSet(),
            onToggle = { selected ->
                val value = uiState.releaseTypes.find { (releaseTypeDisplayMap[it.value] ?: it.value) == selected }?.value
                if (value != null) onToggleReleaseType(value)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterDropdown(
            label = "Prod Status",
            options = uiState.prodStatusCounts,
            selected = uiState.selectedProdStatus,
            onToggle = onToggleProdStatus
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                onClearFilters()
                closeDropdown()
            }) {
                Text("Clear Filters")
            }
            Button(onClick = {
                onApply()
                closeDropdown()
            }) {
                Text("Apply")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<OptionCount>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (selected.isEmpty()) label else selected.joinToString(", ")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    // Clear all selections
                    selected.forEach { onToggle(it) }
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selected.contains(option.value),
                                onCheckedChange = null // handled by onClick
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${option.value} (${option.count})")
                        }
                    },
                    onClick = {
                        onToggle(option.value)
                    }
                )
            }
        }
    }
}
