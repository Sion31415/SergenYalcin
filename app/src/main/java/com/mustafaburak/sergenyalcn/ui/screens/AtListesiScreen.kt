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
import com.mustafaburak.sergenyalcn.data.db.entity.At
import com.mustafaburak.sergenyalcn.ui.viewmodel.AtViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtListesiScreen(navController: NavController, factory: ViewModelFactory) {
    val viewModel: AtViewModel = viewModel(factory = factory)
    val atlar by viewModel.atlar.collectAsState()
    val aramaMetni by viewModel.aramaMetni.collectAsState()

    var atEkleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐴 Atlar") },
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
            FloatingActionButton(onClick = { atEkleDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "At Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Arama kutusu
            OutlinedTextField(
                value = aramaMetni,
                onValueChange = { viewModel.ara(it) },
                label = { Text("At ara...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${atlar.size} at kayıtlı",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (atlar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz at eklenmemiş.\nSağ alttaki + butonuna bas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(atlar) { at ->
                        AtKarti(at = at, onSil = { viewModel.sil(at) })
                    }
                }
            }
        }
    }

    if (atEkleDialog) {
        AtEkleDialog(
            onDismiss = { atEkleDialog = false },
            onKaydet = { at ->
                viewModel.ekle(at)
                atEkleDialog = false
            }
        )
    }
}

@Composable
fun AtKarti(at: At, onSil: () -> Unit) {
    Card(
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
                    text = at.isim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${at.yas} yaş • ${at.cinsiyet}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (at.baba.isNotEmpty()) {
                    Text(
                        text = "B: ${at.baba} / A: ${at.anne}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
fun AtEkleDialog(onDismiss: () -> Unit, onKaydet: (At) -> Unit) {
    var isim by remember { mutableStateOf("") }
    var yas by remember { mutableStateOf("") }
    var cinsiyet by remember { mutableStateOf("e") }
    var baba by remember { mutableStateOf("") }
    var anne by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni At Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = isim,
                    onValueChange = { isim = it.uppercase() },
                    label = { Text("At İsmi *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = yas,
                    onValueChange = { yas = it },
                    label = { Text("Yaş *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Cinsiyet seçimi
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cinsiyet:")
                    listOf("e" to "Erkek", "d" to "Dişi", "k" to "Kısrak").forEach { (kod, ad) ->
                        FilterChip(
                            selected = cinsiyet == kod,
                            onClick = { cinsiyet = kod },
                            label = { Text(ad) }
                        )
                    }
                }
                OutlinedTextField(
                    value = baba,
                    onValueChange = { baba = it },
                    label = { Text("Baba") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = anne,
                    onValueChange = { anne = it },
                    label = { Text("Anne") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isim.isNotEmpty() && yas.isNotEmpty()) {
                        onKaydet(
                            At(
                                isim = isim,
                                yas = yas.toIntOrNull() ?: 0,
                                cinsiyet = cinsiyet,
                                baba = baba,
                                anne = anne
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