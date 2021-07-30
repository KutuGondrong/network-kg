package com.kutugondrong.networkkg.annotation

import com.kutugondrong.networkkg.NetworkKG

/**
 * KG KutuGondrong
 * Header parameter appended to the URL.
 *
 *
 * @see NetworkKG
 */

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Header(
    val value: String,
)