package com.kutugondrong.networkkg.annotation

import com.kutugondrong.networkkg.NetworkKG

/**
 * KG KutuGondrong
 * Body parameter should custom object
 *
 *
 * @see NetworkKG
 */

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Body