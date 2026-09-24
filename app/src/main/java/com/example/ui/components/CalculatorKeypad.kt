package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorKeypad(
    isScientific: Boolean,
    onNumber: (String) -> Unit,
    onOperator: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onTogglePlusMinus: () -> Unit,
    onScientificFunc: (String) -> Unit,
    onMemoryAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    fun performHaptic() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Memory Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("MC", "MR", "M+", "M-", "MS").forEach { mem ->
                SmallMemoryButton(
                    text = mem,
                    onClick = {
                        performHaptic()
                        onMemoryAction(mem)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Scientific Keys Row (collapsible)
        AnimatedVisibility(
            visible = isScientific,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("sin", { performHaptic(); onScientificFunc("sin") }, Modifier.weight(1f))
                    ScientificKey("cos", { performHaptic(); onScientificFunc("cos") }, Modifier.weight(1f))
                    ScientificKey("tan", { performHaptic(); onScientificFunc("tan") }, Modifier.weight(1f))
                    ScientificKey("log", { performHaptic(); onScientificFunc("log") }, Modifier.weight(1f))
                    ScientificKey("ln", { performHaptic(); onScientificFunc("ln") }, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("x²", { performHaptic(); onScientificFunc("x²") }, Modifier.weight(1f))
                    ScientificKey("√x", { performHaptic(); onScientificFunc("√x") }, Modifier.weight(1f))
                    ScientificKey("^", { performHaptic(); onOperator("^") }, Modifier.weight(1f))
                    ScientificKey("π", { performHaptic(); onScientificFunc("π") }, Modifier.weight(1f))
                    ScientificKey("e", { performHaptic(); onScientificFunc("e") }, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificKey("(", { performHaptic(); onOperator("(") }, Modifier.weight(1f))
                    ScientificKey(")", { performHaptic(); onOperator(")") }, Modifier.weight(1f))
                    ScientificKey("1/x", { performHaptic(); onScientificFunc("1/x") }, Modifier.weight(1f))
                    ScientificKey("%", { performHaptic(); onOperator("%") }, Modifier.weight(1f))
                    ScientificKey("±", { performHaptic(); onTogglePlusMinus() }, Modifier.weight(1f))
                }
            }
        }

        // Standard 4x5 Keypad
        // Row 1: C, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton(
                text = "AC",
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { performHaptic(); onClear() },
                modifier = Modifier.weight(1f),
                testTag = "btn_clear"
            )
            KeyIconButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { performHaptic(); onBackspace() },
                modifier = Modifier.weight(1f),
                testTag = "btn_backspace"
            )
            KeyButton(
                text = "%",
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { performHaptic(); onOperator("%") },
                modifier = Modifier.weight(1f),
                testTag = "btn_percent"
            )
            KeyButton(
                text = "÷",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { performHaptic(); onOperator("÷") },
                modifier = Modifier.weight(1f),
                testTag = "btn_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("7", { performHaptic(); onNumber("7") }, Modifier.weight(1f), "btn_7")
            NumberKey("8", { performHaptic(); onNumber("8") }, Modifier.weight(1f), "btn_8")
            NumberKey("9", { performHaptic(); onNumber("9") }, Modifier.weight(1f), "btn_9")
            KeyButton(
                text = "×",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { performHaptic(); onOperator("×") },
                modifier = Modifier.weight(1f),
                testTag = "btn_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("4", { performHaptic(); onNumber("4") }, Modifier.weight(1f), "btn_4")
            NumberKey("5", { performHaptic(); onNumber("5") }, Modifier.weight(1f), "btn_5")
            NumberKey("6", { performHaptic(); onNumber("6") }, Modifier.weight(1f), "btn_6")
            KeyButton(
                text = "−",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { performHaptic(); onOperator("−") },
                modifier = Modifier.weight(1f),
                testTag = "btn_minus"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberKey("1", { performHaptic(); onNumber("1") }, Modifier.weight(1f), "btn_1")
            NumberKey("2", { performHaptic(); onNumber("2") }, Modifier.weight(1f), "btn_2")
            NumberKey("3", { performHaptic(); onNumber("3") }, Modifier.weight(1f), "btn_3")
            KeyButton(
                text = "+",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { performHaptic(); onOperator("+") },
                modifier = Modifier.weight(1f),
                testTag = "btn_plus"
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton(
                text = "±",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { performHaptic(); onTogglePlusMinus() },
                modifier = Modifier.weight(1f),
                testTag = "btn_plus_minus"
            )
            NumberKey("0", { performHaptic(); onNumber("0") }, Modifier.weight(1f), "btn_0")
            KeyButton(
                text = ".",
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { performHaptic(); onNumber(".") },
                modifier = Modifier.weight(1f),
                testTag = "btn_dot"
            )
            KeyButton(
                text = "=",
                containerColor = CalcEqualsBg,
                contentColor = CalcEqualsText,
                onClick = { performHaptic(); onEquals() },
                modifier = Modifier.weight(1f),
                testTag = "btn_equals"
            )
        }
    }
}

@Composable
fun NumberKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    KeyButton(
        text = text,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onClick,
        modifier = modifier,
        testTag = testTag
    )
}

@Composable
fun ScientificKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .testTag("sci_btn_$text"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SmallMemoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(36.dp)
            .testTag("mem_btn_$text"),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

@Composable
fun KeyIconButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
