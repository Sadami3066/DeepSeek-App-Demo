package com.opends.chat.ui

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opends.chat.core.OpenDS
import com.opends.chat.core.ThemeMode
import com.opends.chat.design.theme.OdDensity
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OpenDSTheme
import com.opends.chat.feature.chat.ChatRoute
import com.opends.chat.feature.chat.SessionSearchRoute
import com.opends.chat.feature.shell.ApiKeyOnboarding
import com.opends.chat.feature.shell.SettingsRoute
import com.opends.chat.feature.shell.SiderContent
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AppRoot() {
    val settings by OpenDS.settings.state.collectAsStateWithLifecycle()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val dark = when (settings.theme) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    OpenDSTheme(darkTheme = dark) {
        OdDensity {
            if (settings.apiKey.isBlank()) {
                ApiKeyOnboarding(onDone = { })
            } else {
                AppShell()
            }
        }
    }
}

private const val DRAWER_OPEN_MS = 256
private const val DRAWER_SCALE_DELTA = 0.05f
private const val DRAWER_PAGE_ALPHA_LIGHT = 0.66f
private const val DRAWER_PAGE_ALPHA_DARK = 0.52f
private const val DRAWER_DARK_LIFT = 0.05f
private val DRAWER_EDGE_WIDTH = 27.dp
private val DRAWER_ELEVATION_LIGHT = 10.dp
private val DRAWER_ELEVATION_DARK = 1.dp
private val DRAWER_CORNER = 24.dp
private val DRAWER_VELOCITY = 90.dp
private const val DRAWER_WIDTH_RATIO = 0.7926f

