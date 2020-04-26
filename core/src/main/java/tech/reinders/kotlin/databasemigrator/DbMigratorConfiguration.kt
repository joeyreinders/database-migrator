package tech.reinders.kotlin.databasemigrator

/**
 * This class defines all the parameters for running the database migration
 */
interface DbMigratorConfiguration {
    /**
     * The resource folder where all the database files can be found.
     * Note these folders are executed alphabetically.
     */
    fun resourceFolder() : String = "/dbmigration"

    /**
     * Enable logging.
     * By default this is off, but can be usefull to understand what is going on
     */
    fun enableLogging() : Boolean = true

    /**
     * SQL Logging
     * Logs the (sql) content of the file that is being executed
     */
    fun enableSqlLogging() : Boolean = false

    /**
     * Transaction per script.
     * A transaction per script runs every script as a single transaction. This has the advantage that previously executed
     * scripts are not re-evaluated.
     */
    fun transactionPerScript() : Boolean = false
}