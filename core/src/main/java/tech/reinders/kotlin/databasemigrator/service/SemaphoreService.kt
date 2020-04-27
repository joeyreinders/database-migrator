package tech.reinders.kotlin.databasemigrator.service

/**
 * Semaphore Service.
 * Checks if a process is already running the database upgrade or not
 */
interface SemaphoreService {
    /**
     * Indicator to see if a process is already running
     * @return true if locked
     */
    fun locked() : Boolean

    /**
     * Lock the process
     * @return true if lock acquired
     */
    fun lock() : Boolean

    /**
     * Create table if needed
     */
    fun createTable()

    /**
     * Release.
     * Can only be released if this host is also the
     */
    fun release()

    /**
     * Release lock, no matter the owner.
     * Use this at your own risk
     */
    fun forceRelease()
}