package com.leadlaunch.data.repository

import com.leadlaunch.data.local.SessionStore
import com.leadlaunch.data.remote.*

class AuthRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        val result = if (AppConfig.USE_MOCK_API) {
            MockBackend.login(LoginRequest(email, password))
        } else {
            safeApiCall { api.login(LoginRequest(email, password)) }
        }
        if (result is ApiResult.Success) {
            sessionStore.saveAuth(
                token = result.data.accessToken,
                name = result.data.usuario.nombreCompleto,
                email = result.data.usuario.correo,
                businessId = result.data.negocioId
            )
        }
        return result
    }

    suspend fun register(name: String, email: String, password: String): ApiResult<AuthResponse> {
        val result = if (AppConfig.USE_MOCK_API) {
            MockBackend.register(RegisterRequest(name, email, password))
        } else {
            safeApiCall { api.register(RegisterRequest(name, email, password)) }
        }
        if (result is ApiResult.Success) {
            sessionStore.saveAuth(
                token = result.data.accessToken,
                name = result.data.usuario.nombreCompleto,
                email = result.data.usuario.correo,
                businessId = result.data.negocioId
            )
        }
        return result
    }

    suspend fun logout() = sessionStore.clear()
}

class BusinessRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun createBusiness(request: BusinessCreateRequest): ApiResult<BusinessResponse> {
        val result = if (AppConfig.USE_MOCK_API) {
            MockBackend.createBusiness(request)
        } else {
            safeApiCall { api.createBusiness(request) }
        }
        if (result is ApiResult.Success) {
            sessionStore.saveBusiness(result.data.negocioId, result.data.razonComercial)
        }
        return result
    }

    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.getDashboard()
        } else {
            safeApiCall { api.getDashboard(businessId) }
        }
    }
}

class ProductRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun getProducts(page: Int = 1): ApiResult<ProductListResponse> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.getProducts()
        } else {
            safeApiCall { api.getProducts(businessId, page) }
        }
    }

    suspend fun createProduct(request: ProductCreateRequest): ApiResult<ProductDto> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.createProduct(request)
        } else {
            safeApiCall { api.createProduct(businessId, request) }
        }
    }
}

class RulesRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun getRules(): ApiResult<RulesConfigPayload> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.getRules()
        } else {
            safeApiCall { api.getRules(businessId) }
        }
    }

    suspend fun updateRules(payload: RulesConfigPayload): ApiResult<RulesConfigPayload> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.updateRules(payload)
        } else {
            safeApiCall { api.updateRules(businessId, payload) }
        }
    }
}

class RadarRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun scan(radiusKm: Double): ApiResult<RadarScanResponse> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API) {
            MockBackend.scanRadar(radiusKm)
        } else {
            safeApiCall { api.scanRadar(businessId, RadarScanRequest(radiusKm)) }
                .also { if (it is ApiResult.Success) MockBackend.lastScan = it.data }
        }
    }

    fun getLastCachedScan(): RadarScanResponse? = MockBackend.lastScan
}

class CampaignRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun generateCampaign(request: CampaignGenerateRequest): ApiResult<CampaignResponse> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API || AppConfig.USE_MOCK_PHASE2) {
            MockBackend.generateCampaign(request)
        } else {
            safeApiCall { api.generateCampaign(businessId, request) }
                .also { if (it is ApiResult.Success) MockBackend.lastCampaign = it.data }
        }
    }

    fun getLastCampaign(): CampaignResponse? = MockBackend.lastCampaign
}

class QuoteRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore
) {
    suspend fun createQuote(
        request: QuoteCreateRequest,
        catalogSnapshot: List<ProductDto> = emptyList(),
        rulesSnapshot: RulesConfigPayload? = null
    ): ApiResult<QuoteResponse> {
        val session = sessionStore.getSession()
        val businessId = session.businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API || AppConfig.USE_MOCK_PHASE2) {
            MockBackend.createQuote(
                request = request,
                catalogSnapshot = catalogSnapshot,
                rules = rulesSnapshot ?: MockBackend.rulesConfig,
                businessName = session.businessName ?: MockBackend.currentBusiness?.razonComercial ?: "LeadLaunch"
            )
        } else {
            safeApiCall { api.createQuote(businessId, request) }
                .also { if (it is ApiResult.Success) MockBackend.lastQuote = it.data }
        }
    }

    suspend fun getQuotes(): ApiResult<QuoteListResponse> {
        val businessId = sessionStore.getSession().businessId ?: "neg-demo-001"
        return if (AppConfig.USE_MOCK_API || AppConfig.USE_MOCK_PHASE2) {
            MockBackend.getQuotes()
        } else {
            safeApiCall { api.getQuotes(businessId) }
        }
    }

    suspend fun updateQuoteStatus(quoteId: String, status: String): ApiResult<QuoteResponse> {
        return if (AppConfig.USE_MOCK_API || AppConfig.USE_MOCK_PHASE2) {
            MockBackend.updateQuoteStatus(quoteId, status)
        } else {
            safeApiCall { api.updateQuoteStatus(quoteId, QuoteStatusUpdateRequest(status)) }
                .also { if (it is ApiResult.Success) MockBackend.lastQuote = it.data }
        }
    }

    fun getLastQuote(): QuoteResponse? = MockBackend.lastQuote

    fun setCurrentQuote(quote: QuoteResponse) {
        MockBackend.lastQuote = quote
    }
}
