package tech.reinders.kotlin.databasemigrator.service

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException
import tech.reinders.kotlin.databasemigrator.util.JdbcUtil
import tech.reinders.kotlin.databasemigrator.util.MachineUtil
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp

class DefaultSemaphoreService(private val aConnection: Connection) : SemaphoreService {
    override fun locked(): Boolean {
        val stmt = aConnection.createStatement();
        try {
            val result = stmt.executeQuery(QRY_LOCKED)
            var locked = false
            while (result.next()) {
                locked = true

                val host = result.getString("host")
                val acquired = result.getTimestamp("acquired")
                val pid = result.getString("pid")

                log.info("Database migrator is locked by host '?', since '?' with pid '?'", host, acquired, pid)
            }

            return locked
        } finally {
            JdbcUtil.close(stmt)
        }
    }

    override fun lock() : Boolean {
        if(locked()) {
            log.error("Could not acquire lock, some process is already busy")
            return false
        }

        val stmt = aConnection.prepareStatement(QRY_INSERT)
        try {
            stmt.setInt(1, 1)
            stmt.setString(2, MachineUtil.hostName().orEmpty())
            stmt.setTimestamp(3, Timestamp(System.currentTimeMillis()))
            stmt.setString(4, MachineUtil.pid())

            stmt.execute()
            aConnection.commit()

            return true
        } catch (ex: SQLException) {
            aConnection.rollback()
            log.error("Error while acquiring lock", ex)
            throw DatabaseMigratorException(TAG, "Error while acquiring lock, reason = ${ex.message}", ex)
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

    override fun release() {
        val stmt = aConnection.createStatement();
        try {
            val result = stmt.executeQuery(QRY_LOCKED)

            if(result.next()) {
                val host = result.getString("host")
                val pid = result.getString("pid")

                when {
                    result.next() -> {
                        log.error("Multiple locks exist on this database, this is not normal")
                    }
                    host != MachineUtil.hostName() -> {
                        log.error("Hostname '$host' does not correspond with this machine, cannot release lock")
                    }
                    pid != MachineUtil.pid() -> {
                        log.error("PID '$pid' does not correspond with the PID of this application, cannot release lock")
                    }
                    else -> {
                        stmt.execute(QRY_DELETE)
                        aConnection.commit()
                    }
                }
            } else {
                log.info("Nothing to unlock")
            }
        } catch (ex: SQLException) {
            aConnection.rollback()
            log.error("Error while releasing lock for semaphore")
            throw DatabaseMigratorException(TAG, "Error releasing lock => ${ex.message}", ex)
        } catch (ex: DatabaseMigratorException) {
            log.error(ex.message, ex)
            throw ex
        } finally {
            stmt.close()
        }
    }

    override fun forceRelease() {
        val stmt = aConnection.createStatement();
        try {
            val result = stmt.executeQuery(QRY_LOCKED)
            var hasToRelease = false;

            while(result.next()) {
                val host = result.getString("host")
                val pid = result.getString("pid")

                log.info("Release lock of host '$host' with pid '$pid'")
                hasToRelease = true;
            }

            if(hasToRelease) {
                stmt.execute(QRY_DELETE)
                aConnection.commit()
            } else {
                log.info("Nothing to unlock")
            }
        } catch (ex: SQLException) {
            aConnection.rollback()
            log.error("Error while releasing lock for semaphore")
            throw DatabaseMigratorException(TAG, "Error releasing lock => ${ex.message}", ex)
        } catch (ex: DatabaseMigratorException) {
            log.error(ex.message, ex)
            throw ex
        } finally {
            stmt.close()
        }
    }

    companion object {
        const val TABLE = "t_dbmig_semaphore"
        const val DDL = "CREATE TABLE $TABLE (locked BIT NOT NULL DEFAULT 0, host VARCHAR(200), acquired TIMESTAMP, pid VARCHAR(10))"
        const val QRY_LOCKED = "SELECT * FROM $TABLE WHERE locked = 1"
        const val QRY_INSERT = "INSERT INTO $TABLE (locked, host, acquired, pid) VALUES (?, ?, ?, ?)"
        const val QRY_DELETE = "DELETE FROM $TABLE"

        /**
         * Logger
         */
        private val log = LoggerFactory.getLogger(DefaultSemaphoreService::class.java)

        /**
         * TAG
         */
        const val TAG = "Semaphore"
    }
}