package com.devindie.cmptemplate.feature.auth.impl.otp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding

@Composable
internal fun OtpVerificationContent(
    state: OtpVerificationUiState,
    onBackClick: () -> Unit,
    onDigitsChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onResendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .appStatusBarsPadding()
                .appNavigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("otp_screen"),
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text(
            text = "Enter the 6-digit code sent to:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = state.maskedPhone,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicTextField(
                value = state.digits,
                onValueChange = onDigitsChange,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("otp_input"),
                textStyle =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 32.sp,
                        textAlign = TextAlign.Start,
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                enabled = !state.isVerifying,
            )

            if (state.digits.isNotEmpty()) {
                IconButton(
                    onClick = onClearClick,
                    modifier =
                        Modifier
                            .size(33.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear code")
                }
            }
        }

        state.otpError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Text(
            text = "Didn't receive it?",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 32.dp),
        )

        if (state.resendSecondsRemaining > 0) {
            Text(
                text = formatResendCountdown(state.resendSecondsRemaining),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            TextButton(
                onClick = onResendClick,
                enabled = !state.isResending,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("Request new code")
            }
        }
    }
}

private fun formatResendCountdown(secondsRemaining: Int): String {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    return "Request new code in ${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
