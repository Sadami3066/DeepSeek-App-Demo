package com.opends.chat.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opends.chat.design.R
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

private val SubPageTopGap = 11.8.dp
private val SubPageCardGap = 24.dp
private val DescStartPadding = 15.3.dp
private val FontPanelRadius = 16.dp
private val FontPanelStart = 22.15.dp
private val PreviewBubbleRadius = 20.dp
private val TermsBaseZh = "https://cdn.deepseek.com/policies/zh-CN/"
private val TermsBaseEn = "https://cdn.deepseek.com/policies/en-US/"
private val HelpUrl = "https://cdn.deepseek.com/faq"

@Composable
private fun SettingsSubPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OdTheme.colors.bgLayer2)
            .statusBarsPadding(),
    ) {
        ShellTopBar(title = title, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = OdDimens.settingsCardMargin),
        ) {
            Spacer(Modifier.height(SubPageTopGap))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

internal fun fontLevelOfSystem(systemFontScale: Float): Int = when {
    systemFontScale <= 0.8f -> 1
    systemFontScale <= 0.9f -> 2
    systemFontScale <= 1.01f -> 3
    systemFontScale <= 1.1f -> 4
    systemFontScale <= 1.2f -> 5
    systemFontScale <= 1.4f -> 6
    else -> 7
}

@Composable
internal fun FontSizePage(
    strings: ShellStrings,
    level: Int,
    onLevelChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val colors = OdTheme.colors
    val systemFontScale = LocalDensity.current.fontScale
    var draft by remember { mutableStateOf(level) }
    androidx.compose.runtime.LaunchedEffect(level) {
        if (level != draft) {
            draft = level
        }
    }
    androidx.compose.runtime.LaunchedEffect(draft) {
        if (draft != level) {
            kotlinx.coroutines.delay(300)
            onLevelChange(draft)
        }
    }
    val displayLevel = if (draft == 0) fontLevelOfSystem(systemFontScale) else draft
    val levelScale = if (draft == 0) 1f else com.opends.chat.core.OpenDSApi.fontSizeScale(draft)
    val previewScale = (levelScale * systemFontScale).coerceIn(0.85f, 2.0f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgLayer2)
            .statusBarsPadding(),
    ) {
        ShellTopBar(title = strings.fontSize, onBack = onBack)
        androidx.compose.runtime.CompositionLocalProvider(
            LocalDensity provides androidx.compose.ui.unit.Density(
                density = LocalDensity.current.density,
                fontScale = previewScale,
            ),
        ) {
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Spacer(Modifier.height(34.2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.2.dp)
                            .height(40.1.dp)
                            .clip(RoundedCornerShape(PreviewBubbleRadius))
                            .background(colors.bubble),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = strings.fontPreviewUser,
                            style = OdType.bubble,
                            color = colors.bubbleText,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                Spacer(Modifier.height(28.8.dp))
                Text(
                    text = strings.fontPreviewHint,
                    modifier = Modifier.padding(start = 22.15.dp, end = 20.4.dp),
                    style = OdType.body,
                    color = colors.bodyText,
                )
                Spacer(Modifier.height(27.3.dp))
                Text(
                    text = strings.fontPreviewFeedback,
                    modifier = Modifier.padding(start = 22.15.dp, end = 20.4.dp),
                    style = OdType.body,
                    color = colors.bodyText,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = FontPanelRadius, topEnd = FontPanelRadius))
                .background(colors.menu)
                .navigationBarsPadding()
                .padding(start = FontPanelStart, end = 15.1.dp),
        ) {
            Spacer(Modifier.height(25.3.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.fontFollowSystem,
                    style = OdType.rowTitle,
                    color = colors.labelPrimary,
                )
                Spacer(Modifier.weight(1f))
                GreenSwitch(
                    checked = draft == 0,
                    onCheckedChange = { follow ->
                        draft = if (follow) 0 else fontLevelOfSystem(systemFontScale)
                    },
                )
            }
            Spacer(Modifier.height(35.3.dp))
            SettingsLevelSlider(
                level = draft,
                displayLevel = displayLevel,
                onLevelChange = { draft = it },
            )
            Spacer(Modifier.height(39.75.dp))
        }
    }
}

