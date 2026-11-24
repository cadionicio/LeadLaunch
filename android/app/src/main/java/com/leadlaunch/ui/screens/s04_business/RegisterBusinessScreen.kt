package com.leadlaunch.ui.screens.s04_business

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.BusinessCreateRequest
import com.leadlaunch.data.repository.BusinessRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.AppTextField
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

private val RUBROS = listOf(
    1 to "Repostería y Pastelería",
    2 to "Comida Rápida y Bocaditos",
    3 to "Cafetería y Postres"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBusinessScreen(
    businessRepository: BusinessRepository,
    onBusinessCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var selectedRubroId by remember { mutableIntStateOf(1) }
    var description by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Los Olivos") }
    var whatsapp by remember { mutableStateOf("51") }
    var radiusKm by remember { mutableFloatStateOf(3f) }
    var selectedLat by remember { mutableDoubleStateOf(-11.9912) }
    var selectedLon by remember { mutableDoubleStateOf(-77.0706) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }
    var whatsappError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getLastKnownLocation(context)?.let { (lat, lon) ->
                selectedLat = lat
                selectedLon = lon
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Registra tu negocio") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Nombre del emprendimiento",
                placeholder = "Ej. Dulce Detalle",
                errorText = nameError
            )
            Spacer(Modifier.height(14.dp))

            Text("Categoría (Rubro)", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RUBROS.forEach { (id, label) ->
                    FilterChip(
                        selected = selectedRubroId == id,
                        onClick = { selectedRubroId = id },
                        label = { Text(label.split(" ").first(), maxLines = 1) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = description,
                onValueChange = { description = it; descError = null },
                label = "Descripción del negocio *",
                placeholder = "Ej. Tortas temáticas y bocaditos artesanales a pedido",
                errorText = descError,
                singleLine = false
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = district,
                onValueChange = { district = it },
                label = "Distrito",
                placeholder = "Ej. Los Olivos"
            )
            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = whatsapp,
                onValueChange = { input ->
                    whatsapp = input.filter { it.isDigit() }
                    whatsappError = null
                },
                label = "WhatsApp (solo dígitos con código de país)",
                placeholder = "51987654321",
                errorText = whatsappError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(14.dp))

            // Radio del radar (Decisión D5)
            Text(
                text = "Radio del radar: ${String.format(Locale.US, "%.1f", radiusKm)} km",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary
            )
            Slider(
                value = radiusKm,
                onValueChange = { radiusKm = it },
                valueRange = 1f..10f,
                steps = 17,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )
            Spacer(Modifier.height(10.dp))

            // Mapa osmdroid (Decisión D10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ubicación (toca el mapa para fijar pin)", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                TextButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mi ubicación", color = PrimaryBlue)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(selectedLat, selectedLon))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()
                        val point = GeoPoint(selectedLat, selectedLon)
                        mapView.controller.animateTo(point)

                        val marker = Marker(mapView).apply {
                            position = point
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = name.ifBlank { "Mi negocio" }
                        }
                        mapView.overlays.add(marker)
                        mapView.overlays.add(
                            MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedLat = p.latitude
                                    selectedLon = p.longitude
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            })
                        )
                        mapView.invalidate()
                    }
                )
            }
            Text(
                text = "Pin: ${String.format(Locale.US, "%.4f, %.4f", selectedLat, selectedLon)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "Guardar negocio y continuar",
                loading = loading,
                onClick = {
                    nameError = if (name.trim().length < 2) "Ingresa el nombre de tu negocio." else null
                    descError = if (description.trim().length < 5) "La descripción es obligatoria." else null
                    whatsappError = if (whatsapp.length !in 10..15) "Ingresa entre 10 y 15 dígitos (ej. 51987654321)." else null

                    if (nameError == null && descError == null && whatsappError == null) {
                        scope.launch {
                            loading = true
                            val req = BusinessCreateRequest(
                                rubroId = selectedRubroId,
                                razonComercial = name.trim(),
                                descripcion = description.trim(),
                                whatsappNumero = whatsapp,
                                distrito = district.trim().ifBlank { "Lima" },
                                latitud = selectedLat,
                                longitud = selectedLon,
                                radioCoberturaKm = radiusKm.toDouble()
                            )
                            when (val res = businessRepository.createBusiness(req)) {
                                is ApiResult.Success -> onBusinessCreated()
                                is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                            }
                            loading = false
                        }
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(context: Context): Pair<Double, Double>? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    return loc?.let { Pair(it.latitude, it.longitude) }
}
