package tech.reinders.kotlin.databasemigrator.runner

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException
import tech.reinders.kotlin.databasemigrator.DbMigratorConfiguration
import tech.reinders.kotlin.databasemigrator.util.SqlUtil
import java.io.File
import java.sql.Connection
import java.sql.SQLException


class ScriptRunner(private val aFile: File,
                   private val aConnection: Connection,
                   private val aConfig: DbMigratorConfiguration) {
    fun execute() {
        if(aFile.isDirectory) {
            logger.info("'${aFile} is a directory, skip it")
            return
        }

        val sql = SqlUtil.readScriptFile(aFile)

        if(aConfig.enableSqlLogging) {
            logger.info("Executing script with sql '${sql}', from file ${aFile}")
        }

        //Make sure that autocommit is off
        aConnection.autoCommit = false

        try {
            //Remove Carriage Return
            val noCrSql = sql.replace("\r\n", "\n")
            if(! sql.isBlank()) {
                executeStatement(noCrSql, aConnection)
            } else {
                logger.info("'${aFile}' is blank, nothing to do")
            }
        } catch (dbmex : DatabaseMigratorException) {
            logger.error(dbmex.getOriginalMessage(), dbmex)
            throw dbmex
        }
    }

    @Throws(SQLException::class)
    private fun executeStatement(aCommand: String,
                                 aConnection: Connection) {
        val stmt = aConnection.createStatement()

        try {
            val hasResult = stmt.execute(aCommand)

            if(! hasResult) {
                logger.info("Script has no result")
            } else {
                logger.info("update count: ${stmt.updateCount}")
            }
        } catch (e: SQLException) {
            throw DatabaseMigratorException(TAG, e.message, e)
        } finally {
            try {
                stmt.close()
            } catch (ex: SQLException) {
                logger.error("Error while closing the statement, nothing to worry about")
            }
        }
    }

    companion object {
        private const val TAG = "ScriptRunner"
        private val logger = LoggerFactory.getLogger(ScriptRunner::class.java)
    }
}