@Composable
internal fun DataManagementPage(
    strings: ShellStrings,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onDeleteAll: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = OdTheme.colors
    SettingsSubPage(title = strings.dataManagement, onBack = onBack) {
        SettingsCard {
            SettingsRow(
                title = strings.importChat,
                onClick = onImport,
            )
        }
        Spacer(Modifier.height(SubPageCardGap))
        SettingsCard {
            SettingsRow(
                title = strings.exportZip,
                onClick = onExport,
            )
        }
        Spacer(Modifier.height(SubPageCardGap))
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OdDimens.settingsRowHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDeleteAll,
                    )
                    .padding(horizontal = 11.8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.deleteAllChats,
                    style = OdType.rowTitle,
                    color = colors.stateErrorPrimary,
                )
            }
        }
    }
}

@Composable
internal fun PersonalizationPage(
    strings: ShellStrings,
    autoFold: Boolean,
    onAutoFoldChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = OdTheme.colors
    SettingsSubPage(title = strings.personalization, onBack = onBack) {
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(OdDimens.settingsRowHeight)
                    .padding(start = 11.8.dp, end = 11.8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.autoFoldThinking,
                    modifier = Modifier.weight(1f),
                    style = OdType.rowTitle,
                    color = colors.labelPrimary,
                )
                GreenSwitch(checked = autoFold, onCheckedChange = onAutoFoldChange)
            }
        }
        Text(
            text = strings.autoFoldThinkingDesc,
            modifier = Modifier.padding(start = DescStartPadding, end = DescStartPadding, top = 8.dp),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = colors.labelTertiary,
        )
    }
}

@Composable
internal fun TermsPage(strings: ShellStrings, onBack: () -> Unit) {
    val english = strings.english
    val base = if (english) TermsBaseEn else TermsBaseZh
    val rows = listOf(
        strings.termsUserAgreement to (base + "deepseek-terms-of-use.html"),
        strings.termsPrivacy to (base + "deepseek-privacy-policy.html"),
        strings.termsThirdParty to (base + "third-party-info-sharing-list.html"),
        strings.termsPersonalInfo to (base + "collected-personal-information.html"),
        strings.termsPermissions to (base + "app-permissions.html"),
    )
    val context = androidx.compose.ui.platform.LocalContext.current
    SettingsSubPage(title = strings.serviceAgreement, onBack = onBack) {
        SettingsCard {
            rows.forEach { (label, url) ->
                SettingsRow(
                    title = label,
                    startPadding = 14.7.dp,
                    onClick = { openExternalUrl(context, url) },
                )
            }
        }
    }
}

@Composable
internal fun ApiKeyDialog(
    strings: ShellStrings,
    hasKey: Boolean,
    verifying: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val colors = OdTheme.colors
    var value by rememberSaveable { mutableStateOf("") }
    var visible by rememberSaveable { mutableStateOf(false) }
    ShellDialogCard(onDismiss = onDismiss, modifier = Modifier.width(OdDimens.dialogWidth)) {
        ShellDialogTitleBlock(strings.apiKeyTitle)
        SettingsHairline(colors.dividerStrong)
        Column(Modifier.padding(horizontal = 20.3.dp, vertical = 14.dp)) {
            Text(
                text = strings.apiKeyHint,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.labelSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.selector)
                    .padding(start = 14.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = colors.labelPrimary),
                    cursorBrush = SolidColor(colors.brandPrimary),
                    visualTransformation = if (visible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSave(value.trim()) }),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = if (hasKey) "••••••••••••" else strings.apiKeyPlaceholder,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp,
                                    color = colors.labelPlaceholder,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            inner()
                        }
                    },
                )
                ShellIconButton(
                    res = if (visible) R.drawable.ic_eye_regular else R.drawable.ic_eye_close_regular,
                    size = 20.dp,
                    onClick = { visible = !visible },
                    tint = colors.iconPrimary,
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, fontSize = 13.sp, lineHeight = 18.sp, color = colors.stateErrorPrimary)
            }
        }
        SettingsHairline(colors.dividerStrong)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .shellClickable(onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = strings.cancel, style = OdType.dialogAction, color = colors.labelSecondary)
            }
            Box(
                modifier = Modifier
                    .width(ShellHairline)
                    .fillMaxSize()
                    .background(colors.dividerStrong),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(
                        if (!verifying && value.isNotBlank()) {
                            Modifier.shellClickable { onSave(value.trim()) }
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (verifying) strings.verifying else strings.save,
                    style = OdType.dialogAction,
                    color = if (verifying || value.isBlank()) colors.labelTertiary else colors.brandPrimary,
                )
            }
        }
    }
}

internal fun helpFeedbackUrl(): String = HelpUrl
