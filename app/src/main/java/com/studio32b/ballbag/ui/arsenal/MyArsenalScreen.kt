package com.studio32b.ballbag.ui.arsenal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.text.style.TextOverflow
import com.studio32b.ballbag.data.BowlingBall
import androidx.compose.ui.unit.sp

@Composable
fun getDrawableIdByName(imageFile: String?): Int? {
    val context = androidx.compose.ui.platform.LocalContext.current
    val name = imageFile?.substringBeforeLast('.') ?: return null
    return remember(imageFile) {
        context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
    }
}

@Composable
fun MyBallBagScreen(viewModel: MyArsenalViewModel = hiltViewModel(), fadeIn: Boolean = false) {
    val baggedBalls by viewModel.baggedBalls.collectAsState(initial = emptyList())
    var editingBallId: Long? by remember { mutableStateOf<Long?>(null) }
    var showUsageDialog by remember { mutableStateOf(false) }
    var usageGamesToAdd by remember { mutableStateOf(1) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    // State for showing ball details
    var selectedBallForDetails by remember { mutableStateOf<BowlingBall?>(null) }

    val editingBall = baggedBalls.find { it.arsenalId == editingBallId }
    // Maintenance thresholds and warning color for use in the list and elsewhere
    val resurfacingLowerLimit = 60
    val resurfacingUpperLimit = 80
    val oilExtractionLowerLimit = 50
    val oilExtractionUpperLimit = 75
    val mutedYellow = Color(0xFFFFF59D)

    // Ball list UI
    if (baggedBalls.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No balls in your ball bag yet.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            itemsIndexed(baggedBalls) { index: Int, baggedBall: BaggedBall ->
                val rowColor = if (index % 2 == 0) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowColor)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageRes = getDrawableIdByName(baggedBall.ball.imageFile)
                    if (imageRes != null) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = baggedBall.ball.ballName,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(64.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(64.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Left column: brand, ball name, games used, last used
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(baggedBall.ball.brand, style = MaterialTheme.typography.bodySmall)
                            val dateFormat = remember { SimpleDateFormat("MMM ''yy", Locale.getDefault()) }
                            val dateString = dateFormat.format(Date(baggedBall.dateAdded))
                            Text(
                                "Added $dateString",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    baggedBall.ball.ballName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.clickable { selectedBallForDetails = baggedBall.ball }
                                )
                                Row {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Games Used: ${baggedBall.gamesUsed}", style = MaterialTheme.typography.bodySmall)
                                }
                                val lastUsedString = baggedBall.lastUsed?.let {
                                    val lastUsedFormat = remember { SimpleDateFormat("MM/dd/yy", Locale.getDefault()) }
                                    lastUsedFormat.format(Date(it))
                                } ?: "Never"
                                Row {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Last Used: $lastUsedString", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            // Maintenance icons
                            // Show maintenance icon to the left of the edit button:
                            // - Red error if at/above upper limit for resurfacing or oil extraction
                            // - Yellow warning if at/above lower limit for either, but not upper
                            val showRed = baggedBall.gamesSinceResurfaced >= resurfacingUpperLimit || baggedBall.gamesSinceOilExtraction >= oilExtractionUpperLimit
                            val showYellow = !showRed && (baggedBall.gamesSinceResurfaced >= resurfacingLowerLimit || baggedBall.gamesSinceOilExtraction >= oilExtractionLowerLimit)
                            if (showRed) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = "Maintenance Overdue",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else if (showYellow) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Maintenance Due Soon",
                                    tint = mutedYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(onClick = {
                                editingBallId = baggedBall.arsenalId
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
            }
        }
    }
    if (editingBall != null) {
        val mutedYellow = Color(0xFFFFF59D)
        var showBallMaintenanceDialog by remember { mutableStateOf(false) }
        var tempResurfacingLowerLimit by remember { mutableStateOf(editingBall.resurfacingLowerLimit) }
        var tempResurfacingUpperLimit by remember { mutableStateOf(editingBall.resurfacingUpperLimit) }
        var tempOilExtractionLowerLimit by remember { mutableStateOf(editingBall.oilExtractionLowerLimit) }
        var tempOilExtractionUpperLimit by remember { mutableStateOf(editingBall.oilExtractionUpperLimit) }
        var tempResurfacingEnabled by remember { mutableStateOf(true) }
        var tempOilExtractionEnabled by remember { mutableStateOf(true) }
        var showBallDetailsDialog by remember { mutableStateOf(false) }
        var gamesToAdd by remember { mutableStateOf(1) }
        AlertDialog(
            onDismissRequest = { editingBallId = null },
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showBallMaintenanceDialog = true }) {
                            Icon(Icons.Filled.Notes, contentDescription = "Ball Journal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "Ball Journal",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Ball", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Text(
                        "${editingBall.ball.brand} - ${editingBall.ball.ballName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { showBallDetailsDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Add Games to Ball:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (gamesToAdd > 1) gamesToAdd-- }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrement")
                        }
                        Text(gamesToAdd.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                        IconButton(onClick = { gamesToAdd++ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increment")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val resurfacedString = editingBall.lastResurfaced?.let {
                        val fmt = remember { SimpleDateFormat("MM/dd/yy", Locale.getDefault()) }
                        fmt.format(Date(it))
                    } ?: "Never"
                    val oilExtractedString = editingBall.lastOilExtraction?.let {
                        val fmt = remember { SimpleDateFormat("MM/dd/yy", Locale.getDefault()) }
                        fmt.format(Date(it))
                    } ?: "Never"
                    Spacer(modifier = Modifier.height(8.dp))
                    val resurfacingColor = when {
                        editingBall.gamesSinceResurfaced > editingBall.resurfacingUpperLimit -> MaterialTheme.colorScheme.error
                        editingBall.gamesSinceResurfaced >= editingBall.resurfacingLowerLimit && editingBall.gamesSinceResurfaced < editingBall.resurfacingUpperLimit -> mutedYellow
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                    val oilExtractionColor = when {
                        editingBall.gamesSinceOilExtraction > editingBall.oilExtractionUpperLimit -> MaterialTheme.colorScheme.error
                        editingBall.gamesSinceOilExtraction >= editingBall.oilExtractionLowerLimit && editingBall.gamesSinceOilExtraction < editingBall.oilExtractionUpperLimit -> mutedYellow
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                    Text(
                        "Games Since Resurfaced: ${editingBall.gamesSinceResurfaced}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = resurfacingColor
                    )
                    Text(
                        "Last Resurfaced: $resurfacedString",
                        style = MaterialTheme.typography.bodySmall,
                        color = resurfacingColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        viewModel.updateResurfaced(editingBall.arsenalId)
                    }) { Text("Mark as Resurfaced") }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Games Since Oil Extraction: ${editingBall.gamesSinceOilExtraction}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = oilExtractionColor
                    )
                    Text(
                        "Last Oil Extraction: $oilExtractedString",
                        style = MaterialTheme.typography.bodySmall,
                        color = oilExtractionColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        viewModel.updateOilExtraction(editingBall.arsenalId)
                    }) { Text("Mark Oil Extracted") }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        viewModel.recordUsage(
                            editingBall.arsenalId,
                            editingBall.gamesUsed,
                            gamesToAdd,
                            "game"
                        )
                        editingBallId = null
                    }) { Text("Ok") }
                }
            },
            dismissButton = {}
        )
        if (showBallMaintenanceDialog) {
            MaintenanceDialog(
                resurfacingLowerLimit = tempResurfacingLowerLimit,
                resurfacingUpperLimit = tempResurfacingUpperLimit,
                oilExtractionLowerLimit = tempOilExtractionLowerLimit,
                oilExtractionUpperLimit = tempOilExtractionUpperLimit,
                resurfacingEnabled = tempResurfacingEnabled,
                onResurfacingEnabledChange = { tempResurfacingEnabled = it },
                oilExtractionEnabled = tempOilExtractionEnabled,
                onOilExtractionEnabledChange = { tempOilExtractionEnabled = it },
                onResurfacingLowerChange = { tempResurfacingLowerLimit = it },
                onResurfacingUpperChange = { tempResurfacingUpperLimit = it },
                onOilExtractionLowerChange = { tempOilExtractionLowerLimit = it },
                onOilExtractionUpperChange = { tempOilExtractionUpperLimit = it },
                onDismiss = {
                    viewModel.updateBallMaintenanceLimits(
                        editingBall.arsenalId,
                        tempResurfacingLowerLimit,
                        tempResurfacingUpperLimit,
                        tempOilExtractionLowerLimit,
                        tempOilExtractionUpperLimit
                    )
                    showBallMaintenanceDialog = false
                }
            )
        }
        if (showBallDetailsDialog) {
            com.studio32b.ballbag.ui.browse.BowlingBallDetailsDialog(
                ball = editingBall.ball,
                onDismiss = { showBallDetailsDialog = false }
            )
        }
    }
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete ${editingBall?.ball?.brand} - ${editingBall?.ball?.ballName}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteBallFromBag(editingBall!!.arsenalId)
                    editingBallId = null
                    showDeleteConfirmDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
    if (showUsageDialog && editingBall != null) {
        AlertDialog(
            onDismissRequest = { showUsageDialog = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Record Ball Usage", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // No number picker here; just a message or info if needed
                    Text("Confirm adding games to this ball.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = { showUsageDialog = false }, modifier = Modifier.padding(end = 16.dp)) { Text("Cancel") }
                    Button(onClick = {
                        viewModel.incrementGamesAndMaintenanceCounters(editingBall.arsenalId, usageGamesToAdd)
                        showUsageDialog = false
                    }) { Text("Save") }
                }
            },
            dismissButton = {}
        )
    }
    // Show ball details dialog if a ball is selected
    if (selectedBallForDetails != null) {
        BallDetailsDialog(
            ball = selectedBallForDetails!!,
            onDismiss = { selectedBallForDetails = null }
        )
    }
}

