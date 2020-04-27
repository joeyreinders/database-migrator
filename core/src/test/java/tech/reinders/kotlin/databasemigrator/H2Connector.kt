package tech.reinders.kotlin.databasemigrator

import org.junit.jupiter.api.Assertions
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.function.Consumer

class H2Connector {
    private val connections: MutableList<Connection> = ArrayList()
    val connection: Connection
        get() = try {
            val connection = DriverManager.getConnection(URL, USR, PWD)
            connections.add(connection)
            connection
        } catch (e: SQLException) {
            Assertions.fail<Any>(e)
            throw RuntimeException(e)
        }

    fun close() {
        try {
            connection.createStatement().execute("DROP ALL OBJECTS")
        } catch (ex: Exception) {
            throw RuntimeException("Error while dropping all objects", ex)
        }
        connections.forEach(Consumer { obj: Connection -> obj.close() })
        File(FILE).delete()
    }

    companion object {
        private const val FILE = "/tmp/test.tmp"
        private const val URL = "jdbc:h2:$FILE"
        private const val USR = "sa"
        private const val PWD = ""
    }
}