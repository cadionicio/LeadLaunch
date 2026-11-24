package com.leadlaunch.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "leadlaunch_session")

data class UserSession(
    val token: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val businessId: String? = null,
    val businessName: String? = null
)

class SessionStore(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_BUSINESS_ID = stringPreferencesKey("negocio_id")
        private val KEY_BUSINESS_NAME = stringPreferencesKey("business_name")
    }

    val sessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            token = prefs[KEY_TOKEN],
            userName = prefs[KEY_USER_NAME],
            userEmail = prefs[KEY_USER_EMAIL],
            businessId = prefs[KEY_BUSINESS_ID],
            businessName = prefs[KEY_BUSINESS_NAME]
        )
    }

    suspend fun getSession(): UserSession = sessionFlow.first()

    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]

    suspend fun saveAuth(token: String, name: String, email: String, businessId: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            if (businessId != null) {
                prefs[KEY_BUSINESS_ID] = businessId
            }
        }
    }

    suspend fun saveBusiness(businessId: String, businessName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BUSINESS_ID] = businessId
            prefs[KEY_BUSINESS_NAME] = businessName
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
