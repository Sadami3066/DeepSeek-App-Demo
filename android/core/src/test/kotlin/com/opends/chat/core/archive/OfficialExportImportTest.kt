package com.opends.chat.core.archive

import com.opends.chat.core.db.ImportedSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.Base64

class OfficialExportImportTest {

    private val fixturesRoot = Files.createTempDirectory("opends-official-export").toFile()

    private fun officialZip(): File {
        val env = System.getenv("OPENDS_REAL_EXPORT_ZIP")
        return if (!env.isNullOrBlank()) File(env) else File("/tmp/opends-real-export.zip")
    }

    @Test
    fun readsStoreEntryWithDataDescriptor() {
        val bytes = Base64.getDecoder().decode(DESCRIPTOR_FIXTURE_BASE64)
        ArchiveZipSource.open(bytes, fixturesRoot).use { source ->
            val imageEntry = source.entries().first { it.name.endsWith("_image.png") }
            assertTrue(source.canRead(imageEntry))
            val expected = byteArrayOf(0x89.toByte()) + "PNG\r\n".toByteArray() +
                byteArrayOf(0x1A, 0x0A) + "FAKEDATA".toByteArray()
            assertTrue(source.read(imageEntry).contentEquals(expected))
            val inspection = ArchiveZipImport.inspect(source)
            assertEquals(1, inspection.sessions)
            assertEquals(0, inspection.messages)
        }
    }

    @Test
    fun importsRealOfficialExportInStreamingMode() {
        val zip = officialZip()
        assumeTrue("缺少官方导出样例，跳过：$zip", zip.exists())
        val started = System.currentTimeMillis()
        var sessions = 0
        var messages = 0
        var images = 0
        var imageBytes = 0L
        var sample: ImportedSession? = null
        ArchiveZipSource.open(zip.readBytes(), fixturesRoot).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            assertEquals(1028, inspection.sessions)
            assertTrue("消息总数应大于 6000，实际 ${inspection.messages}", inspection.messages > 6000)
            assertEquals(42, inspection.imagePaths.size)
            assertTrue("合法数据不应有非法片段，skipped=${inspection.skipped}", inspection.skipped == 0)
            ArchiveZipImport.copy(
                source = source,
                inspection = inspection,
                onSession = { session ->
                    sessions += 1
                    messages += session.messages.size
                    if (sample == null) sample = session
                },
                onImage = { _, data ->
                    images += 1
                    imageBytes += data.size
                },
            )
            assertEquals(inspection.sessions, sessions)
            assertEquals(inspection.messages, messages)
            assertEquals(42, images)
            assertTrue("图片总字节数异常：$imageBytes", imageBytes > 1_000_000L)
        }
        assertNotNull(sample)
        val session = sample!!
        session.messages.forEach { imported ->
            val parentId = imported.message.parentId
            if (parentId != null) {
                assertTrue(
                    "父消息必须存在：session=${session.id} message=${imported.message.id} parent=$parentId",
                    session.messages.any { it.message.id == parentId },
                )
            }
        }
        println(
            "[OfficialExportImportTest] sessions=$sessions messages=$messages images=$images " +
                "imageBytes=$imageBytes totalMs=${System.currentTimeMillis() - started}"
        )
    }

    companion object {
        private const val DESCRIPTOR_FIXTURE_BASE64 =
            "UEsDBBQACAAIAAAAAAAAAAAAAAAAAAAAAAANAAAAbWFuaWZlc3QuanNvbqtWSq0oyC8qiU/LL8pN" +
            "LIkvSy0qzszPU7IyrgUAUEsHCB/IYiMAAAAAGwAAAFBLAwQUAAgACAAAAAAAAAAAAAAAAAAAAAAA" +
            "FAAAAGNoYXRfc2Vzc2lvbnMveC5qc29ujcxBCkIxDATQu8y6StsPH8w5XClSio1SqOHTpCCId7eC" +
            "BxBmNzPvhVpAeMLBqjUGyWjNYasiPJtbbsoO19E7i6UHq+Y7p+/JO1RR7sYlZZtI9HHdhTBz9JGW" +
            "haLfh8N6mvbYSv5n9/MVdL68P1BLBwhXDulhAAAAAJwAAABQSwMEFAAIAAAAAAAAAAAAAAAAAAAA" +
            "AAAAACMAAABjaGF0X3Nlc3Npb25zL3hfZmlsZXMvYWJjX2ltYWdlLnBuZ4lQTkcNChoKRkFLRURB" +
            "VEFQSwcI4OL6MhAAAAAQAAAAUEsDBBQACAAIAAAAAAAAAAAAAAAAAAAAAAAXAAAAY2hhdF9zZXNz" +
            "aW9uX2luZGV4Lmpzb26FjFsKgzAQRfdyv6PkAYKzjn5ZRIIZSyCdhiaBQnHvZgfC/Tmcw33+EQMI" +
            "PyjUWBODpKWkkKMId3P4VFjhzaX4F2/7p0kFaYUohb+Vw+Y7w2o7Dcb0PbQl58jq0czT0m9bDv6+" +
            "O9cLUEsHCLu43ZEAAAAAiwAAAFBLAQIeAxQACAAIAAAAAAAfyGIjHQAAABsAAAANAAAAAAAAAAAA" +
            "IAAAAAAAAABtYW5pZmVzdC5qc29uUEsBAh4DFAAIAAgAAAAAAFcO6WFtAAAAnAAAABQAAAAAAAAA" +
            "AAAgAAAAWAAAAGNoYXRfc2Vzc2lvbnMveC5qc29uUEsBAh4DFAAIAAAAAAAAAODi+jIQAAAAEAAA" +
            "ACMAAAAAAAAAAAAgAAAABwEAAGNoYXRfc2Vzc2lvbnMveF9maWxlcy9hYmNfaW1hZ2UucG5nUEsB" +
            "Ah4DFAAIAAgAAAAAALu43ZFnAAAAiwAAABcAAAAAAAAAAAAgAAAAaAEAAGNoYXRfc2Vzc2lvbl9p" +
            "bmRleC5qc29uUEsFBgAAAAAEAAQAEwEAABQCAAAAAA=="
    }
}
