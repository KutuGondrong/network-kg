package com.kutugondrong.networkkg

import android.util.Log
import com.kutugondrong.httpclientkg.HttpClientKG
import com.kutugondrong.httpclientkg.PropertyKG
import com.kutugondrong.httpclientkg.collection.RequestMethodTypeKG
import com.kutugondrong.networkkg.annotation.*
import com.kutugondrong.networkkg.callback.CallbackKG
import com.kutugondrong.networkkg.collection.NetworkType
import com.kutugondrong.networkkg.exception.NetworkKGException
import com.kutugondrong.networkkgadapter.ConverterNetworkKGAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.lang.reflect.*
import java.net.URLEncoder
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

/**
 * KG KutuGondrong
 * This class is main class for module network
 *
 * <pre><code>
 * val service = NetworkKG.dslNetworkKG{
 *      httpClient = DefaultHttpClient.dslDefaultHttpClient {
 *          baseUrl = BuildConfig.SERVER_BASE_URL
 *          properties {
 *              property {
 *                  key = "Authorization"
 *                  value = "Client-ID ${BuildConfig.API_KEY}"
 *              }
 *          }
 *      }
 *      converterAdapter = JsonKGAdapter.create()
 *  }.createService<PhotoService>()
 *
 *
 * this module using DefaultHttpClient for network request
 * @see HttpClientKG
 * Make a GET or Post request and set value of path url
 * @see Network
 * Query parameter appended to the URL using use Header
 * @see Header
 * Body parameter should custom object
 * @see Body
 * Query parameter appended to the URL using use Query
 * @see Query
 * Handle return type if use CallbackKG or handle if return is a arraylist
 * @see ReturnTypeKG
 * If want to handle status response use Callback
 * @see CallbackKG
 * Example for initiate
 */
class NetworkKG private constructor(builder: Builder) {

    private var httpClient: HttpClientKG? = null
    private var converterAdapter = ArrayList<ConverterNetworkKGAdapter?>()

