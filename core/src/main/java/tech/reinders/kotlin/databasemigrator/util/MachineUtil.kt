package tech.reinders.kotlin.databasemigrator.util

import java.net.InetAddress

/**
 * Machine related utilities
 */
object MachineUtil {
    /**
     * Get the hostname of this machine
     */
    fun hostName() = InetAddress.getLocalHost().getHostName()

    /**
     * Get pid
     */
    fun pid() = ""
}