package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MemoryPressureState
import com.example.model.RamBudgetConfig
import com.example.model.RamStandard
import java.text.DecimalFormat

@Composable
fun RamHubScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deviceStats by viewModel.deviceRamStats.collectAsStateWithLifecycle()
    val budgetConfig by viewModel.budgetConfig.collectAsStateWithLifecycle()
    val budgetResult by viewModel.budgetResult.collectAsStateWithLifecycle()
    val convertInput by viewModel.convertInput.collectAsStateWithLifecycle()
    val convertUnit by viewModel.convertUnit.collectAsStateWithLifecycle()
    val isBinary by viewModel.isBinaryUnit.collectAsStateWithLifecycle()
    val convertedResults by viewModel.convertedResults.collectAsStateWithLifecycle()
    val selectedStandard by viewModel.selectedStandard.collectAsStateWithLifecycle()
    val benchmarkResult by viewModel.benchmarkResult.collectAsStateWithLifecycle()
    val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("8GB Planner", "Live Device RAM", "Unit Converter", "RAM Specs & Speed")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sub-tabs scrollable or centered
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("ram_tab_$index")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ram_hub_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            when (selectedSubTab) {
                0 -> {
                    // --- 8GB Mobile RAM Planner & Multitasking Capacity ---
                    item {
                        RamBudgetHeaderCard(result = budgetResult)
                    }

                    item {
                        RamArchitectureCard()
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Multitasking App Sliders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = { viewModel.resetBudgetToDefault() },
                                        modifier = Modifier.testTag("reset_budget_button")
                                    ) {
                                        Text("Reset")
                                    }
                                }

                                BudgetCounterRow(
                                    title = "Heavy 3D Games (Genshin, CoD, PUBG)",
                                    subtitle = "~1,650 MB each",
                                    value = budgetConfig.games,
                                    max = 4,
                                    onValueChange = { viewModel.updateBudgetConfig(budgetConfig.copy(games = it)) }
                                )

                                BudgetCounterRow(
                                    title = "Social & Video Apps (IG, TikTok, YT)",
                                    subtitle = "~380 MB each",
                                    value = budgetConfig.socialApps,
                                    max = 12,
                                    onValueChange = { viewModel.updateBudgetConfig(budgetConfig.copy(socialApps = it)) }
                                )

                                BudgetCounterRow(
                                    title = "Active Browser Tabs (Chrome/Firefox)",
                                    subtitle = "~110 MB each",
                                    value = budgetConfig.browserTabs,
                                    max = 25,
                                    onValueChange = { viewModel.updateBudgetConfig(budgetConfig.copy(browserTabs = it)) }
                                )

                                BudgetCounterRow(
                                    title = "Background Services (Music, Chat, VPN)",
                                    subtitle = "~160 MB each",
                                    value = budgetConfig.bgServices,
                                    max = 10,
                                    onValueChange = { viewModel.updateBudgetConfig(budgetConfig.copy(bgServices = it)) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // --- Live Device RAM Monitor ---
                    item {
                        DeviceRamCard(
                            stats = deviceStats,
                            onRefresh = {
                                viewModel.refreshRamStats()
                                Toast.makeText(context, "RAM Stats Updated", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        SystemMemoryInfoCard()
                    }
                }

                2 -> {
                    // --- Memory & Data Unit Converter ---
                    item {
                        MemoryConverterCard(
                            input = convertInput,
                            unit = convertUnit,
                            isBinary = isBinary,
                            results = convertedResults,
                            onInputChange = { viewModel.updateConversion(it, convertUnit, isBinary) },
                            onUnitChange = { viewModel.updateConversion(convertInput, it, isBinary) },
                            onBinaryToggle = { viewModel.updateConversion(convertInput, convertUnit, it) },
                            onPresetSelect = { viewModel.updateConversion(it, "GB", isBinary) },
                            onCopy = { val ok = viewModel.copyToClipboard("Conversion", it); if (ok) Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }
                        )
                    }

                    item {
                        RealWorld8GbEquivalentsCard()
                    }
                }

                3 -> {
                    // --- RAM Specs & Benchmark ---
                    item {
                        RamStandardsCard(
                            selected = selectedStandard,
                            onSelect = { viewModel.selectRamStandard(it) }
                        )
                    }

                    item {
                        RamBenchmarkCard(
                            isBenchmarking = isBenchmarking,
                            result = benchmarkResult,
                            onRun = { viewModel.runRamBenchmark() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RamBudgetHeaderCard(result: com.example.model.RamBudgetResult) {
    val progress = (result.totalRequiredMb.toFloat() / result.totalDeviceRamMb.toFloat()).coerceIn(0f, 1.2f)
    val stateColor = when (result.memoryPressureState) {
        MemoryPressureState.COMFORTABLE -> Color(0xFF10B981)
        MemoryPressureState.MODERATE -> Color(0xFF0284C7)
        MemoryPressureState.HIGH -> Color(0xFFF59E0B)
        MemoryPressureState.CRITICAL -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "8 GB Mobile RAM Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Capacity: 8,192 MB (8.00 GB)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = stateColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = result.memoryPressureState.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }
            }

            // Visual Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { (result.totalRequiredMb.toFloat() / result.totalDeviceRamMb.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp)),
                    color = stateColor,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Simulated: ${result.totalRequiredMb} MB (${String.format("%.2f", result.totalRequiredMb / 1024.0)} GB)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (result.remainingMb >= 0) "${result.remainingMb} MB Free" else "${-result.remainingMb} MB Over Capacity!",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (result.remainingMb >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFEF4444)
                    )
                }
            }

            Text(
                text = result.memoryPressureState.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RamArchitectureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "How 8 GB RAM Works on Android",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Android smartphones divide 8 GB into 3 essential partitions:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RamPartitionRow(
                color = Color(0xFF64748B),
                title = "Hardware & Kernel Reserve",
                size = "~1,850 MB (22.5%)",
                desc = "Dedicated to GPU framebuffer, modem, camera ISP, ZRAM swap space"
            )

            RamPartitionRow(
                color = Color(0xFF0284C7),
                title = "Android OS & System UI",
                size = "~1,350 MB (16.5%)",
                desc = "System server, Google Play services, launcher, core background listeners"
            )

            RamPartitionRow(
                color = Color(0xFF10B981),
                title = "User Apps Multitasking Pool",
                size = "~4,992 MB (61.0%)",
                desc = "Dynamic RAM pool available for foreground 3D games, social apps, and browser tabs"
            )
        }
    }
}

@Composable
fun RamPartitionRow(color: Color, title: String, size: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = size, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BudgetCounterRow(
    title: String,
    subtitle: String,
    value: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                enabled = value > 0,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            FilledTonalIconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                enabled = value < max,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun DeviceRamCard(
    stats: com.example.model.DeviceRamStats?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "RAM Chip",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Live Device RAM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardware Telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_ram_stats_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh RAM")
                }
            }

            if (stats != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RAM Usage",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.1f", stats.usedPercentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { stats.usedPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricColumn("Total RAM", stats.totalGbFormatted)
                    StatMetricColumn("Used RAM", stats.usedGbFormatted)
                    StatMetricColumn("Available", stats.availGbFormatted)
                }

                if (stats.isLowMemory) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text(
                                text = "Device is in Low Memory condition. LMK is active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun StatMetricColumn(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SystemMemoryInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Understanding Mobile RAM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• In Linux/Android, 'Free RAM is wasted RAM'. Android keeps recently opened apps cached in RAM so they reopen instantaneously.\n• When new memory is needed, Android triggers LMK (Low Memory Killer) to silently drop the least recently used background processes.\n• 8 GB is currently considered the sweet spot for modern mobile Android gaming and seamless multi-app switching.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun MemoryConverterCard(
    input: String,
    unit: String,
    isBinary: Boolean,
    results: Map<String, String>,
    onInputChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onBinaryToggle: (Boolean) -> Unit,
    onPresetSelect: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Data & Memory Unit Converter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Preset shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("4", "6", "8", "12", "16").forEach { preset ->
                    SuggestionChip(
                        onClick = { onPresetSelect(preset) },
                        label = { Text("${preset} GB") },
                        modifier = Modifier.testTag("preset_$preset")
                    )
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    label = { Text("Memory Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("convert_input_field"),
                    singleLine = true
                )

                var expanded by remember { mutableStateOf(false) }
                Box {
                    FilledTonalButton(
                        onClick = { expanded = true },
                        modifier = Modifier.testTag("unit_selector_button")
                    ) {
                        Text(unit)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Bytes", "KB", "MB", "GB", "TB").forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    onUnitChange(u)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Binary (1024) vs Decimal (1000) Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBinary) "Binary Mode (1024 / GiB, MiB)" else "Decimal Mode (1000 / GB, MB)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isBinary) "Used by RAM chips & OS" else "Used by storage manufacturers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isBinary,
                    onCheckedChange = onBinaryToggle,
                    modifier = Modifier.testTag("binary_mode_switch")
                )
            }

            HorizontalDivider()

            // Converted Results
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.forEach { (u, valStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onCopy("$valStr $u") }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = u, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = valStr, style = MaterialTheme.typography.bodyMedium)
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealWorld8GbEquivalentsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "What Fits Inside 8 GB (8,192 MB)?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            EquivalentItem(Icons.Default.PhotoCamera, "1,638 High-Res Photos (~5 MB each)")
            EquivalentItem(Icons.Default.MusicNote, "2,048 MP3 Audio Tracks (~4 MB each)")
            EquivalentItem(Icons.Default.Movie, "16.4 Hours of 1080p Video Buffer (~500 MB/hr)")
            EquivalentItem(Icons.Default.VideogameAsset, "4-5 Concurrent AAA Mobile Games in RAM")
        }
    }
}

@Composable
fun EquivalentItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RamStandardsCard(
    selected: RamStandard,
    onSelect: (RamStandard) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Mobile RAM Standards & Speed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            RamStandard.values().forEach { standard ->
                val isSel = standard == selected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(standard) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = standard.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${standard.bandwidthGbps} GB/s",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Data Rate: ${standard.speedMt} MT/s • Voltage: ${standard.typicalVoltage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = standard.commonIn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RamBenchmarkCard(
    isBenchmarking: Boolean,
    result: com.example.model.MemoryBenchmarkResult?,
    onRun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Local RAM Speed Benchmark",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tests physical page allocation & write rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onRun,
                    enabled = !isBenchmarking,
                    modifier = Modifier.testTag("run_benchmark_button")
                ) {
                    if (isBenchmarking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Test Speed")
                    }
                }
            }

            if (result != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Allocation Throughput:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${String.format("%,.0f", result.speedMbPerSec)} MB/s",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "Allocated & verified ${result.allocatedMb} MB physical buffers in ${result.durationMs} ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
