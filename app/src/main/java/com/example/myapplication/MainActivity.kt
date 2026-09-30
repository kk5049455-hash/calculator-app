package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

import androidx.compose.ui.tooling.preview.Preview

/**
 * TASK 01 — Calculator App
 * Supports: add, subtract, multiply, divide, decimal point,
 * percentage, sign toggle, clear, and backspace.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    MaterialTheme {
        CalculatorScreen()
    }
}


private enum class Operation { ADD, SUBTRACT, MULTIPLY, DIVIDE }

private class CalculatorState {
    var displayText by mutableStateOf("0")
    private var firstOperand: Double? = null
    private var pendingOperation: Operation? = null
    private var isNewEntry = true

    fun onDigit(digit: String) {
        if (isNewEntry || displayText == "0") {
            displayText = digit
            isNewEntry = false
        } else {
            displayText += digit
        }
    }

    fun onDecimal() {
        if (isNewEntry) {
            displayText = "0."
            isNewEntry = false
        } else if (!displayText.contains(".")) {
            displayText += "."
        }
    }

    fun onOperation(op: Operation) {
        val current = displayText.toDoubleOrNull() ?: return
        if (firstOperand != null && !isNewEntry) {
            // Chain operations: compute the running result first
            compute()
            firstOperand = displayText.toDoubleOrNull()
        } else {
            firstOperand = current
        }
        pendingOperation = op
        isNewEntry = true
    }

    fun onEquals() {
        compute()
        pendingOperation = null
        firstOperand = null
        isNewEntry = true
    }

    fun onPercent() {
        val current = displayText.toDoubleOrNull() ?: return
        displayText = formatResult(current / 100.0)
        isNewEntry = true
    }

    fun onToggleSign() {
        val current = displayText.toDoubleOrNull() ?: return
        displayText = formatResult(current * -1.0)
    }

    fun onBackspace() {
        if (isNewEntry) return
        displayText = if (displayText.length <= 1) "0" else displayText.dropLast(1)
        if (displayText == "-") displayText = "0"
    }

    fun onClear() {
        displayText = "0"
        firstOperand = null
        pendingOperation = null
        isNewEntry = true
    }

    private fun compute() {
        val second = displayText.toDoubleOrNull() ?: return
        val first = firstOperand ?: return
        val op = pendingOperation ?: return

        val result = when (op) {
            Operation.ADD -> first + second
            Operation.SUBTRACT -> first - second
            Operation.MULTIPLY -> first * second
            Operation.DIVIDE -> if (second == 0.0) Double.NaN else first / second
        }
        displayText = formatResult(result)
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"

        val absValue = abs(value)
        if (absValue != 0.0 && absValue !in 1e-7..<1e12){
            return String.format(Locale.US, "%.6e", value)
        }

        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}

// ---------- UI ----------

@Composable
private fun CalculatorScreen() {
    val state = remember { CalculatorState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Display
        Text(
            text = state.displayText,
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        val buttonRows = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        buttonRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    CalcButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (label) {
                                "C" -> state.onClear()
                                "±" -> state.onToggleSign()
                                "%" -> state.onPercent()
                                "⌫" -> state.onBackspace()
                                "÷" -> state.onOperation(Operation.DIVIDE)
                                "×" -> state.onOperation(Operation.MULTIPLY)
                                "−" -> state.onOperation(Operation.SUBTRACT)
                                "+" -> state.onOperation(Operation.ADD)
                                "=" -> state.onEquals()
                                "." -> state.onDecimal()
                                else -> state.onDigit(label)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = label in listOf("÷", "×", "−", "+", "=")
    val isFunction = label in listOf("C", "±", "%", "⌫")

    val backgroundColor = when {
        isOperator -> Color(0xFFFF9500)
        isFunction -> Color(0xFFA5A5A5)
        else -> Color(0xFF333333)
    }
    val textColor = if (isFunction) Color.Black else Color.White

    Button(
        onClick = onClick,
        modifier = modifier
            .height(72.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 26.sp)
    }
}