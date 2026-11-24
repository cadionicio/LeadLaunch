package com.leadlaunch.data.remote

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: String? = null,
        val errorFields: List<String> = emptyList(),
        val isUnauthorized: Boolean = false,
        val isNetworkError: Boolean = false
    ) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: HttpException) {
        val statusCode = e.code()
        val bodyString = e.response()?.errorBody()?.string()
        val parsed = runCatching {
            Gson().fromJson(bodyString, ApiErrorWrapper::class.java)?.error
        }.getOrNull()

        ApiResult.Error(
            message = parsed?.mensaje ?: when (statusCode) {
                401 -> "Tu sesión expiró. Inicia sesión nuevamente."
                422 -> "Revisa los campos ingresados."
                else -> "Ocurrió un error en el servidor ($statusCode)."
            },
            code = parsed?.codigo,
            errorFields = parsed?.campos ?: emptyList(),
            isUnauthorized = statusCode == 401
        )
    } catch (e: IOException) {
        ApiResult.Error(
            message = "Sin conexión a internet. Verifica tu red.",
            isNetworkError = true
        )
    } catch (e: Exception) {
        ApiResult.Error(message = e.localizedMessage ?: "Error inesperado.")
    }
}