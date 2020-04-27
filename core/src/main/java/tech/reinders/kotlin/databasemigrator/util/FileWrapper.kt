package tech.reinders.kotlin.databasemigrator.util

import java.io.File

/**
 * A file wrapper class.
 * We will be sorting on filename and this makes some information just that bit handier to access
 */
class FileWrapper(parentDir : File,
                  val file: File) : Comparable<FileWrapper> {
    /**
     * Indicator to see if the file is a directory or not
     */
    val isDirectory : Boolean = file.isDirectory

    /**
     * The name of the file.
     */
    val name : String = file.name

    /**
     * Checks if the file is an sql file or not.
     * We do this by checking the suffix, not the most glamorous way, but if you have any other ideas, please be my guest
     */
    val isSqlFile : Boolean = name.endsWith(".sql") || name.endsWith(".SQL")

    /**
     * The name relative to the resource folder
     */
    val relativeName = getRelativeName(parentDir)

    /**
     * To sort on name
     * @param the other object to sort
     * @return you should know how this works :-)
     */
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

    /**
     * Tostring method, delegates to file
     */
    override fun toString(): String {
        return file.toString()
    }
}