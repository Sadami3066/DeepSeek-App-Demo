package com.opends.chat.core.archive

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.opends.chat.core.TestPaths
import com.opends.chat.core.db.ImportedSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class OfficialExportImportTest {

    private val fixturesRoot = Files.createTempDirectory("opends-official-export").toFile()

    private fun sample(name: String): File = TestPaths.repoFile("app-data/imports/$name")

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
        val zip = sample("deepseek_data-official.zip")
        assumeTrue("缺少官方导出样例，跳过：$zip", zip.exists())
        val started = System.currentTimeMillis()
        var sessions = 0
        var messages = 0
        var images = 0
        var imageBytes = 0L
        var sampleSession: ImportedSession? = null
        ArchiveZipSource.open(zip).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            assertEquals(1028, inspection.sessions)
            assertTrue("消息总数应大于 6000，实际 ${inspection.messages}", inspection.messages > 6000)
            assertEquals(42, inspection.imagePaths.size)
            assertEquals("skipped=${inspection.skipped}", 0, inspection.skipped)
            ArchiveZipImport.copy(
                source = source,
                inspection = inspection,
                onSession = { session ->
                    sessions += 1
                    messages += session.messages.size
                    if (sampleSession == null) sampleSession = session
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
        assertNotNull(sampleSession)
        val session = sampleSession!!
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

    @Test
    fun importsArchiveWithIndexBeforeManifest() {
        val zip = sample("official_export_test.zip")
        assumeTrue("缺少样例，跳过：$zip", zip.exists())
        val expected = expectedIndexCounts(zip)
        ArchiveZipSource.open(zip).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            assertEquals(1, inspection.sessions)
            assertEquals(expected.first, inspection.messages)
            assertEquals(expected.second, inspection.sessions)
            var copied = 0
            ArchiveZipImport.copy(source, inspection, { _ -> copied += 1 }, { _, _ -> })
            assertEquals(1, copied)
        }
    }

    @Test
    fun importsArchiveWithoutUserJson() {
        val zip = sample("aaa_backup.zip")
        assumeTrue("缺少样例，跳过：$zip", zip.exists())
        ArchiveZipSource.open(zip).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            assertTrue(inspection.sessions > 0)
            assertTrue(inspection.messages > 0)
            var copied = 0
            var copiedMessages = 0
            ArchiveZipImport.copy(
                source,
                inspection,
                { session ->
                    copied += 1
                    copiedMessages += session.messages.size
                },
                { _, _ -> },
            )
            assertEquals(inspection.sessions, copied)
            assertEquals(inspection.messages, copiedMessages)
        }
    }

    @Test
    fun toleratesUnorderedStructureAndRepairsMessageTree() {
        val sessionJson = JsonObject().apply {
            addProperty("id", SESSION_ID)
            add("title", com.google.gson.JsonNull.INSTANCE)
            addProperty("pinned", false)
            addProperty("current_message_id", 12345)
            addProperty("inserted_at", "2026-11-11T02:33:20.196Z")
            addProperty("updated_at", "2026-11-11T02:33:26.307Z")
            add("messages", com.google.gson.JsonArray().apply {
                add(assistantMessage(4, 3, "assistant-4"))
                add(userMessage(3, 2, "user-3"))
                add(assistantMessage(2, 1, "assistant-2"))
                add(userMessage(1, null, "user-1", "chat_sessions/${SESSION_ID}_files/file-uuid1_image.png"))
                add(assistantMessage(9, 99, "orphan-assistant"))
            })
        }
        val indexJson = """[{"id":"another-session-id","title":null,"pinned":false,"message_count":2,""" +
            """"inserted_at":"2026-11-11T02:33:20.196Z","updated_at":"2026-11-11T02:33:26.307Z"}]"""
        val imagePath = "chat_sessions/${SESSION_ID}_files/file-uuid1_image.png"
        val zipBytes = buildZip(
            listOf(
                imagePath to byteArrayOf(1, 2, 3, 4),
                "notes.txt" to "ignored".toByteArray(),
                "chat_session_index.json" to indexJson.toByteArray(),
                "manifest.json" to """{"export_format_version":3}""".toByteArray(),
                "chat_sessions/$SESSION_ID.json" to sessionJson.toString().toByteArray(),
            )
        )
        ArchiveZipSource.open(zipBytes, fixturesRoot).use { source ->
            val inspection = ArchiveZipImport.inspect(source)
            assertEquals(1, inspection.sessions)
            assertEquals(5, inspection.messages)
            assertTrue(inspection.warnings.any { it.contains("未识别文件") })
            assertTrue(inspection.warnings.any { it.contains("会话索引与文件内容不一致") })
            assertTrue(inspection.warnings.any { it.contains("修复了 1 条缺失的父消息引用") })
            assertTrue(inspection.warnings.any { it.contains("当前消息不存在") })
            assertEquals(1, inspection.imagePaths.size)
            var session: ImportedSession? = null
            var imageCount = 0
            ArchiveZipImport.copy(
                source,
                inspection,
                { session = it },
                { fileId, data ->
                    imageCount += 1
                    assertEquals("uuid1", fileId)
                    assertEquals(4, data.size)
                },
            )
            assertEquals(1, imageCount)
            assertNotNull(session)
            val imported = session!!
            assertEquals(5, imported.messages.size)
            assertEquals(listOf(1L, 2L, 3L, 4L, 5L), imported.messages.map { it.message.id })
            imported.messages.forEach { message ->
                val parent = message.message.parentId
                if (parent != null) {
                    assertTrue("parent_id 必须小于 id", parent < message.message.id)
                }
            }
            val byId = imported.messages.associateBy { it.message.id }
            val path = ArrayList<Long>()
            var cursor = imported.currentMessageId
            while (cursor != null) {
                path.add(cursor)
                cursor = byId[cursor]?.message?.parentId
            }
            path.reverse()
            assertEquals(listOf(1L, 2L, 3L, 4L), path)
            val roles = path.map { id -> byId.getValue(id).message.role }
            assertEquals(
                listOf(
                    com.opends.chat.core.Role.USER,
                    com.opends.chat.core.Role.ASSISTANT,
                    com.opends.chat.core.Role.USER,
                    com.opends.chat.core.Role.ASSISTANT,
                ),
                roles,
            )
        }
    }

    private fun expectedIndexCounts(zip: File): Pair<Int, Int> {
        val bytes = zip.readBytes()
        java.util.zip.ZipInputStream(bytes.inputStream()).use { input ->
            while (true) {
                val entry = input.nextEntry ?: break
                if (entry.name == "chat_session_index.json") {
                    val text = input.readBytes().toString(Charsets.UTF_8)
                    val array = JsonParser.parseString(text).asJsonArray
                    return array.sumOf { element ->
                        element.asJsonObject.get("message_count")?.asInt ?: 0
                    } to array.size()
                }
            }
        }
        return 0 to 0
    }

    private fun userMessage(
        id: Long,
        parentId: Long?,
        text: String,
        imageExportPath: String? = null,
    ): JsonObject = JsonObject().apply {
        addProperty("id", id)
        if (parentId == null) add("parent_id", com.google.gson.JsonNull.INSTANCE) else addProperty("parent_id", parentId)
        addProperty("role", "USER")
        addProperty("status", "FINISHED")
        addProperty("thinking_enabled", false)
        addProperty("search_enabled", false)
        add("fragments", com.google.gson.JsonArray().apply {
            if (imageExportPath != null) {
                add(JsonObject().apply {
                    addProperty("type", "FILE")
                    add("files", com.google.gson.JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("id", "file-uuid1")
                            addProperty("file_name", "img.png")
                            addProperty("file_size", 4)
                            addProperty("is_image", true)
                            addProperty("export_path", imageExportPath)
                        })
                    })
                })
            }
            add(JsonObject().apply {
                addProperty("type", "REQUEST")
                addProperty("content", text)
            })
        })
    }

    private fun assistantMessage(id: Long, parentId: Long, text: String): JsonObject = JsonObject().apply {
        addProperty("id", id)
        addProperty("parent_id", parentId)
        addProperty("role", "ASSISTANT")
        addProperty("status", "FINISHED")
        addProperty("thinking_enabled", false)
        addProperty("search_enabled", false)
        add("fragments", com.google.gson.JsonArray().apply {
            add(JsonObject().apply {
                addProperty("type", "RESPONSE")
                addProperty("content", text)
            })
        })
    }

    private fun buildZip(entries: List<Pair<String, ByteArray>>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, data) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(data)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    companion object {
        private const val SESSION_ID = "8e0e749a-c9b5-4e5f-8ad1-d09c649e1d1f"

        private const val DESCRIPTOR_FIXTURE_BASE64 =
            "UEsDBBQACAAIAAAAAAAAAAAAAAAAAAAAAAANAAAAbWFuaWZlc3QuanNvbqtWSq0oyC8qiU/LL8pN" +
            "LIkvSy0qzszPU7IyrgUAUEsHCB/IYiMdAAAAGwAAAFBLAwQUAAgACAAAAAAAAAAAAAAAAAAAAAAA" +
            "FAAAAGNoYXRfc2Vzc2lvbnMveC5qc29ujcxBCkIxDATQu8y6StsPH8w5XClSio1SqOHTpCCId7eC" +
            "BxBmNzPvhVpAeMLBqjUGyWjNYasiPJtbbsoO19E7i6UHq+Y7p+/JO1RR7sYlZZtI9HHdhTBz9JGW" +
            "haLfh8N6mvbYSv5n9/MVdL68P1BLBwhXDulhbQAAAJwAAABQSwMEFAAIAAAAAAAAAAAAAAAAAAAA" +
            "AAAAACMAAABjaGF0X3Nlc3Npb25zL3hfZmlsZXMvYWJjX2ltYWdlLnBuZ4lQTkcNChoKRkFLRURB" +
            "VEFQSwcI4OL6MhAAAAAQAAAAUEsDBBQACAAIAAAAAAAAAAAAAAAAAAAAAAAXAAAAY2hhdF9zZXNz" +
            "aW9uX2luZGV4Lmpzb26FjFsKgzAQRfdyv6PkAYKzjn5ZRIIZSyCdhiaBQnHvZgfC/Tmcw33+EQMI" +
            "PyjUWBODpKWkkKMId3P4VFjhzaX4F2/7p0kFaYUohb+Vw+Y7w2o7Dcb0PbQl58jq0czT0m9bDv6+" +
            "O9cLUEsHCLu43ZFnAAAAiwAAAFBLAQIeAxQACAAIAAAAAAAfyGIjHQAAABsAAAANAAAAAAAAAAAA" +
            "IAAAAAAAAABtYW5pZmVzdC5qc29uUEsBAh4DFAAIAAgAAAAAAFcO6WFtAAAAnAAAABQAAAAAAAAA" +
            "AAAgAAAAWAAAAGNoYXRfc2Vzc2lvbnMveC5qc29uUEsBAh4DFAAIAAAAAAAAAODi+jIQAAAAEAAA" +
            "ACMAAAAAAAAAAAAgAAAABwEAAGNoYXRfc2Vzc2lvbnMveF9maWxlcy9hYmNfaW1hZ2UucG5nUEsB" +
            "Ah4DFAAIAAgAAAAAALu43ZFnAAAAiwAAABcAAAAAAAAAAAAgAAAAaAEAAGNoYXRfc2Vzc2lvbl9p" +
            "bmRleC5qc29uUEsFBgAAAAAEAAQAEwEAABQCAAAAAA=="
    }
}
