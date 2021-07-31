# NETWORKKG

A type-safe HTTP client for Android Using Kotlin

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
	        implementation 'com.github.KutuGondrong:network-kg:0.0.1'
	}

```
## How To Use

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
        converterAdapter = JsonKGAdapter.create()
        isDebug = true
    }.createService<TestService>()
```
## Adapter

- [JSONKG](https://github.com/KutuGondrong/jsonkgadapter)
- [GSON](https://github.com/KutuGondrong/gsonkgadapter)

## Website
[kutugondrong.com](https://kutugondrong.com/)
