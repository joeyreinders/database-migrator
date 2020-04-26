package tech.reinders.kotlin.databasemigrator.util

import org.slf4j.LoggerFactory
import java.io.File

/**
 * SQL Utility
 */
object SqlUtil {
    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(SqlUtil::class.java)

    /**
     * Check if a line is a comment
     */
    fun lineIsComment(aLine: String?) : Boolean {
        if(aLine.isNullOrEmpty()) {
            logger.trace("'${aLine}' is empty or null")
            return false
        }

        val trimmed = aLine.trim()
        if(aLine.isBlank()) {
            logger.trace("'${aLine}' is blank")

            return false
        }

        return trimmed.startsWith("//") || trimmed.startsWith("--")
                || (trimmed.startsWith("/*") && trimmed.endsWith("*/"))
    }

    internal fun startLineComment(aLine: String? ) : Boolean {
        if(aLine.isNullOrEmpty()) {
            logger.trace("'${aLine}' is empty or null")
            return false
        }

        return aLine.trim().startsWith("/*")
    }

    internal fun endLineComment(aLine: String? ) : Boolean {
        if(aLine.isNullOrEmpty()) {
            logger.trace("'${aLine}' is empty or null")
            return false
        }

        return aLine.trim().endsWith("*/")
    }

    /**
     * Remove all useless content
     */
    fun readScriptFile(aFile: File) : String {
        val lines = aFile.readLines()

        val builder = StringBuilder()
        var isCommentBlock = false

        for (line in lines) {
            //Check if we are the end of a multiline comment
            if(endLineComment(line)) {
                isCommentBlock = false
                continue
            }

            //Check if we are at the beginning of a multiline comment
            if(startLineComment(line)) {
                isCommentBlock = true
                continue
            }

            //if in comment block, skip this line
            if(isCommentBlock) {
                continue
            }

            //Check if comment line
            if(lineIsComment(line)) {
                continue
            }

            builder.append(line)
        }

        return builder.toString()
    }
}