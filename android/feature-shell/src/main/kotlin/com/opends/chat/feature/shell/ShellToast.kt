package com.opends.chat.feature.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdTheme
import kotlinx.coroutines.delay

@Stable
internal class ShellToastState {
    var message by mutableStateOf<String?>(null)
        private set
    var isError by mutableStateOf(false)
        private set
    var token by mutableIntStateOf(0)
        private set

    fun show(text: String, error: Boolean = false) {
        message = text
        isError = error
        token += 1
    }

    fun dismiss(shownToken: Int) {
        if (token == shownToken) {
            message = null
        }
    }
}

@Composable
internal fun rememberShellToast(): ShellToastState = remember { ShellToastState() }

@Composable
internal fun ShellToastHost(state: ShellToastState, modifier: Modifier = Modifier) {
    val message = state.message
    LaunchedEffect(state.token) {
        val shownToken = state.token
        if (message != null) {
            delay(3000)
            state.dismiss(shownToken)
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 40.dp, start = 14.dp, end = 14.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
            exit = fadeOut(tween(120)),
        ) {
            ToastCard(text = message.orEmpty(), error = state.isError)
        }
    }
}

@Composable
private fun ToastCard(text: String, error: Boolean) {
    val colors = OdTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .clip(shape)
            .background(colors.toastBg)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.Top,
    ) {
        if (error) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_warning_outline_16),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 3.dp, end = 7.dp)
                    .size(16.dp),
                tint = colors.stateErrorPrimary,
            )
        }
        Text(
            text = text,
            color = colors.labelPrimaryForeground,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = if (error) TextAlign.Start else TextAlign.Center,
        )
    }
}
