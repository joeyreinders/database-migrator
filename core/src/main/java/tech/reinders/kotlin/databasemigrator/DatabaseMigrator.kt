package tech.reinders.kotlin.databasemigrator

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.runner.ScriptRunner
import tech.reinders.kotlin.databasemigrator.service.DefaultExecutedScriptService
import tech.reinders.kotlin.databasemigrator.service.DefaultSemaphoreService
import tech.reinders.kotlin.databasemigrator.service.ExecutedScriptService
import tech.reinders.kotlin.databasemigrator.service.SemaphoreService
import tech.reinders.kotlin.databasemigrator.util.FileUtil
import tech.reinders.kotlin.databasemigrator.util.FileWrapper
import java.io.File
import java.lang.Exception
import java.sql.Connection
import java.util.*

class DatabaseMigrator(private val aConnection: Connection,
                       private val aConfiguration: DbMigratorConfiguration) : Runnable {
    private val semaphoreService : SemaphoreService
    private val executedScriptService : ExecutedScriptService

    init {
        if(this.aConnection.isClosed) {
            throw DatabaseMigratorException(TAG, "The provided connection is already closed")
        }

        if(this.aConnection.isReadOnly) {
            throw DatabaseMigratorException(TAG, "The provided connection is a readonly connection, cannot do anything with it")
        }

        this.aConnection.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        this.aConnection.autoCommit = false

        this.semaphoreService = DefaultSemaphoreService(this.aConnection)
        this.executedScriptService = DefaultExecutedScriptService(this.aConnection)
    }

    private fun createNecessaryTables() {
        this.semaphoreService.createTable()
        this.executedScriptService.createTable()
    }

    override fun run() {
        createNecessaryTables()

        val files = FileUtil.getFiles(this.aConfiguration.resourceFolder())
        if(files.isEmpty()) {
            log.info("No files found to handle, return")

            return
        }

       if(! this.semaphoreService.lock()) {
           log.error("Could not acquire lock, returning")
           return
       }

        try {
            for (file in files) {

            }
        } finally {
            this.semaphoreService.release()
        }
    }

    private fun handleFile(aFile: FileWrapper) : Boolean {
        try {
            val start = Date()
            ScriptRunner(aFile.file, aConnection, aConfiguration).execute()
            val stop = Date()

            this.executedScriptService.insert(aFile.relativeName, start, stop)

            return true
        } catch (ex : DatabaseMigratorException) {
            log.error("Error within script $aFile", ex)
            throw ex
        } catch (ex: Exception) {
            log.error("Error within script $aFile", ex)
            throw DatabaseMigratorException(TAG, ex.message, ex)
        }

        return false
    }

    companion object {
        /**
         * TAG
         */
        private const val TAG = "DatabaseMigrator"

        /**
         * Logger
         */
        private val log = LoggerFactory.getLogger(DatabaseMigrator::class.java)
    }
}