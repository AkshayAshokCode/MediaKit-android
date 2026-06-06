package com.akshayashokcode.mediakitcore.exception

/**
 * Base class for all MediaKit module exceptions.
 *
 * Each module's own sealed exception hierarchy extends this class, allowing
 * callers to catch any MediaKit error with a single `catch (e: MediaKitException)`.
 */
abstract class MediaKitException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
