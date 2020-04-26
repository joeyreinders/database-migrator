package tech.reinders.kotlin.databasemigrator.service

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException
import tech.reinders.kotlin.databasemigrator.util.JdbcUtil
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*

internal class DefaultExecutedScriptService(private val aConnection: Connection) : ExecutedScriptService {
    override fun hasRun(aScriptName: String): Boolean {
        val stmt = aConnection.prepareStatement(QRY_SELECT)

        try {
            stmt.setString(1, aScriptName)
            stmt.execute()

            val rs = stmt.resultSet
            return rs != null && rs.next()
        } catch (t: Throwable) {
            aConnection.rollback()
            throw t
        } finally {
            JdbcUtil.close(stmt)
        }
    }

    override fun insert(aFileName: String, aStartTime: Date, aEndTime: Date) {
        val stmt = aConnection.prepareStatement(QRY_INSERT)

        try {
            stmt.setString(1, aFileName)
            stmt.setTimestamp(2, Timestamp(aStartTime.time))
            stmt.setTimestamp(3, Timestamp(aEndTime.time))

            stmt.execute()
            aConnection.commit()
        } catch (t: Throwable) {
            aConnection.rollback()
            throw t
        } finally {
            JdbcUtil.close(stmt)
        }
    }

    override fun createTable() {
        if(! JdbcUtil.tableExists(TABLE, aConnection)) {
            log.info("Table $TABLE does not exist, try creating it")
            try {
                JdbcUtil.createTable(DDL, aConnection)
            } catch (ex: SQLException) {
                aConnection.rollback()
                log.error("Error while creating $TABLE", ex)
                throw DatabaseMigratorException(TAG, "Error while creating table $TABLE, reason = ${ex.message}", ex)
            }
        }
    }

    companion object {
        /**
         * Log
         */
        private val log = LoggerFactory.getLogger(DefaultExecutedScriptService::class.java)

        /**
         * TAG
         */
        private const val TAG = "ExecutedScript"

        private const val TABLE = "t_dbmig_executed_scripts"
        private const val DDL = "CREATE TABLE $TABLE (filename VARCHAR(250), starttime TIMESTAMP, endtime TIMESTAMP)"
        private const val QRY_INSERT = "INSERT INTO $TABLE (filename, starttime, endtime) VALUES (? , ? , ?)"
        private const val QRY_SELECT = "SELECT * FROM $TABLE WHERE filename = ?"
    }
}