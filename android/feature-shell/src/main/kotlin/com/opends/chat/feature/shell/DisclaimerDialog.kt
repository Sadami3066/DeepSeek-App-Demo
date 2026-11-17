package com.opends.chat.feature.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.opends.chat.design.theme.OdDimens
import com.opends.chat.design.theme.OdTheme
import com.opends.chat.design.theme.OdType

@Composable
internal fun DisclaimerDialog(strings: ShellStrings, onAccept: () -> Unit) {
    val colors = OdTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.overlay)
            .shellClickable {},
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(OdDimens.dialogWidth)
                    .clip(RoundedCornerShape(OdDimens.dialogRadius))
                    .background(colors.menu)
                    .shellClickable {},
            ) {
                ShellDialogTitleBlock(strings.disclaimerTitle)
                SettingsHairline(colors.dividerStrong)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.3.dp, vertical = 16.dp),
                ) {
                    strings.disclaimerParagraphs.forEachIndexed { index, paragraph ->
                        Text(
                            text = paragraph,
                            style = OdType.dialogBody,
                            color = colors.labelSecondary,
                        )
                        if (index != strings.disclaimerParagraphs.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
                SettingsHairline(colors.dividerStrong)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(OdDimens.settingsRowHeight)
                        .shellClickable(onAccept),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = strings.disclaimerAccept,
                        style = OdType.dialogAction,
                        color = colors.brandPrimary,
                    )
                }
            }
        }
    }
}
