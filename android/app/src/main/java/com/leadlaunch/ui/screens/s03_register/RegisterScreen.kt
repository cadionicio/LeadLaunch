package com.leadlaunch.ui.screens.s03_register

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.repository.AuthRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.AppTextField
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val passwordValid = hasMinLength && hasUppercase && hasNumber

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Crear cuenta", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Empieza en 2 minutos", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text("Crea tu cuenta para registrar tu emprendimiento.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = fullName,
                onValueChange = { fullName = it; nameError = null },
                label = "Nombre completo",
                placeholder = "Ej. Ana Torres",
                errorText = nameError
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                label = "Correo electrónico",
                placeholder = "ana@dulcedetalle.pe",
                errorText = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                placeholder = "Mínimo 8 caracteres",
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(10.dp))

            // Checks en vivo (CP1)
            PasswordCheckRow("Mínimo 8 caracteres", hasMinLength)
            PasswordCheckRow("Al menos una mayúscula", hasUppercase)
            PasswordCheckRow("Al menos un número", hasNumber)

            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = "Crear cuenta",
                enabled = passwordValid && fullName.isNotBlank() && email.isNotBlank(),
                loading = loading,
                onClick = {
                    nameError = if (fullName.trim().length < 2) "Ingresa tu nombre." else null
                    emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        "Correo inválido."
                    } else null

                    if (nameError == null && emailError == null && passwordValid) {
                        scope.launch {
                            loading = true
                            when (val res = authRepository.register(fullName.trim(), email.trim(), password)) {
                                is ApiResult.Success -> onRegisterSuccess()
                                is ApiResult.Error -> {
                                    if (res.isNetworkError) {
                                        snackbarHostState.showSnackbar("Sin internet. Reintentar")
                                    } else {
                                        // Error junto al campo sin borrar lo demás
                                        emailError = res.fieldErrors["correo"] ?: res.message
                                    }
                                }
                            }
                            loading = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun PasswordCheckRow(label: String, passed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (passed) SuccessGreen else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (passed) SuccessGreen else TextSecondary
        )
    }
}
