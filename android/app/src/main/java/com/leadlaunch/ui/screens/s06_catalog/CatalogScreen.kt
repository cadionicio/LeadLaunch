package com.leadlaunch.ui.screens.s06_catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leadlaunch.data.remote.ApiResult
import com.leadlaunch.data.remote.ProductDto
import com.leadlaunch.data.repository.ProductRepository
import com.leadlaunch.ui.components.AppBar
import com.leadlaunch.ui.components.EmptyState
import com.leadlaunch.ui.components.ProductRow
import com.leadlaunch.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    productRepository: ProductRepository,
    onAddProduct: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var loading by remember { mutableStateOf(true) }

    fun loadProducts() {
        scope.launch {
            loading = true
            when (val res = productRepository.getProducts()) {
                is ApiResult.Success -> products = res.data.items
                is ApiResult.Error -> Unit
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadProducts() }

    val filtered = remember(products, searchQuery, selectedFilter) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.nombre.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Activos" -> p.activo && p.stockDisponible > 0
                "Sin stock" -> p.stockDisponible == 0
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { AppBar(title = "Mi Catálogo") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = PrimaryBlue,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o SKU") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Activos", "Sin stock").forEach { chip ->
                    FilterChip(
                        selected = selectedFilter == chip,
                        onClick = { selectedFilter = chip },
                        label = { Text(chip) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = "Sin productos",
                    subtitle = "Agrega tu primer producto para empezar a cotizar.",
                    actionText = "Nuevo producto",
                    onAction = onAddProduct
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(filtered, key = { it.productoId }) { item ->
                        ProductRow(product = item)
                    }
                }
            }
        }
    }
}
