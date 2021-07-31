# NETWORKKG

A type-safe HTTP client for Android Using Kotlin with suspend function. NetworkKG uses the [HttpClientKG](https://github.com/KutuGondrong/httpclient-kg) library for HTTP requests. 

## Usage
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```
dependencies {
        implementation 'com.github.KutuGondrong:network-kg:0.0.2'
}
```
## Introduction
NetworkKG turns your HTTP API into a Kotlin interface.
```
interface PhotoService {
    //With CallbackKG
    @ReturnTypeKG(PhotoResponse::class, true)
    @Network(NetworkType.GET, "/photos")
    suspend fun getPhotos() : CallbackKG<List<PhotoResponse>>?
    
    //Without CallbackKG
    @ReturnTypeKG(PhotoResponse::class, true)
    @Network(NetworkType.GET, "/photos")
    suspend fun getPhotos() : List<PhotoResponse>?
}
```
The NetworkKG class generates an implementation of the PhotoService interface.
```
val service = NetworkKG.dslNetworkKG{
        httpClient = HttpClientKG.dslDefaultHttpClient {
            baseUrl = BuildConfig.SERVER_BASE_URL
            properties {
                property {
                    key = "Authorization"
                    value = "Client-ID ${BuildConfig.API_KEY}"
                }
            }
        }
        //See below the json adapter converter that can be used
        converterAdapter = JsonKGAdapter.create()
        isDebug = true
    }.createService<PhotoService>()
    
val response = service.getPhotos()
```
## Anotation
Annotations on the interface methods and its parameters indicate how a request will be handled.
#### @ReturnTypeKG
ReturnTypeKG has two parameters. value parameter defines the object class that the service returns and isList(true/false) defines the service return whether it is an array list or not.
```
    /**
     * If the return from service is an object without or using Return Type KG
     * the service will work, but if you using ReturnTypeKG set isList false
     * @ReturnTypeKG(PhotoResponse::class, false) or
     * @ReturnTypeKG(PhotoResponse::class)
     */
    @Network(NetworkType.GET, "/photo")
    suspend fun getPhoto() : PhotoResponse?
    
    @ReturnTypeKG(PhotoResponse::class)
    @Network(NetworkType.GET, "/photo")
    suspend fun getPhoto() : PhotoResponse?
```
```
    /**
     * If the return from service is an Array of object we need anotation ReturnTypeKG
     * for parameter value set class object service return
     * for parameter isList set true
     */
    @ReturnTypeKG(PhotoResponse::class, true)
    @Network(NetworkType.GET, "/photos")
    suspend fun getPhotos() : List<PhotoResponse>?
```
```
    /**
     * If the return from service is 
     * an CallbackKG we need anotation ReturnTypeKG
     * for parameter value set class object service return
     * for parameter isList set true or false
     * CallbackKG<List<PhotoResponse>> => isList set true
     * CallbackKG<PhotoResponse> => isList set false
     * @ReturnTypeKG(PhotoResponse::class, true/false)
     */
     
    @ReturnTypeKG(PhotoResponse::class, false)
    @Network(NetworkType.GET, "/photo")
    suspend fun getPhoto() : CallbackKG<PhotoResponse>?
    
    @ReturnTypeKG(PhotoResponse::class, true)
    @Network(NetworkType.GET, "/photos")
    suspend fun getPhotos() : CallbackKG<List<PhotoResponse>>?
```
#### @Network
Network has two parameters. the type parameter defines what request method is used. And the path parameter defines the path of the url before running the service
```
    /**
     * Request Method Get : @Network(NetworkType.GET, "/photos")
     * Request Method POST : @Network(NetworkType.POST, "/photos")
     * Request Method PUT : @Network(NetworkType.PUT, "/photos")
     * Request Method DELETE : @Network(NetworkType.DELETE, "/photos")
     */
    @Network(NetworkType.GET, "/photo")
    suspend fun getPhoto() : PhotoResponse?
```
#### @Path
A request URL can be updated dynamically using replacement blocks and parameters on the method. A replacement block is an alphanumeric string surrounded by { and }. A corresponding parameter must be annotated with @Path using the same string.
```
    @Network(NetworkType.GET, "/photo/{customPath}/detail")
    suspend fun getPhotos(@Path("customPath") id: String) : PhotoResponse?
```
#### @Query
Query parameters can also be added.
```
    @Network(NetworkType.GET, "/photo/{customPath}/detail")
    suspend fun getPhotos(@Path("customPath") id: String, @Query("typeImage") type: Int) : PhotoResponse?
```
#### @Body
An object can be specified for use as an HTTP request body with the @Body annotation.
```
    @Network(NetworkType.POST, "/photo")
    suspend fun createPhoto(@Body photo: Photo) : PhotoResponse?
```
#### @Header
A request Header can be updated dynamically using the @Header annotation. A corresponding parameter must be provided to the @Header.
```
    @Network(NetworkType.POST, "/photo")
    suspend fun createPhoto(@Body photo: Photo, @Header("Autorize") auth: String) : PhotoResponse?
```
## Adapter
Converters Adapter can be added to support other types. 
- [JSONKG](https://github.com/KutuGondrong/jsonkgadapter)
- [GSON](https://github.com/KutuGondrong/gsonkgadapter)

## Website
[kutugondrong.com](https://kutugondrong.com/)