// Helper composable for press-and-hold increment/decrement
@Composable
fun HoldableButton(onClick: () -> Unit, onHold: () -> Unit, enabled: Boolean = true, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        job = scope.launch {
                            delay(400) // Initial delay before repeat
                            while (isPressed) {
                                onHold()
                                delay(80)
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        job?.cancel()
                    }
                )
            }
    ) { content() }
}

@Composable
fun HoldableIconButton(
    onClick: () -> Unit,
    onHold: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    Surface(
        modifier = Modifier
            .size(40.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            onClick()
                            job = scope.launch {
                                delay(400)
                                while (isPressed) {
                                    onHold()
                                    delay(80)
                                }
                            }
                            tryAwaitRelease()
                            isPressed = false
                            job?.cancel()
                        }
                    )
                }
            },
        color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (isPressed) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { if (value > min) onValueChange(value - 1) },
            enabled = enabled && value > min,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrement")
        }
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = { if (value < max) onValueChange(value + 1) },
            enabled = enabled && value < max,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increment")
        }
    }
}

@Composable
fun MaintenanceDialog(
    resurfacingLowerLimit: Int,
    resurfacingUpperLimit: Int,
    oilExtractionLowerLimit: Int,
    oilExtractionUpperLimit: Int,
    resurfacingEnabled: Boolean,
    onResurfacingEnabledChange: (Boolean) -> Unit,
    oilExtractionEnabled: Boolean,
    onOilExtractionEnabledChange: (Boolean) -> Unit,
    onResurfacingLowerChange: (Int) -> Unit,
    onResurfacingUpperChange: (Int) -> Unit,
    onOilExtractionLowerChange: (Int) -> Unit,
    onOilExtractionUpperChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ball Maintenance", style = MaterialTheme.typography.titleMedium)
                    Text("Reminder Settings", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                // Resurfacing toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (resurfacingEnabled) "Resurfacing Notifs Enabled" else "Resurfacing Notifs Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (resurfacingEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.Start),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = resurfacingEnabled,
                        onCheckedChange = onResurfacingEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (resurfacingEnabled) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Resurface Notifs Game Settings ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "Warning",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                "Threshold",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        NumberPicker(
                            value = resurfacingLowerLimit,
                            onValueChange = onResurfacingLowerChange,
                            modifier = Modifier.weight(1f),
                            min = 0,
                            max = resurfacingUpperLimit,
                            enabled = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        NumberPicker(
                            value = resurfacingUpperLimit,
                            onValueChange = onResurfacingUpperChange,
                            modifier = Modifier.weight(1f),
                            min = resurfacingLowerLimit,
                            max = 9999,
                            enabled = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Oil Extraction toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (oilExtractionEnabled) "Oil Extraction Notifs Enabled" else "Oil Extraction Notifs Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (oilExtractionEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(Alignment.Start),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = oilExtractionEnabled,
                        onCheckedChange = onOilExtractionEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (oilExtractionEnabled) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Oil Extraction Notifs\nGame Settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("Warning", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("Threshold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        NumberPicker(
                            value = oilExtractionLowerLimit,
                            onValueChange = onOilExtractionLowerChange,
                            modifier = Modifier.weight(1f),
                            min = 0,
                            max = oilExtractionUpperLimit,
                            enabled = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        NumberPicker(
                            value = oilExtractionUpperLimit,
                            onValueChange = onOilExtractionUpperChange,
                            modifier = Modifier.weight(1f),
                            min = oilExtractionLowerLimit,
                            max = 9999,
                            enabled = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {}
    )
}

@Composable
fun BallDetailsDialog(ball: BowlingBall, onDismiss: () -> Unit) {
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
                val releaseTypeDisplay = when (ball.releaseType) {
                    "OS" -> "OVS"
                    "US" -> "USA"
                    else -> ball.releaseType
                }
                Text("Released: ${ball.releaseDate} - $releaseTypeDisplay", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(12.dp))
                val indentStyle = MaterialTheme.typography.bodyMedium.copy(
                    textIndent = TextIndent(firstLine = 0.sp, restLine = 16.sp)
                )
                Text("Coverstock: ${ball.coverstock}", style = indentStyle)
                Text("Core: ${ball.core}", style = indentStyle)
                Text("OOB Finish: ${ball.factoryFinish}", style = indentStyle)
                Spacer(modifier = Modifier.height(8.dp))
                Text("RG: %.3f".format(ball.rg), style = indentStyle)
                Text("Diff: %.3f".format(ball.diff), style = indentStyle)
                val mbDiffDisplay = ball.mbDiff?.let { "%.3f".format(it) } ?: "n/a"
                Text("MB Diff: $mbDiffDisplay", style = indentStyle)
            }
        }
    )
}
