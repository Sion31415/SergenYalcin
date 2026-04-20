package com.mustafaburak.sergenyalcn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mustafaburak.sergenyalcn.data.db.entity.Kosu
import com.mustafaburak.sergenyalcn.ui.navigation.Screen
import com.mustafaburak.sergenyalcn.ui.viewmodel.KosuViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KosuListesiScreen(navController: NavController, factory: ViewModelFactory) {
    val viewModel: KosuViewModel = viewModel(factory = factory)
    val kosular by viewModel.kosular.collectAsState()

    var kosuEkleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏁 Koşular") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { kosuEkleDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Koşu Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "${kosular.size} koşu kayıtlı",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (kosular.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz koşu eklenmemiş.\nSağ alttaki + butonuna bas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(kosular) { kosu ->
                        KosuKarti(
                            kosu = kosu,
                            onSil = { viewModel.sil(kosu) },
                            onDetay = {
                                navController.navigate(
                                    Screen.KosuDetay.createRoute(kosu.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (kosuEkleDialog) {
        KosuEkleDialog(
            onDismiss = { kosuEkleDialog = false },
            onKaydet = { kosu ->
                viewModel.ekle(kosu)
                kosuEkleDialog = false
            }
        )
    }
}

@Composable
fun KosuKarti(kosu: Kosu, onSil: () -> Unit, onDetay: () -> Unit) {
    Card(
        onClick = onDetay,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${kosu.tarih} — Koşu ${kosu.kosuNo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${kosu.hipodrom} • ${kosu.mesafe}m",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pist: ${kosu.pistDurumu} • Hava: ${kosu.havaDurumu}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSil) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun KosuEkleDialog(onDismiss: () -> Unit, onKaydet: (Kosu) -> Unit) {
    var tarih by remember { mutableStateOf("") }
    var hipodrom by remember { mutableStateOf("istanbul") }
    var kosuNo by remember { mutableStateOf("") }
    var mesafe by remember { mutableStateOf("") }
    var pistDurumu by remember { mutableStateOf("kuru") }
    var havaDurumu by remember { mutableStateOf("açık") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Koşu Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tarih,
                    onValueChange = { tarih = it },
                    label = { Text("Tarih (2024-01-15) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = kosuNo,
                    onValueChange = { kosuNo = it },
                    label = { Text("Koşu No *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mesafe,
                    onValueChange = { mesafe = it },
                    label = { Text("Mesafe (metre) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Hipodrom seçimi
                Text("Hipodrom:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("istanbul", "ankara", "izmir", "bursa").forEach { h ->
                        FilterChip(
                            selected = hipodrom == h,
                            onClick = { hipodrom = h },
                            label = { Text(h.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                // Pist durumu
                Text("Pist Durumu:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("kuru", "iyi", "ağır", "çok ağır").forEach { p ->
                        FilterChip(
                            selected = pistDurumu == p,
                            onClick = { pistDurumu = p },
                            label = { Text(p) }
                        )
                    }
                }
                // Hava durumu
                Text("Hava:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("açık", "bulutlu", "yağmurlu").forEach { h ->
                        FilterChip(
                            selected = havaDurumu == h,
                            onClick = { havaDurumu = h },
                            label = { Text(h) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tarih.isNotEmpty() && kosuNo.isNotEmpty() && mesafe.isNotEmpty()) {
                        onKaydet(
                            Kosu(
                                tarih = tarih,
                                hipodrom = hipodrom,
                                kosuNo = kosuNo.toIntOrNull() ?: 1,
                                mesafe = mesafe.toIntOrNull() ?: 1000,
                                pistDurumu = pistDurumu,
                                havaDurumu = havaDurumu
                            )
                        )
                    }
                }
            ) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}