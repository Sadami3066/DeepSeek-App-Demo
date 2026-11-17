package com.opends.chat.core

import java.io.File

internal object TestPaths {
    fun repoFile(relative: String): File {
        var directory: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (directory != null) {
            if (File(directory, "refs").isDirectory) return File(directory, relative)
            directory = directory.parentFile
        }
        return File(relative)
    }
}
