package com.opends.chat.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

@Composable
internal fun UpdateAvailableDialog(
    strings: ShellStrings,
    release: UpdateRelease,
    downloading: Boolean,
    progress: Int?,
    onLater: () -> Unit,
    onUpdate: () -> Unit,
    onCancelDownload: () -> Unit,
) {
    val colors = OdTheme.colors
    ShellDialogCard(onDismiss = { if (!downloading) onLater() }) {
        ShellDialogTitleBlock(strings.newVersionFound(release.version))
        SettingsHairline(colors.dividerStrong)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.3.dp, vertical = 14.dp),
        ) {
            Text(
                text = release.notes.ifBlank { strings.newVersionTitle },
                style = OdType.dialogBody,
                color = colors.labelSecondary,
            )
        }
        if (downloading) {
            SettingsHairline(colors.dividerStrong)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.3.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = strings.downloading(progress ?: 0),
                    style = OdType.dialogBody,
                    color = colors.labelSecondary,
                )
            }
        }
        SettingsHairline(colors.dividerStrong)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(OdDimens.settingsRowHeight),
        ) {
            if (downloading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .shellClickable(onCancelDownload),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = strings.cancel, style = OdType.dialogAction, color = colors.labelSecondary)
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .shellClickable(onLater),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = strings.updateLater, style = OdType.dialogAction, color = colors.labelSecondary)
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
                        .shellClickable(onUpdate),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = strings.updateNow, style = OdType.dialogAction, color = colors.brandPrimary)
                }
            }
        }
    }
}
