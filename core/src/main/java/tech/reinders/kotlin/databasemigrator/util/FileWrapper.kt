package tech.reinders.kotlin.databasemigrator.util

import java.io.File

/**
 * A file wrapper class.
 * We will be sorting on filename and getting the name of the file is a rather costly affair
 */
class FileWrapper(parentDir : File,
                  val file: File) : Comparable<FileWrapper> {
    val isDirectory : Boolean = file.isDirectory
    val name : String = file.name
    val isSqlFile : Boolean = name.endsWith(".sql") || name.endsWith(".SQL")

    val relativeName = getRelativeName(parentDir)

    override fun compareTo(other: FileWrapper): Int {
        return this.name.compareTo(other.name, true)
    }

    private fun getRelativeName(parentDir: File) : String{
        val f = file.toString().replaceFirst(parentDir.toString(), "")
        return if(f.startsWith("/")) {
            f.substring(1)
        } else {
            f
        }
    }
}