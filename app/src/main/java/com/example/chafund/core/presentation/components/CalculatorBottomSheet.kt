package com.example.chafund.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chafund.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var display by remember { mutableStateOf("0") }
    var leftOperand by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var awaitingOperand by remember { mutableStateOf(false) }

    fun formatNum(v: Double): String {
        if (v.isInfinite() || v.isNaN()) return "0"
        return if (v == kotlin.math.floor(v)) v.toLong().toString()
        else "%.4f".format(v).trimEnd('0').trimEnd('.')
    }

    fun appendDigit(d: String) {
        if (awaitingOperand) {
            display = d
            awaitingOperand = false
        } else {
            if (display.length >= 12) return
            display = if (display == "0") d else display + d
        }
    }

    fun appendDecimal() {
        if (awaitingOperand) {
            display = "0."
            awaitingOperand = false
        } else if (!display.contains('.')) {
            display += "."
        }
    }

    fun compute(left: Double, op: String, right: Double): Double = when (op) {
        "+" -> left + right
        "-" -> left - right
        "×" -> left * right
        "÷" -> if (right != 0.0) left / right else 0.0
        else -> right
    }

    fun pressOperator(op: String) {
        val current = display.toDoubleOrNull() ?: 0.0
        if (leftOperand != null && pendingOp != null && !awaitingOperand) {
            val result = compute(leftOperand!!, pendingOp!!, current)
            display = formatNum(result)
            leftOperand = result
        } else {
            leftOperand = current
        }
        pendingOp = op
        awaitingOperand = true
    }

    fun pressEquals() {
        val right = display.toDoubleOrNull() ?: 0.0
        val left = leftOperand ?: return
        val op = pendingOp ?: return
        display = formatNum(compute(left, op, right))
        leftOperand = null
        pendingOp = null
        awaitingOperand = false
    }

    fun pressClear() {
        display = "0"
        leftOperand = null
        pendingOp = null
        awaitingOperand = false
    }

    fun pressBackspace() {
        display = if (display.length <= 1) "0" else display.dropLast(1)
    }

    fun formattedDisplay(): String {
        val value = display.toDoubleOrNull() ?: 0.0
        return if (value == kotlin.math.floor(value) && !value.isInfinite())
            value.toLong().toString()
        else
            "%.2f".format(value).trimEnd('0').trimEnd('.')
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            // Auto-fill the amount field if the user typed something before swiping away
            if (display != "0") onDone(formattedDisplay()) else onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Expression hint (e.g. "150 +")
            if (leftOperand != null && pendingOp != null) {
                Text(
                    text = "${formatNum(leftOperand!!)} $pendingOp",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.End,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(Modifier.height(17.dp))
            }

            // Main display
            Text(
                text = display,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.End,
                fontSize = 40.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            HorizontalDivider(color = AppColors.BorderLight, thickness = 0.5.dp)
            Spacer(Modifier.height(4.dp))

            val btnHeight = Modifier.height(58.dp)

            // Row 1: AC  ⌫  ÷  ×
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcBtn("AC", btnHeight.weight(1f), AppColors.SpentFill, AppColors.SpentText) { pressClear() }
                CalcBtn("⌫", btnHeight.weight(1f), AppColors.SpentFill, AppColors.SpentText) { pressBackspace() }
                CalcBtn("÷", btnHeight.weight(1f), AppColors.BalanceFillLight, AppColors.BalanceTextLight) { pressOperator("÷") }
                CalcBtn("×", btnHeight.weight(1f), AppColors.BalanceFillLight, AppColors.BalanceTextLight) { pressOperator("×") }
            }

            // Row 2: 7  8  9  -
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcBtn("7", btnHeight.weight(1f)) { appendDigit("7") }
                CalcBtn("8", btnHeight.weight(1f)) { appendDigit("8") }
                CalcBtn("9", btnHeight.weight(1f)) { appendDigit("9") }
                CalcBtn("-", btnHeight.weight(1f), AppColors.BalanceFillLight, AppColors.BalanceTextLight) { pressOperator("-") }
            }

            // Row 3: 4  5  6  +
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcBtn("4", btnHeight.weight(1f)) { appendDigit("4") }
                CalcBtn("5", btnHeight.weight(1f)) { appendDigit("5") }
                CalcBtn("6", btnHeight.weight(1f)) { appendDigit("6") }
                CalcBtn("+", btnHeight.weight(1f), AppColors.BalanceFillLight, AppColors.BalanceTextLight) { pressOperator("+") }
            }

            // Row 4: 1  2  3  .
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcBtn("1", btnHeight.weight(1f)) { appendDigit("1") }
                CalcBtn("2", btnHeight.weight(1f)) { appendDigit("2") }
                CalcBtn("3", btnHeight.weight(1f)) { appendDigit("3") }
                CalcBtn(".", btnHeight.weight(1f)) { appendDecimal() }
            }

            // Row 5: 0 (wide)  =  (wide)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcBtn("0", btnHeight.weight(2f)) { appendDigit("0") }
                CalcBtn("=", btnHeight.weight(2f), AppColors.BalanceFillLight, AppColors.BalanceTextLight) { pressEquals() }
            }

            Spacer(Modifier.height(4.dp))

            PrimaryButton(
                text = "Use this amount",
                onClick = { onDone(formattedDisplay()) },
            )
        }
    }
}

@Composable
private fun CalcBtn(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    val resolvedContent = if (contentColor == Color.Unspecified)
        MaterialTheme.colorScheme.onSurface else contentColor
    val hasTint = containerColor != Color.Transparent
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = resolvedContent,
        ),
        border = if (hasTint) null else BorderStroke(0.5.dp, AppColors.BorderLight),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
        ),
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.W500)
    }
}
