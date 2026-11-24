package com.leadlaunch.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("businesses")
    suspend fun createBusiness(@Body request: BusinessCreateRequest): BusinessResponse

    @GET("businesses/{id}/dashboard")
    suspend fun getDashboard(@Path("id") businessId: String): DashboardResponse

    @GET("businesses/{id}/products")
    suspend fun getProducts(
        @Path("id") businessId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ProductListResponse

    @POST("businesses/{id}/products")
    suspend fun createProduct(
        @Path("id") businessId: String,
        @Body request: ProductCreateRequest
    ): ProductDto

    @GET("businesses/{id}/rules")
    suspend fun getRules(@Path("id") businessId: String): RulesDto

    @PUT("businesses/{id}/rules")
    suspend fun updateRules(
        @Path("id") businessId: String,
        @Body request: RulesDto
    ): RulesDto

    @POST("businesses/{id}/radar/scan")
    suspend fun scanRadar(
        @Path("id") businessId: String,
        @Body request: RadarScanRequest
    ): RadarScanResponse

    @GET("businesses/{id}/radar/latest")
    suspend fun getLatestRadar(@Path("id") businessId: String): RadarScanResponse
}
