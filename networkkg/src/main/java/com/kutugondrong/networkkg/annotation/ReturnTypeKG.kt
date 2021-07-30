package com.kutugondrong.networkkg.annotation

import com.kutugondrong.networkkg.NetworkKG
import kotlin.reflect.KClass

/**
 * KG KutuGondrong
 * Adapter to handle response list
 *
 *
 * @see NetworkKG
 */

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ReturnTypeKG(
    val value: KClass<*>,
    val isList: Boolean = false
)