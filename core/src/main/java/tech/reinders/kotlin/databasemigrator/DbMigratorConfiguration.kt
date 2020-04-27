package tech.reinders.kotlin.databasemigrator

/**
 * This class defines all the parameters for running the database migration
 */
class DbMigratorConfiguration {
    /**
     * The resource folder where all the database files can be found.
     * Note these folders are executed alphabetically.
     */
    var resourceFolder : String = "/dbmigration"

    /**
     * SQL Logging
     * Logs the (sql) content of the file that is being executed
     */
    var enableSqlLogging : Boolean = false

    /**
     * transaction per script
     * If true, a transaction per script will be used, if false one transaction for the whole migration will be used
     */
    var transactionPerScript : Boolean = true
}