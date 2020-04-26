package tech.reinders.kotlin.databasemigrator

/**
 * Custom database migration class
 */
class DatabaseMigratorException : RuntimeException {
    /**
     * A code tag for the exception
     */
    private val tag: String

    /**
     * Default constructor
     * @param aTag
     */
    constructor(aTag: String) {
        tag = aTag
    }

    constructor(aTag: String, message: String?) : super(message) {
        tag = aTag
    }

    constructor(aTag: String, message: String?, cause: Throwable?) : super(message, cause) {
        tag = aTag
    }

    constructor(aTag: String, cause: Throwable?) : super(cause) {
        tag = aTag
    }

    override fun toString(): String {
        return "${prefix()} ${super.toString()}"
    }

    override val message: String
        get() = "${prefix()} ${super.message}"

    fun getOriginalMessage() = super.message

    private fun prefix() = "$tag -"
}