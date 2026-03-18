package com.gustavo.brilhante.coinroutine.core.biometric

import androidx.compose.runtime.Composable
import com.gustavo.brilhante.coinroutine.biometric.IosBiometricAuthenticator

object IosPlatformContext : PlatformContext

@Composable
actual fun getPlatformContext(): PlatformContext = IosPlatformContext

actual fun getBiometricAuthenticator(context: PlatformContext): BiometricAuthenticator =
    IosBiometricAuthenticator()