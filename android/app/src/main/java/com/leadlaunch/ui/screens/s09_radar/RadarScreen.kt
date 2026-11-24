package com.leadlaunch.ui.screens.s09_radar

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.MockBackend
import com.leadlaunch.data.repository.RadarRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.PrimaryButton
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Locale

@Composable
fun RadarScreen(
    radarRepository: RadarRepository,
    onScanComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val biz = MockBackend.currentBusiness
    val centerLat = biz?.latitud ?: -11.9912
    val centerLon = biz?.longitud ?: -77.0706

    var radiusKm by remember { mutableFloatStateOf((biz?.radioCoberturaKm ?: 3.0).toFloat()) }
    var scanning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Radar de Competencia", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                text = "Analiza quejas reales de negocios cercanos en tu zona.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)
                            controller.setCenter(GeoPoint(centerLat, centerLon))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()
                        val center = GeoPoint(centerLat, centerLon)

                        val circle = Polygon(mapView).apply {
                            points = Polygon.pointsAsCircle(center, radiusKm * 1000.0)
                            fillPaint.color = AndroidColor.argb(40, 23, 92, 244)
                            outlinePaint.color = AndroidColor.rgb(23, 92, 244)
                            outlinePaint.strokeWidth = 3f
                        }
                        mapView.overlays.add(circle)

                        val pin = Marker(mapView).apply {
                            position = center
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = biz?.razonComercial ?: "Mi negocio"
                        }
                        mapView.overlays.add(pin)
                        mapView.invalidate()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Radio del radar: ${String.format(Locale.US, "%.1f", radiusKm)} km",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = radiusKm,
                onValueChange = { radiusKm = it },
                valueRange = 1f..10f,
                steps = 17,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )

            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = if (scanning) "Escaneando zona..." else "Escanear competencia",
                loading = scanning,
                onClick = {
                    scope.launch {
                        scanning = true
                        when (val res = radarRepository.scan(radiusKm.toDouble())) {
                            is ApiResult.Success -> onScanComplete()
                            is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                        }
                        scanning = false
                    }
                }
            )
        }
    }
}
