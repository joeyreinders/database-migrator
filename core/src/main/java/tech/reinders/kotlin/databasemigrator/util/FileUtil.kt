package tech.reinders.kotlin.databasemigrator.util

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException
import java.io.File
import java.nio.file.Files

/**
 * File utility class
 */
object FileUtil {
    /**
     * TAG
     */
    private const val TAG = "FileUtil";

    fun checkDirectory(aLocation: String) {
        val folder = getResourceDir(aLocation)

        if(! folder.exists()) {
            throw DatabaseMigratorException(TAG, "No file found for location : '${folder}' ")
        }

        if(! folder.isDirectory) {
            throw DatabaseMigratorException(TAG, "'${folder}' is a file not a folder as expected")
        }

        if(! Files.isReadable(folder.toPath())) {
            throw DatabaseMigratorException(TAG, "'${folder}' is not readable")
        }
    }

    fun getResourceDir(aLocation: String) : File {
        if(aLocation.isEmpty()) {
            throw DatabaseMigratorException(TAG, "No resource location is defined, this is a required parameter")
        }

        val resourceUrl = FileUtil.javaClass.getResource(aLocation)
                ?: throw DatabaseMigratorException(TAG, "No resource found at location: '${aLocation}'")

        return File(resourceUrl.toURI())
    }

    //TODO handle subdirectories
    fun getFiles(aLocation: String) : Array<FileWrapper> {
        val folder = getResourceDir(aLocation)
        val files = folder.listFiles()
        if(files == null || files.isEmpty()) {
            return emptyArray()
        }

        return files.map { t -> FileWrapper(folder, t) }.sorted().filter { t -> t.isSqlFile }.toTypedArray()
    }
}