    /**
     * Create an implementation of the API endpoints defined by the interface.
     */
    inline fun <reified T : Any> createService(): T {
        val service = T::class.java
        validateServiceInterface(service)
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf(service),
            invocationHandler
        ) as T
    }

    fun <T> validateServiceInterface(service: Class<T>) {
        if (!service.isInterface) {
            throw NetworkKGException("API declarations must be interfaces.")
        }
        if (httpClient == null) {
            throw NetworkKGException("Need to set DefaultHttpClient")
        }
        if (converterAdapter.size == 0 && converterAdapter[0] == null) {
            throw NetworkKGException("Need to set converterAdapter for default using " +
                    "com.kutugondrong.networkkg.jsonkgadapter.JsonKGAdapter")
        }
    }

    val invocationHandler = suspendInvocationHandlerReplace(
        { pathAndQuery, proxy, method, args ->
            converterAdapter[0]?.type?.also {
                if (it > 0) {
                    throw NetworkKGException("if using " +
                            "com.kutugondrong.networkkg.jsonkgadapter.JsonKGAdapter, " +
                            "com.kutugondrong.networkkg.gsonadapter.GsonKGAdapter, " +
                            "function must be suspend")
                }
            }
            null
        },
        { pathAndQuery, proxy, method, args ->
            try {
                withContext(Dispatchers.IO) {
                    var result: Any? = null
                    converterAdapter[0]?.type?.also {
                        val cloneClass = method.kotlinFunction?.returnType?.jvmErasure as KClass<*>
                        if (cloneClass.javaPrimitiveType == null && cloneClass != Unit::class && it > 0) {
                            result = executeServiceMethod(pathAndQuery, method, cloneClass)
                        }
                    }
                    result
                }
            } catch (e: NetworkKGException) {
                throw NetworkKGException("${e.message}")
            } catch (e: Exception) {
                if (isDebug) {
                    Log.e(TAG, e.toString())
                }
                null
            }
        }
    )

    @Suppress("NAME_SHADOWING", "UNCHECKED_CAST", "UNUSED_PARAMETER")
    fun suspendInvocationHandlerReplace(
        blockNoSuspend: (pathAndQuery: String, proxy: Any, method: Method, args: Array<*>?) -> Any?,
        blockSuspend: suspend (pathAndQuery: String, proxy: Any, method: Method, args: Array<*>?) -> Any?,
    ) =
        InvocationHandler { proxy, method, args ->
            val network = method.getAnnotation(Network::class.java)
                ?: throw NetworkKGException("Check your service method ${method.name} " +
                        "you need implement com.kutugondrong.networkkg.annotation.Network")
            val pathAndQuery = getPathAndQuery(args, method, network.path)
            val cont = args?.lastOrNull() as? Continuation<*>
            if (cont == null) {
                blockNoSuspend(pathAndQuery, proxy, method, args)
            } else {
                val args = args.dropLast(1).toTypedArray()
                val suspendInvoker =
                    blockSuspend as (String, Any, Method, Array<*>?, Continuation<*>) -> Any?
                suspendInvoker(pathAndQuery, proxy, method, args, cont)
            }
        }


    private suspend fun executeServiceMethod(
        pathAndQuery: String,
        method: Method,
        cloneClass: KClass<*>,
    ): Any? {
        val network = method.getAnnotation(Network::class.java)
        var requestMethod: RequestMethodTypeKG? = null
        network?.type?.also {
            requestMethod = getRequestMethod(it, method.name)
        }
        val response = httpClient?.execute(HttpClientKG.dslSettingBuilder {
            this.requestMethod = requestMethod
            this.pathAndQuery = pathAndQuery
            this.properties.addAll(customProperties)
            this.jsonBody = jsonBodyValue
        }).also {
            customProperties = ArrayList()
            jsonBodyValue = null
        }
        var responseFromHttpClient: Any? = null
        if (response?.success == true) {
            var isList = false
            val classOf = method.getAnnotation(ReturnTypeKG::class.java)?.let {
                isList = it.isList
                it.value
            } ?: cloneClass

            responseFromHttpClient = response.responseBody?.let {
                converterAdapter[0]?.fromJson(it, classOf, isList)
            }
        }
        if (cloneClass.isSubclassOf(CallbackKG::class)) {
            responseFromHttpClient = response?.let {
                val result = CallbackKG<Any>()
                result.success = it.success
                result.url = it.url
                result.responseCode = it.responseCode
                result.responseBody = it.responseBody
                result.responseBodyError = it.responseBodyError
                result.settingRequestKG = it.settingRequestKG.toString()
                result.message = it.message
                result.data = responseFromHttpClient
                if (isDebug) {
                    Log.wtf(TAG, it.toString())
                }
                result
            }
        }
        return responseFromHttpClient
    }

    private fun getRequestMethod(type: NetworkType, methodName: String): RequestMethodTypeKG {
        return when (type) {
            NetworkType.GET -> {
                RequestMethodTypeKG.GET
            }
            NetworkType.POST -> {
                RequestMethodTypeKG.POST
            }
            NetworkType.PUT -> {
                RequestMethodTypeKG.PUT
            }
            NetworkType.DELETE -> {
                RequestMethodTypeKG.DELETE
            }
            else -> {
                throw NetworkKGException("Check your service method $methodName \n" +
                        "you need implement com.kutugondrong.networkkg.collection.NetworkType GET or POST")
            }
        }
    }

    private fun getPathAndQuery(args: Array<*>?, method: Method, path: String): String {
        var pathAndQuery = path
        val parameterType = method.parameterTypes
        val parameterAnnotation = method.parameterAnnotations
        args?.size?.also { it ->
            val params = LinkedHashMap<String, String>()
            for (i in 0 until it) {
                var parameterAnnotationCopy: Any? = null
                if (parameterAnnotation[i]?.isNotEmpty() == true) {
                    parameterAnnotationCopy = parameterAnnotation[i][0]
                }
                when {
                    parameterAnnotationCopy is Query -> {
                        params[parameterAnnotationCopy.value] = args[i].toString()
                    }
                    parameterAnnotationCopy is Path -> {
                        pathAndQuery = pathAndQuery.replace(
                            "{${parameterAnnotationCopy.value}}",
                            args[i].toString(), false)
                    }
                    parameterAnnotationCopy is Body -> {
                        args[i]?.also { value ->
                            jsonBodyValue = converterAdapter[0]?.toJson(value)
                        }
                    }
                    parameterAnnotationCopy is Header -> {
                        customProperties.add(
                            PropertyKG(
                                parameterAnnotationCopy.value,
                                args[i].toString()
                            )
                        )
                    }
                    parameterType[i] != null -> {
                        var name: Any? = null
                        if (parameterType[i].fields.isNotEmpty()) {
                            name = parameterType[i].fields[0].type
                        }
                        if (name != null) {
                            throw NetworkKGException("Check your service method ${method.name} " +
                                    "you need implement com.kutugondrong.networkkg.annotation.Query or\n" +
                                    "you need implement com.kutugondrong.networkkg.annotation.Path  or\n" +
                                    "you need implement com.kutugondrong.networkkg.annotation.Header \n" +
                                    "to initiate " +
                                    "parameter")
                        }
                    }
                }
            }
            formatQuery(params).also { value ->
                pathAndQuery = "$pathAndQuery$value"
            }
        }
        return pathAndQuery
    }

    @Throws(UnsupportedEncodingException::class)
    private fun formatQuery(params: LinkedHashMap<String, String>): String {
        val result = StringBuilder()
        var first = true
        for (pair in params) {
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(pair.key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(pair.value, "UTF-8"))
        }
        var checkResult = result.toString()
        if (checkResult.isNotBlank()) {
            checkResult = "?$checkResult"
        }
        return checkResult
    }

    companion object {
        private const val TAG = "NetworkKG: "
        inline fun dslNetworkKG(block: Builder.() -> Unit) =
            Builder().apply(block)
                .build()
    }

    class Builder {
        var httpClient: HttpClientKG? = null
        var converterAdapter: ConverterNetworkKGAdapter? = null
        var isDebug = false
        fun build() = NetworkKG(this)
    }

    private var isDebug: Boolean = false
    private var customProperties: MutableList<PropertyKG> = ArrayList()
    private var jsonBodyValue: String? = null

    init {
        httpClient = builder.httpClient
        httpClient?.properties {
            property {
                key = "Content-Type"
                value = "application/json; utf-8"
            }
            property {
                key = "Accept"
                value = "application/json"
            }
        }
        converterAdapter.add(0, builder.converterAdapter)
        isDebug = builder.isDebug
    }
}



