package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val previewResult by viewModel.previewResult.collectAsStateWithLifecycle()
    val isScientific by viewModel.isScientific.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorDisplay(
                expression = expression,
                previewResult = previewResult,
                hasMemory = memoryValue != 0.0,
                memoryValue = memoryValue,
                isScientific = isScientific,
                onCopy = {
                    val target = if (previewResult.isNotBlank()) {
                        previewResult.removePrefix("= ")
                    } else {
                        expression
                    }
                    val ok = viewModel.copyToClipboard("Result", target)
                    if (ok) {
                        Toast.makeText(context, "Copied: $target", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Scientific Toggle Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilterChip(
                    selected = isScientific,
                    onClick = { viewModel.onToggleScientific() },
                    label = {
                        Text(
                            text = if (isScientific) "Scientific Active" else "Scientific Functions",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (isScientific) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_scientific_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad Section
        CalculatorKeypad(
            isScientific = isScientific,
            onNumber = { char -> viewModel.onInput(char) },
            onOperator = { op -> viewModel.onInput(op) },
            onClear = { viewModel.onClear() },
            onBackspace = { viewModel.onBackspace() },
            onEquals = { viewModel.onEquals() },
            onTogglePlusMinus = { viewModel.onTogglePlusMinus() },
            onScientificFunc = { func -> viewModel.onScientificFunction(func) },
            onMemoryAction = { mem ->
                when (mem) {
                    "MC" -> {
                        viewModel.memoryClear()
                        Toast.makeText(context, "Memory Cleared", Toast.LENGTH_SHORT).show()
                    }
                    "MR" -> viewModel.memoryRecall()
                    "M+" -> {
                        viewModel.memoryAdd()
                        Toast.makeText(context, "Added to Memory", Toast.LENGTH_SHORT).show()
                    }
                    "M-" -> {
                        viewModel.memorySubtract()
                        Toast.makeText(context, "Subtracted from Memory", Toast.LENGTH_SHORT).show()
                    }
                    "MS" -> {
                        viewModel.memoryStore()
                        Toast.makeText(context, "Saved in Memory", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
