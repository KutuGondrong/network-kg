package com.kutugondrong.networkkg.annotation

import com.kutugondrong.networkkg.NetworkKG

/**
 * KG KutuGondrong
 * Path parameter appended to the URL.
 * Special case if Network path want to customize
 * @Network(NetworkType.GET, "/check/{tests}")
 * suspend fun getPhotos(@Path("tests") test: Int)
 *
 * @see NetworkKG
 */

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Path(
    val value: String,
)