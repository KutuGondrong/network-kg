package com.kutugondrong.networkkg.callback

import com.kutugondrong.networkkg.NetworkKG

/**
 * KG KutuGondrong
 * Callback is used if we want to present status from response
 *
 *
 * @see NetworkKG
 */
class CallbackKG<T> {
    var success = false
    var data: T? = null
    var url: String? = null
    var responseCode: Int? = null
    var responseBody: String? = null
    var responseBodyError: String? = null
    var settingRequestKG: String? = null
    var message: String? = null
}
