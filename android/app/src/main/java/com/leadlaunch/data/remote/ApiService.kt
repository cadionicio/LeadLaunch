package com.leadlaunch.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("businesses")
    suspend fun createBusiness(@Body request: BusinessCreateRequest): BusinessResponse

    @GET("businesses/{id}/dashboard")
    suspend fun getDashboard(@Path("id") businessId: String): DashboardResponse

    @POST("businesses/{id}/products")
    suspend fun createProduct(
        @Path("id") businessId: String,
        @Body request: ProductCreateRequest
    ): ProductDto

    @GET("businesses/{id}/products")
    suspend fun getProducts(
        @Path("id") businessId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ProductListResponse

    @GET("businesses/{id}/rules")
    suspend fun getRules(@Path("id") businessId: String): RulesConfigPayload

    @PUT("businesses/{id}/rules")
    suspend fun updateRules(
        @Path("id") businessId: String,
        @Body request: RulesConfigPayload
    ): RulesConfigPayload

    @POST("businesses/{id}/radar/scan")
    suspend fun scanRadar(
        @Path("id") businessId: String,
        @Body request: RadarScanRequest
    ): RadarScanResponse

    // --- FASE 2 (CP3, CP4, CP5) ---
    @POST("businesses/{id}/campaigns/generate")
    suspend fun generateCampaign(
        @Path("id") businessId: String,
        @Body request: CampaignGenerateRequest
    ): CampaignResponse

    @POST("businesses/{id}/quotes")
    suspend fun createQuote(
        @Path("id") businessId: String,
        @Body request: QuoteCreateRequest
    ): QuoteResponse

    @GET("businesses/{id}/quotes")
    suspend fun getQuotes(
        @Path("id") businessId: String
    ): QuoteListResponse

    @PATCH("quotes/{quote_id}/status")
    suspend fun updateQuoteStatus(
        @Path("quote_id") quoteId: String,
        @Body request: QuoteStatusUpdateRequest
    ): QuoteResponse
}
