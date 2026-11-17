package com.opends.chat.feature.chat.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.opends.chat.design.icon.OfficialIcons

object AppIcons {
    val menu = OfficialIcons.menuOutline24
    val newChat = OfficialIcons.newChatOutline24
    val autoRead = OfficialIcons.muteAudioOutline24
    val autoReadActive = OfficialIcons.playingFill24
    val send = OfficialIcons.sendFill20
    val stop = OfficialIcons.stopFill20
    val voice = OfficialIcons.voiceOutline26
    val mic = OfficialIcons.micFilled20
    val attach = OfficialIcons.plusOutline20
    val paperclip = OfficialIcons.paperclipOutline20
    val think = OfficialIcons.thinkOutline16
    val search = OfficialIcons.searchOutline16
    val globe = OfficialIcons.globeLanguageOutline16
    val copy = OfficialIcons.copyOutline
    val edit = OfficialIcons.editOutline20
    val refresh = OfficialIcons.refreshOutline20
    val share = OfficialIcons.shareOutline20
    val like = OfficialIcons.likeOutline
    val likeFill = OfficialIcons.likeFill
    val dislike = OfficialIcons.dislikeOutline
    val dislikeFill = OfficialIcons.dislikeFill
    val play = OfficialIcons.playMediaOutline20
    val stopSpeech = OfficialIcons.stopSpeechOutline20
    val playing = OfficialIcons.playingFill24
    val chevronDown = OfficialIcons.chevronDownOutline20
    val chevronRight = OfficialIcons.chevronRightOutline20
    val chevronLeft = OfficialIcons.chevronLeftOutline20
    val close = OfficialIcons.closeOutline16
    val close20 = OfficialIcons.closeOutline20
    val close24 = OfficialIcons.closeOutline24
    val check = OfficialIcons.checkOutline20
    val checked = OfficialIcons.checkedFilled16
    val warning = OfficialIcons.warningOutline16
    val info = OfficialIcons.infoOutline16
    val keyboard = OfficialIcons.keyboardOutline26
    val camera = OfficialIcons.cameraOutline20
    val photo = OfficialIcons.photoOutline20
    val folder = OfficialIcons.folderOutline20
    val link = OfficialIcons.linkOutline20
    val browse = OfficialIcons.browseOutline16
    val browseWarning = OfficialIcons.browseWarningOutline16
    val searchWarning = OfficialIcons.searchWarningOutline16
    val file = OfficialIcons.typeText
    val photoError = OfficialIcons.photoErrorFilled2
    val sitePlaceholder = OfficialIcons.siteIconPlaceholder
    val more = OfficialIcons.ellipsisOutline
    val loading = OfficialIcons.loading
    val branch = OfficialIcons.branchOutline20
    val arrowRight = OfficialIcons.arrowRightOutline20
    val cancelSearch = OfficialIcons.cancelSearchOutline20
    val trash = OfficialIcons.trashOutline20
    val add = OfficialIcons.addOutline26
    val refreshCircle = OfficialIcons.refreshCircleFill
    val selectText = OfficialIcons.selectAllOutline20
    val report = OfficialIcons.reportOutline
    val erase = OfficialIcons.trashOutline20
    val find: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_find_page",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString(
                    "M10.5,3.6C14.31,3.6 17.4,6.69 17.4,10.5C17.4,14.31 14.31,17.4 10.5,17.4C6.69,17.4 3.6,14.31 3.6,10.5C3.6,6.69 6.69,3.6 10.5,3.6Z",
                ).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.4,15.4L19.6,19.6").toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.2,8.4H13.6V9.5H7.2V8.4Z").toNodes(),
                fill = SolidColor(Color.Black),
            )
            addPath(
                pathData = PathParser().parsePathString("M7.2,10.5H11.3V11.6H7.2V10.5Z").toNodes(),
                fill = SolidColor(Color.Black),
            )
        }.build()
    }
}
