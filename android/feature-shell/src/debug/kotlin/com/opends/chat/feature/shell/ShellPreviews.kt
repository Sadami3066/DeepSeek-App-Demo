package com.opends.chat.feature.shell

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import com.opends.chat.design.theme.OpenDSTheme

@Preview(name = "Sider light", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SiderPreview() {
    OpenDSTheme(darkTheme = false) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.width(300.dp).fillMaxSize()) {
                SiderContent(
                    currentSessionId = null,
                    onSelectSession = {},
                    onOpenSettings = {},
                    onClose = {},
                    onOpenSearch = {},
                )
            }
        }
    }
}

@Preview(name = "Sider dark", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SiderDarkPreview() {
    OpenDSTheme(darkTheme = true) {
        Box(Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
            Box(Modifier.width(300.dp).fillMaxSize()) {
                SiderContent(
                    currentSessionId = null,
                    onSelectSession = {},
                    onOpenSettings = {},
                    onClose = {},
                    onOpenSearch = {},
                )
            }
        }
    }
}

@Preview(name = "Settings light", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SettingsPreview() {
    OpenDSTheme(darkTheme = false) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides PreviewActivityResultOwner) {
            SettingsRoute(onBack = {})
        }
    }
}

@Preview(name = "Settings dark", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun SettingsDarkPreview() {
    OpenDSTheme(darkTheme = true) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides PreviewActivityResultOwner) {
            SettingsRoute(onBack = {})
        }
    }
}

@Preview(name = "Onboarding light", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun OnboardingPreview() {
    OpenDSTheme(darkTheme = false) {
        ApiKeyOnboarding(onDone = {})
    }
}

@Preview(name = "Onboarding dark", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun OnboardingDarkPreview() {
    OpenDSTheme(darkTheme = true) {
        ApiKeyOnboarding(onDone = {})
    }
}

private class PreviewActivityResultRegistry : ActivityResultRegistry() {
    override fun <I, O> onLaunch(
        requestCode: Int,
        contract: ActivityResultContract<I, O>,
        input: I,
        options: ActivityOptionsCompat?,
    ) {
    }
}

private object PreviewActivityResultOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry = PreviewActivityResultRegistry()
}
