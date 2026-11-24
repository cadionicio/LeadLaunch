package com.leadlaunch.ui.screens.s15_quotes_history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.QuoteResponse
import com.leadlaunch.data.repository.QuoteRepository
import com.leadlaunch.ui.components.*
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QuotesHistoryScreen(
    quoteRepository: QuoteRepository,
    onNewQuote: () -> Unit,
    onOpenQuote: (QuoteResponse) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var quotes by remember { mutableStateOf<List<QuoteResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODAS") }

    // Modal de confirmación para GANADA / PERDIDA
    var pendingStatusChange by remember { mutableStateOf<Pair<QuoteResponse, String>?>(null) }
    var updating by remember { mutableStateOf(false) }

    fun loadQuotes() {
        scope.launch {
            loading = true
            errorMsg = null
            when (val res = quoteRepository.getQuotes()) {
                is ApiResult.Success -> quotes = res.data.items
                is ApiResult.Error -> errorMsg = res.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadQuotes() }

    val filtered = remember(quotes, searchQuery, selectedFilter) {
        quotes.filter { q ->
            val matchesQuery = searchQuery.isBlank() ||
                q.nombreProspecto.contains(searchQuery, ignoreCase = true) ||
                (q.codigo ?: q.cotizacionId).contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedFilter == "TODAS" || q.estado.equals(selectedFilter, ignoreCase = true)
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Cotizaciones") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewQuote,
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Nueva cotización") },
                text = { Text("Nueva cotización") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SimulatedDataBanner(Modifier.padding(bottom = 12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por cliente o código") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            val filterOptions = listOf(
                "TODAS" to "Todas (${quotes.size})",
                "EMITIDA" to "Emitida (${quotes.count { it.estado == "EMITIDA" }})",
                "ENVIADA_WA" to "Enviada (${quotes.count { it.estado == "ENVIADA_WA" }})",
                "GANADA" to "Ganada (${quotes.count { it.estado == "GANADA" }})",
                "PERDIDA" to "Perdida (${quotes.count { it.estado == "PERDIDA" }})"
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                errorMsg != null -> {
                    EmptyState(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "No pudimos cargar las cotizaciones",
                        subtitle = errorMsg!!,
                        actionText = "Reintentar",
                        onAction = { loadQuotes() }
                    )
                }
                filtered.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Aún no hay cotizaciones",
                        subtitle = "Crea tu primera cotización y envíala por WhatsApp en un toque.",
                        actionText = "Nueva cotización",
                        onAction = onNewQuote
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 92.dp)
                    ) {
                        items(filtered, key = { it.cotizacionId }) { quote ->
                            QuoteHistoryRow(
                                quote = quote,
                                onClick = {
                                    quoteRepository.setCurrentQuote(quote)
                                    onOpenQuote(quote)
                                },
                                onSelectStatus = { newStatus ->
                                    if (newStatus == "GANADA" || newStatus == "PERDIDA") {
                                        pendingStatusChange = quote to newStatus
                                    } else {
                                        scope.launch {
                                            when (val res = quoteRepository.updateQuoteStatus(quote.cotizacionId, newStatus)) {
                                                is ApiResult.Success -> {
                                                    loadQuotes()
                                                    snackbarHostState.showSnackbar("Estado actualizado")
                                                }
                                                is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingStatusChange?.let { (quote, newStatus) ->
        val label = if (newStatus == "GANADA") "Ganada" else "Perdida"
        AlertDialog(
            onDismissRequest = { if (!updating) pendingStatusChange = null },
            title = { Text("Marcar cotización como $label") },
            text = {
                Text(
                    "Esta acción actualizará tus métricas de ventas y tasa de conversión para ${quote.nombreProspecto} (${formatSoles(quote.totalNeto)})."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !updating,
                    onClick = {
                        scope.launch {
                            updating = true
                            when (val res = quoteRepository.updateQuoteStatus(quote.cotizacionId, newStatus)) {
                                is ApiResult.Success -> {
                                    loadQuotes()
                                    snackbarHostState.showSnackbar("Cotización marcada como $label")
                                }
                                is ApiResult.Error -> snackbarHostState.showSnackbar(res.message)
                            }
                            updating = false
                            pendingStatusChange = null
                        }
                    }
                ) {
                    Text(
                        text = "Confirmar $label",
                        color = if (newStatus == "GANADA") SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !updating,
                    onClick = { pendingStatusChange = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun QuoteHistoryRow(
    quote: QuoteResponse,
    onClick: () -> Unit,
    onSelectStatus: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = quote.codigo ?: quote.cotizacionId.take(8).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    QuoteStatusBadge(quote.estado)
                }
                Spacer(Modifier.height(4.dp))
                Text(quote.nombreProspecto, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = quote.creadoEn ?: "Reciente",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = formatSoles(quote.totalNeto),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Cambiar estado de cotización")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Marcar como Enviada") },
                        onClick = { menuExpanded = false; onSelectStatus("ENVIADA_WA") }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar como Ganada", color = SuccessGreen) },
                        onClick = { menuExpanded = false; onSelectStatus("GANADA") }
                    )
                    DropdownMenuItem(
                        text = { Text("Marcar como Perdida", color = ErrorRed) },
                        onClick = { menuExpanded = false; onSelectStatus("PERDIDA") }
                    )
                }
            }
        }
    }
}
