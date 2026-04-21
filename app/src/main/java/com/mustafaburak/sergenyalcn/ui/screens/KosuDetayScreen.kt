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
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import com.mustafaburak.sergenyalcn.ui.viewmodel.KosuSonucuViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KosuDetayScreen(
    navController: NavController,
    factory: ViewModelFactory,
    kosuId: Int
) {
    val viewModel: KosuSonucuViewModel = viewModel(factory = factory)
    val sonuclar by viewModel.sonuclar.collectAsState()
    var sonucEkleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(kosuId) {
        viewModel.kosuYukle(kosuId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Koşu Detayı") },
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
            FloatingActionButton(onClick = { sonucEkleDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Sonuç Ekle")
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
                text = "${sonuclar.size} at kayıtlı",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (sonuclar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz sonuç eklenmemiş.\nSağ alttaki + butonuna bas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sonuclar) { sonucItem ->
                        SonucKarti(
                            sonuc = sonucItem,
                            onSil = { viewModel.sil(sonucItem) }
                        )
                    }
                }
            }
        }
    }

    if (sonucEkleDialog) {
        SonucEkleDialog(
            kosuId = kosuId,
            onDismiss = { sonucEkleDialog = false },
            onKaydet = { sonucItem ->
                viewModel.ekle(sonucItem)
                sonucEkleDialog = false
            }
        )
    }
}

@Composable
fun SonucKarti(sonuc: KosuSonucu, onSil: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (sonuc.sonuc) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.secondaryContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sonuc.sonuc}.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                // ✅ atIsmi artık gösteriliyor
                Text(
                    text = sonuc.atIsmi.ifEmpty { "Start: ${sonuc.startNo}" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = "Start: ${sonuc.startNo} | Jokey: ${sonuc.jokey} | Ant: ${sonuc.antrenor}", fontSize = 13.sp)
                if (sonuc.yas.isNotEmpty() || sonuc.orijin.isNotEmpty()) {
                    Text(
                        text = "${sonuc.yas} | ${sonuc.orijin}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Derece: ${sonuc.derece}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "GNY: ${sonuc.gny}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sıklet: ${sonuc.siklet}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sonuc.fark.isNotEmpty()) {
                    Text(
                        text = "Fark: ${sonuc.fark}",
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
fun SonucEkleDialog(
    kosuId: Int,
    onDismiss: () -> Unit,
    onKaydet: (KosuSonucu) -> Unit
) {
    // ✅ atIsmi state değişkeni eklendi
    var atIsmiGiris by remember { mutableStateOf("") }
    var startNoGiris by remember { mutableStateOf("") }
    var jokeyGiris by remember { mutableStateOf("") }
    var antrenorGiris by remember { mutableStateOf("") }
    var sahipGiris by remember { mutableStateOf("") }
    var sikletGiris by remember { mutableStateOf("") }
    var dereceGiris by remember { mutableStateOf("") }
    var gnyGiris by remember { mutableStateOf("") }
    var agfGiris by remember { mutableStateOf("") }
    var farkGiris by remember { mutableStateOf("") }
    var gikisGiris by remember { mutableStateOf("") }
    var hpGiris by remember { mutableStateOf("") }
    var sonucGiris by remember { mutableStateOf("") }
    var yasGiris by remember { mutableStateOf("") }
    var orijinGiris by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sonuç Ekle") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // ✅ EN ÜSTE EKLENDİ — ML motorunun çalışması için kritik
                item {
                    OutlinedTextField(
                        value = atIsmiGiris,
                        onValueChange = { atIsmiGiris = it.uppercase() },
                        label = { Text("At İsmi *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("ML motoru bu isimle eşleştirir") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = startNoGiris,
                        onValueChange = { startNoGiris = it },
                        label = { Text("Start No *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = sonucGiris,
                        onValueChange = { sonucGiris = it },
                        label = { Text("Sonuç (kaçıncı oldu) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = jokeyGiris,
                        onValueChange = { jokeyGiris = it },
                        label = { Text("Jokey *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = antrenorGiris,
                        onValueChange = { antrenorGiris = it },
                        label = { Text("Antrenör *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = yasGiris,
                        onValueChange = { yasGiris = it },
                        label = { Text("Yaş (Örn: 3y d e)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = orijinGiris,
                        onValueChange = { orijinGiris = it },
                        label = { Text("Orijin (Örn: KLIMT - SHANTI)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = sahipGiris,
                        onValueChange = { sahipGiris = it },
                        label = { Text("Sahip") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = sikletGiris,
                        onValueChange = { sikletGiris = it },
                        label = { Text("Sıklet *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = dereceGiris,
                        onValueChange = { dereceGiris = it },
                        label = { Text("Derece (0.52.29)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = gnyGiris,
                        onValueChange = { gnyGiris = it },
                        label = { Text("Ganyan Oranı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = agfGiris,
                        onValueChange = { agfGiris = it },
                        label = { Text("AGF") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = farkGiris,
                        onValueChange = { farkGiris = it },
                        label = { Text("Fark (2 Boy, Boyun...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = gikisGiris,
                        onValueChange = { gikisGiris = it },
                        label = { Text("Giriş Sırası") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = hpGiris,
                        onValueChange = { hpGiris = it },
                        label = { Text("HP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                // ✅ atIsmiGiris zorunlu alana eklendi
                onClick = {
                    if (atIsmiGiris.isNotEmpty() && startNoGiris.isNotEmpty() &&
                        jokeyGiris.isNotEmpty() && sikletGiris.isNotEmpty() &&
                        sonucGiris.isNotEmpty()
                    ) {
                        onKaydet(
                            KosuSonucu(
                                kosuId = kosuId,
                                atId = 0,
                                atIsmi = atIsmiGiris,                          // ✅ Artık dolu geliyor
                                startNo = startNoGiris.toIntOrNull() ?: 0,
                                jokey = jokeyGiris,
                                antrenor = antrenorGiris,
                                sahip = sahipGiris,
                                siklet = sikletGiris.toFloatOrNull() ?: 0f,
                                derece = dereceGiris,
                                gny = gnyGiris.toFloatOrNull() ?: 0f,
                                agf = agfGiris.toFloatOrNull() ?: 0f,
                                fark = farkGiris,
                                gikis = gikisGiris.toIntOrNull() ?: 0,
                                hp = hpGiris.toIntOrNull() ?: 0,
                                sonuc = sonucGiris.toIntOrNull() ?: 0,
                                yas = yasGiris,
                                orijin = orijinGiris
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