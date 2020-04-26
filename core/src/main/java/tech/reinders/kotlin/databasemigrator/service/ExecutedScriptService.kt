package tech.reinders.kotlin.databasemigrator.service

import java.util.*

interface ExecutedScriptService {
    /**
     * Check if a script has already been executed
     * @param aScriptName the name of the script that has to be checked
     * @return true if already executed
     */
    fun hasRun(aScriptName: String) : Boolean

    /**
     * Insert the script in the table
     * @param aFileName The name of the script that has been executed
     * @param aStartTime The script execution start time
     * @param aEndTime The script execution end time
     */
    fun insert(aFileName: String, aStartTime: Date, aEndTime: Date)

    /**
     * Create table if needed
     */
    fun createTable()
}