package com.kutugondrong.networkkg.annotation

import com.kutugondrong.networkkg.NetworkKG
import com.kutugondrong.networkkg.collection.NetworkType

/**
 * KG KutuGondrong
 * Make a GET or Post request and set value of path url
 * Type of network must initiate as NetworkType
 * @see NetworkType
 *
 *
 * @see NetworkKG
 */

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Network(
    val type: NetworkType = NetworkType.NONE,
    val path: String = "",
)