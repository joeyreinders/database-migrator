package tech.reinders.kotlin.databasemigrator.util

import org.slf4j.LoggerFactory
import tech.reinders.kotlin.databasemigrator.DatabaseMigratorException
import tech.reinders.kotlin.databasemigrator.service.DefaultSemaphoreService
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


/**
 * JDBC Utility
 */
object JdbcUtil {
    /**
     * TAG
     */
    const val TAG = "JdbcUtil - "

    /**
     * Logger
     */
    private val log = LoggerFactory.getLogger(JdbcUtil::class.java)

    /**
     * Check if a table exists on the database
     * @param aTableName The tablename to find
     * @param aConnection The connection to access the database
     * @return true if the table exists, else return false
     */
    fun tableExists(aTableName: String,
                    aConnection: Connection) : Boolean {
        val metadata = aConnection.metaData
        //TODO figure out how to use a naming pattern that ignores casing
        val res: ResultSet = metadata.getTables(null, null, "%", arrayOf("TABLE"))
        var result = false;
        try {
            while(res.next()) {
                val name = res.getString("TABLE_NAME")
                if(name.equals(aTableName, true)) {
                    result = true
                    break
                }
            }


        } finally {
            res.close()
            return result
        }
    }

    /**
     * Closes an autocloseable
     */
    fun close(aAutoCloseable: AutoCloseable) {
        try {
            aAutoCloseable.close()
        } catch (ex: Exception) {
            log.error("Error while closing $aAutoCloseable", ex)
        }
    }

    fun createTable(aDdl: String,
                    aConnection: Connection) {
        val stmt = aConnection.createStatement()
        try {
            stmt.execute(aDdl)
            aConnection.commit()
        } finally {
            close(stmt)
        }
    }
}