@Composable
private fun AppShell() {
    var sessionId by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var siderOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettings || showSearch || siderOpen) {
        when {
            siderOpen -> siderOpen = false
            showSearch -> showSearch = false
            showSettings -> showSettings = false
        }
    }

    val colors = OdTheme.colors
    val dark = OdTheme.isDark
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val hostView = LocalView.current
    DisposableEffect(hostView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val width = with(density) { 24.dp.toPx().toInt() }
            val height = with(density) { 200.dp.toPx().toInt() }
            val viewHeight = hostView.height.takeIf { it > 0 } ?: 2400
            val top = ((viewHeight - height) / 2).coerceAtLeast(0)
            hostView.systemGestureExclusionRects =
                listOf(Rect(0, top, width, top + height))
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hostView.systemGestureExclusionRects = emptyList()
            }
        }
    }
    val progress = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    val dragValue = remember { mutableFloatStateOf(0f) }
    fun currentProgress(): Float = if (dragging) dragValue.floatValue else progress.value
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val context = LocalContext.current
    val screenWidthPx = context.resources.displayMetrics.widthPixels.toFloat()
    val translationPx = screenWidthPx * DRAWER_WIDTH_RATIO
    val edgePx = with(density) { DRAWER_EDGE_WIDTH.toPx() }
    val velocityThreshold = with(density) { DRAWER_VELOCITY.toPx() } / 1000f
    val cornerPx = with(density) { DRAWER_CORNER.toPx() }
    val pageAlphaDelta = if (dark) DRAWER_PAGE_ALPHA_DARK else DRAWER_PAGE_ALPHA_LIGHT
    val rootBackground = colors.sidebarFill

    LaunchedEffect(siderOpen) {
        if (!dragging) {
            progress.animateTo(
                targetValue = if (siderOpen) 1f else 0f,
                animationSpec = tween(DRAWER_OPEN_MS, easing = FastOutSlowInEasing),
            )
        }
    }

    val scrimActive by remember {
        derivedStateOf { (if (dragging) dragValue.floatValue else progress.value) > 0.001f }
    }

    fun settle(open: Boolean, from: Float) {
        siderOpen = open
        scope.launch {
            progress.snapTo(from.coerceIn(0f, 1f))
            dragging = false
            progress.animateTo(
                targetValue = if (open) 1f else 0f,
                animationSpec = tween(DRAWER_OPEN_MS, easing = FastOutSlowInEasing),
            )
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(rootBackground)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
                    if (currentProgress() > 0.001f) return@awaitEachGesture
                    var totalX = 0f
                    var totalY = 0f
                    var claimed = false
                    var velocity = 0f
                    var lastTime = down.uptimeMillis
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val dx = change.positionChange().x
                        val dy = change.positionChange().y
                        if (!claimed && (dx != 0f || dy != 0f) && change.isConsumed) break
                        totalX += dx
                        totalY += dy
                        if (!claimed) {
                            if (abs(totalX) > touchSlop && abs(totalX) > abs(totalY) * 1.3f) {
                                if (totalX > 0f) claimed = true else break
                            } else if (abs(totalY) > touchSlop) {
                                break
                            }
                        }
                        if (claimed) {
                            change.consume()
                            if (!dragging) {
                                dragValue.floatValue = currentProgress()
                                dragging = true
                            }
                            dragValue.floatValue = (dragValue.floatValue + dx / translationPx).coerceIn(0f, 1f)
                            val now = change.uptimeMillis
                            val dt = (now - lastTime).coerceAtLeast(1L)
                            velocity = dx / dt
                            lastTime = now
                        }
                        if (!change.pressed) break
                    }
                    if (claimed) {
                        val value = dragValue.floatValue
                        settle(velocity > velocityThreshold || value > 0.15f, value)
                    }
                }
            },
    ) {
        val drawerWidth: Dp = maxWidth * DRAWER_WIDTH_RATIO
        val drawerWidthPx = with(density) { drawerWidth.toPx() }

        val cardShape = RoundedCornerShape(
            DRAWER_CORNER * (if (dragging) dragValue.floatValue else progress.value),
        )

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = if (dragging) dragValue.floatValue else progress.value
                    val scale = 1f - DRAWER_SCALE_DELTA * p
                    scaleX = scale
                    scaleY = scale
                    translationX = translationPx * p
                }
                .shadow(
                    elevation = (if (dark) DRAWER_ELEVATION_DARK else DRAWER_ELEVATION_LIGHT) *
                        (if (dragging) dragValue.floatValue else progress.value),
                    shape = cardShape,
                    clip = false,
                )
                .clip(cardShape)
                .background(if (dark) colors.bgLayer2 else colors.bgBase),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - pageAlphaDelta * (if (dragging) dragValue.floatValue else progress.value)
                    },
            ) {
                when {
                    showSettings -> SettingsRoute(onBack = { showSettings = false })
                    showSearch -> SessionSearchRoute(
                        onBack = { showSearch = false },
                        onOpenSession = {
                            sessionId = it
                            showSearch = false
                        },
                    )
                    else -> ChatRoute(
                        sessionId = sessionId,
                        onSessionCreated = { sessionId = it },
                        onOpenSider = { siderOpen = true },
                        onOpenSettings = { showSettings = true },
                    )
                }
                if (dark) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = DRAWER_DARK_LIFT * (if (dragging) dragValue.floatValue else progress.value) }
                            .background(Color.White),
                    )
                }
            }
        }

        if (scrimActive) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (currentProgress() < 0.5f) return@awaitEachGesture
                            var totalX = 0f
                            var claimed = false
                            var velocity = 0f
                            var lastTime = down.uptimeMillis
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                val dx = change.positionChange().x
                                totalX += dx
                                if (!claimed && abs(totalX) > touchSlop) claimed = true
                                if (claimed) {
                                    change.consume()
                                    if (!dragging) {
                                        dragValue.floatValue = currentProgress()
                                        dragging = true
                                    }
                                    dragValue.floatValue = (dragValue.floatValue + dx / drawerWidthPx).coerceIn(0f, 1f)
                                    val now = change.uptimeMillis
                                    val dt = (now - lastTime).coerceAtLeast(1L)
                                    velocity = dx / dt
                                    lastTime = now
                                }
                                if (!change.pressed) break
                            }
                            if (claimed) {
                                val value = dragValue.floatValue
                                settle(!(velocity < -velocityThreshold || value < 0.85f), value)
                            } else if (currentProgress() > 0.9f) {
                                settle(false, currentProgress())
                            }
                        }
                    },
            )
        }

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .width(drawerWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    val p = if (dragging) dragValue.floatValue else progress.value
                    translationX = -drawerWidthPx * (1f - p)
                }
                .background(colors.sidebarFill),
        ) {
            SiderContent(
                currentSessionId = sessionId,
                onSelectSession = {
                    sessionId = it
                    showSearch = false
                    showSettings = false
                    siderOpen = false
                },
                onOpenSettings = {
                    siderOpen = false
                    showSettings = true
                },
                onClose = { siderOpen = false },
                onOpenSearch = {
                    siderOpen = false
                    showSearch = true
                },
            )
        }
    }
}
