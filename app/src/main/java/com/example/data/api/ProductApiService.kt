package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ProductApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: List<ApiProduct>?
)

data class ApiProduct(
    @Json(name = "id") val id: String,
    @Json(name = "sku") val sku: String,
    @Json(name = "name") val name: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "price") val price: Double,
    @Json(name = "currency") val currency: String?,
    @Json(name = "inventory_count") val inventoryCount: Int,
    @Json(name = "image_urls") val imageUrls: List<String>?,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "category_name") val categoryName: String?
)

interface ProductApiService {
    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("locale") locale: String = "en",
        @Query("active_only") activeOnly: Boolean = true
    ): ProductApiResponse
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/" // standard localhost loopback emulator address

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val productApiService: ProductApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ProductApiService::class.java)
    }
}
