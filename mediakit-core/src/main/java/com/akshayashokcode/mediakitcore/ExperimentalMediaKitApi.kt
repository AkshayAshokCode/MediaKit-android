package com.akshayashokcode.mediakitcore

/**
 * Marks a MediaKit API as experimental.
 *
 * Opt in with `@OptIn(ExperimentalMediaKitApi::class)` or propagate the
 * requirement to your own API with `@ExperimentalMediaKitApi`.
 *
 * APIs marked experimental may change signature or behaviour in future releases
 * without a deprecation cycle.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This MediaKit API is experimental and subject to change."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalMediaKitApi
