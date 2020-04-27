package tech.reinders.kotlin.databasemigrator.util

import java.lang.management.ManagementFactory
import java.net.InetAddress


/**
 * Machine related utilities
 */
object MachineUtil {
    /**
     * Get the hostname of this machine
     */
    fun hostName() = InetAddress.getLocalHost().hostName

    /**
     * Get pid
     */
    fun pid() : String {
        val bean = ManagementFactory.getRuntimeMXBean()
        val name = bean.name

        return name.split("@")[0]
    }
}