package com.leadlaunch.data.remote

import com.leadlaunch.data.local.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
    private val onUnauthorized: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionStore.getToken() }
        val requestBuilder = chain.request().newBuilder()
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401) {
            runBlocking { sessionStore.clear() }
            onUnauthorized()
        }
        return response
    }
}
