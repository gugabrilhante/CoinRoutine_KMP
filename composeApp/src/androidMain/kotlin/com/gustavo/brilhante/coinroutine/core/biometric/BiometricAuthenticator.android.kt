package com.gustavo.brilhante.coinroutine.core.biometric

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.gustavo.brilhante.coinroutine.biometric.AndroidBiometricAuthenticator

class AndroidPlatformContext(val context: Context) : PlatformContext

@Composable
actual fun getPlatformContext(): PlatformContext {
    val context = LocalContext.current
    return AndroidPlatformContext(context)
}

actual fun getBiometricAuthenticator(context: PlatformContext): BiometricAuthenticator {
    val androidContext = (context as AndroidPlatformContext).context
    return AndroidBiometricAuthenticator(androidContext)
}