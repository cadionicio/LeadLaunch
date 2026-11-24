package com.leadlaunch.ui.screens.s02_login

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.repository.AuthRepository
import com.leadlaunch.ui.components.AppTextField
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (hasBusiness: Boolean) -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = BackgroundLight,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bienvenido a LeadLaunch", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Inicia sesión para gestionar tu radar local y cotizaciones.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(28.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it; emailError = null; generalError = null },
                label = "Correo electrónico",
                placeholder = "ejemplo@correo.com",
                errorText = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it; passwordError = null; generalError = null },
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                errorText = passwordError,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                }
            )

            // Decisión D6: deshabilitado con "Próximamente"
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {}, enabled = false) {
                    Text("¿Olvidaste tu contraseña? (Próximamente)", color = TextSecondary)
                }
            }

            if (generalError != null) {
                Surface(
                    color = ErrorSoft,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(
                        text = generalError!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Iniciar sesión",
                loading = loading,
                onClick = {
                    emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        "Ingresa un correo válido."
                    } else null
                    passwordError = if (password.isBlank()) "Ingresa tu contraseña." else null

                    if (emailError == null && passwordError == null) {
                        scope.launch {
                            loading = true
                            when (val res = authRepository.login(email.trim(), password)) {
                                is ApiResult.Success -> {
                                    onLoginSuccess(!res.data.negocioId.isNullOrBlank())
                                }
                                is ApiResult.Error -> {
                                    if (res.isNetworkError) {
                                        snackbarHostState.showSnackbar("Sin internet. Reintentar")
                                    } else {
                                        generalError = "No pudimos iniciar sesión. Revisa tus datos."
                                    }
                                }
                            }
                            loading = false
                        }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))
            // Decisión D6: botón Google deshabilitado
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continuar con Google (Próximamente)", color = TextSecondary)
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onGoRegister) {
                    Text("Crear cuenta", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
