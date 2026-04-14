package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.ErrorRed
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.TextSecondary

@Composable
fun SweetHomePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = SweetHomeShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = OnPrimaryWhite,
            disabledContainerColor = DividerColor,
            disabledContentColor = TextSecondary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun SweetHomeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = SweetHomeShapes.Medium,
        border = BorderStroke(1.5.dp, if (enabled) PrimaryGreen else DividerColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PrimaryGreen,
            disabledContentColor = TextSecondary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun SweetHomeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = SweetHomeShapes.Medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = PrimaryGreen,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun SweetHomeDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = SweetHomeShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorRed,
            contentColor = OnPrimaryWhite,
            disabledContainerColor = DividerColor,
            disabledContentColor = TextSecondary,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
