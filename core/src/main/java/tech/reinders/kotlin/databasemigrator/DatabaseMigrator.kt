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
import java.sql.Savepoint
import java.util.*

class DatabaseMigrator(private val aConnection: Connection,
                       private val aConfiguration: DbMigratorConfiguration) : Runnable {
    private val semaphoreService: SemaphoreService
    private val executedScriptService: ExecutedScriptService

    init {
        if (this.aConnection.isClosed) {
            throw DatabaseMigratorException(TAG, "The provided connection is already closed")
        }

        if (this.aConnection.isReadOnly) {
            throw DatabaseMigratorException(TAG, "The provided connection is a readonly connection, cannot do anything with it")
        }

        FileUtil.checkDirectory(aConfiguration.resourceFolder)

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

        val files = FileUtil.getFiles(this.aConfiguration.resourceFolder)
        if (files.isEmpty()) {
            log.info("No files found to handle, return")

            return
        }

        if (!this.semaphoreService.lock()) {
            log.error("Could not acquire lock, returning")
            return
        }

        val savepoint = if(! aConfiguration.transactionPerScript) {
            aConnection.setSavepoint("DatabaseMigrator")
        } else {
            null
        }

        try {
            var doneExecutions = false
            for (file in files) {
                if (executedScriptService.hasRun(file.relativeName)) {
                    log.info("Not running file $file as it has been executed in a previous run")
                } else {
                    handleFile(file)
                    doneExecutions = true
                }
            }

            if (doneExecutions && !aConfiguration.transactionPerScript) {
                this.aConnection.commit()
            }
        } catch (ex: Exception) {
            //Rethrow logging has already been done
            if(savepoint != null) {
                this.aConnection.rollback(savepoint)
            } else {
                this.aConnection.rollback()
            }
            throw ex
        } finally {
            this.semaphoreService.release()
        }
    }

    private fun handleFile(aFile: FileWrapper): Boolean {
        try {
            log.debug("Handling file $aFile")
            val start = Date()
            ScriptRunner(aFile.file, aConnection, aConfiguration).execute()
            val stop = Date()

            this.executedScriptService.insert(aFile.relativeName, start, stop)

            log.info("Handled file $aFile, result OK")

            if (aConfiguration.transactionPerScript) {
                aConnection.commit()
            }
            return true
        } catch (ex: DatabaseMigratorException) {
            log.error("Handled file $aFile, result NOK")
            log.error("Error within script $aFile", ex)
            throw ex
        } catch (ex: Exception) {
            log.error("Handled file $aFile, result NOK")
            log.error("Error within script $aFile", ex)
            throw DatabaseMigratorException(TAG, ex.message, ex)
        }
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