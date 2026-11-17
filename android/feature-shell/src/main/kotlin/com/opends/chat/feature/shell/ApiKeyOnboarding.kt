package com.opends.chat.feature.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.core.OpenDSApi
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val API_KEY_URL = "https://platform.deepseek.com/api_keys"

@Composable
fun ApiKeyOnboarding(onDone: () -> Unit) {
    val colors = OdTheme.colors
    val strings = shellStrings(rememberSettingsState().languageTag)
    val store = remember { runningSettings() }
    val toast = rememberShellToast()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by rememberSaveable { mutableStateOf("") }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }
    var pendingKey by remember { mutableStateOf<String?>(null) }

    fun saveKeyAndFinish(key: String) {
        if (verifying) return
        verifying = true
        scope.launch {
            val saved = runCatching { store?.setApiKey(key) ?: error("unavailable") }
            verifying = false
            if (saved.isSuccess) {
                toast.show(strings.onboardingSaved)
                delay(180)
                onDone()
            } else {
                error = strings.operationFailed
            }
        }
    }

    BackHandler(enabled = showDisclaimer) {}

    fun submit() {
        if (verifying) return
        val key = input.trim()
        if (key.isEmpty()) {
            error = strings.apiKeyEmpty
            return
        }
        verifying = true
        error = null
        scope.launch {
            val verification = OpenDSApi.verifyApiKey(key)
            verifying = false
            if (verification.isFailure) {
                error = strings.verifyFailed(verification.exceptionOrNull()?.message.orEmpty())
            } else if (isDisclaimerAccepted(context)) {
                saveKeyAndFinish(key)
            } else {
                pendingKey = key
                showDisclaimer = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))
            OpenDSBrandMark(size = 72.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                text = strings.onboardingTitle,
                color = colors.labelPrimary,
                fontSize = 21.2.sp,
                lineHeight = 31.3.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = strings.onboardingSubtitle,
                color = colors.labelSecondary,
                style = OdType.bodySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            ShellTextField(
                value = input,
                onValueChange = {
                    input = it
                    error = null
                },
                placeholder = strings.apiKeyPlaceholder,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.stateErrorPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            ShellPrimaryButton(
                text = if (verifying) strings.verifying else strings.startUsing,
                onClick = { submit() },
                enabled = !verifying && input.isNotBlank(),
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.shellClickable {
                    if (!openExternalUrl(context, API_KEY_URL)) {
                        toast.show(strings.openLinkFailed, true)
                    }
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = strings.getApiKey,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = colors.brandText,
                )
                Spacer(Modifier.width(4.dp))
                ShellIcon(
                    res = R.drawable.ic_redirect_regular,
                    size = 12.dp,
                    tint = colors.brandText,
                )
            }
            Spacer(Modifier.weight(1f).height(24.dp))
            Row(
                modifier = Modifier.shellClickable { showPrivacy = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                ShellIcon(
                    res = R.drawable.ic_info_outline_16,
                    size = 14.dp,
                    tint = colors.labelTertiary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.privacy,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.labelTertiary,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
        ShellToastHost(state = toast, modifier = Modifier.align(Alignment.TopCenter))
    }

    if (showPrivacy) {
        ShellMessageDialog(
            title = strings.privacyTitle,
            message = strings.privacyContent,
            confirmLabel = strings.confirm,
            onDismiss = { showPrivacy = false },
        )
    }

    if (showDisclaimer) {
        DisclaimerDialog(strings = strings) {
            acceptDisclaimer(context)
            showDisclaimer = false
            val key = pendingKey
            pendingKey = null
            if (key != null) {
                saveKeyAndFinish(key)
            }
        }
    }
}
