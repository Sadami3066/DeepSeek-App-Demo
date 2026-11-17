package com.opends.chat.core.attach

import android.content.Context
import android.net.Uri
import com.opends.chat.core.AttachmentRef
import com.opends.chat.core.AttachmentStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class AttachmentStoreImpl(context: Context) : AttachmentStore {

    private val appContext = context.applicationContext
    private val directory = File(appContext.filesDir, "attachments").apply { mkdirs() }

    override suspend fun import(attachment: AttachmentRef): AttachmentRef = withContext(Dispatchers.IO) {
        val target = File(directory, attachment.id)
        if (!target.exists() || target.length() == 0L) {
            val uri = Uri.parse(attachment.uri)
            val stream = when (uri.scheme) {
                "file" -> uri.path?.let { path ->
                    File(path).takeIf { it.exists() }?.inputStream()
                }
                else -> appContext.contentResolver.openInputStream(uri)
            } ?: throw java.io.IOException("无法读取附件")
            stream.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
            if (!target.exists() || target.length() == 0L) {
                target.delete()
                throw java.io.IOException("无法读取附件")
            }
        }
        attachment
    }

    override fun findLocal(attachment: AttachmentRef): File? {
        val byId = File(directory, attachment.id)
        if (byId.exists()) return byId
        val uri = Uri.parse(attachment.uri)
        if (uri.scheme == "file") {
            val file = uri.path?.let { File(it) }
            if (file != null && file.exists()) return file
        }
        return null
    }

    fun localFile(attachmentId: String): File? =
        File(directory, attachmentId).takeIf { it.exists() }

    fun save(id: String, bytes: ByteArray): File {
        val target = File(directory, id)
        target.writeBytes(bytes)
        return target
    }

    fun localPath(attachmentId: String): String? = localFile(attachmentId)?.absolutePath

    fun sniffMime(file: File): String = runCatching { sniffMime(file.readBytes()) }.getOrDefault("")

    fun sniffMime(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
            return "image/jpeg"
        }
        if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        ) {
            return "image/png"
        }
        if (bytes.size >= 6 && bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
            return "image/gif"
        }
        if (bytes.size >= 12 && bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()
        ) {
            return "image/webp"
        }
        if (bytes.size >= 2 && bytes[0] == 'B'.code.toByte() && bytes[1] == 'M'.code.toByte()) {
            return "image/bmp"
        }
        if (bytes.size >= 4 && bytes[0] == 0x49.toByte() && bytes[1] == 0x49.toByte() &&
            bytes[2] == 0x2A.toByte() && bytes[3] == 0x00.toByte()
        ) {
            return "image/tiff"
        }
        if (bytes.size >= 4 && bytes[0] == 0x4D.toByte() && bytes[1] == 0x4D.toByte() &&
            bytes[2] == 0x00.toByte() && bytes[3] == 0x2A.toByte()
        ) {
            return "image/tiff"
        }
        if (bytes.size >= 12 && bytes[4] == 'f'.code.toByte() && bytes[5] == 't'.code.toByte() &&
            bytes[6] == 'y'.code.toByte() && bytes[7] == 'p'.code.toByte()
        ) {
            val brand = String(bytes, 8, 4, Charsets.US_ASCII)
            if (brand == "avif" || brand == "avis") return "image/avif"
        }
        if (bytes.size >= 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() &&
            bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte()
        ) {
            return "application/pdf"
        }
        return ""
    }
}
