package com.mustafaburak.sergenyalcn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mustafaburak.sergenyalcn.ml.AtGirisi
import com.mustafaburak.sergenyalcn.ml.AtSkoru
import com.mustafaburak.sergenyalcn.ui.viewmodel.TahminViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TahminScreen(navController: NavController, factory: ViewModelFactory) {
    val viewModel: TahminViewModel = viewModel(factory = factory)
    val tahminSonuclari by viewModel.tahminSonuclari.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    var secilenKupon by remember { mutableStateOf("Ganyan") }
    var pistDurumu by remember { mutableStateOf("kuru") }
    var mesafe by remember { mutableStateOf("1200") }
    var atlarMetin by remember { mutableStateOf("") }
    var tahminYapildi by remember { mutableStateOf(false) }

    val kuponlar = listOf("Ganyan", "Plase", "İkili", "Sıralı İkili", "Üçlü", "Dörtlü", "Altılı", "Tabela")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Tahmin") },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Kupon seçimi
            item {
                Text("Kupon Türü", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    kuponlar.take(4).forEach { kupon ->
                        FilterChip(
                            selected = secilenKupon == kupon,
                            onClick = { secilenKupon = kupon },
                            label = { Text(kupon, fontSize = 12.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    kuponlar.drop(4).forEach { kupon ->
                        FilterChip(
                            selected = secilenKupon == kupon,
                            onClick = { secilenKupon = kupon },
                            label = { Text(kupon, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Pist durumu
            item {
                Text("Pist Durumu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("kuru", "iyi", "ağır", "çok ağır").forEach { p ->
                        FilterChip(
                            selected = pistDurumu == p,
                            onClick = { pistDurumu = p },
                            label = { Text(p) }
                        )
                    }
                }
            }

            // Mesafe
            item {
                Text("Mesafe", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mesafe,
                    onValueChange = { mesafe = it },
                    label = { Text("Mesafe (metre)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // At girişi
            item {
                Text("Atlar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "Her satıra bir at: StartNo,AtId,Jokey,Antrenor,Siklet",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = atlarMetin,
                    onValueChange = { atlarMetin = it },
                    label = { Text("Örnek: 1,23,H.CİZİK,YUS.KAYA,57") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 10
                )
            }

            // Tahmin butonu
            item {
                Button(
                    onClick = {
                        val atlar = atlarMetinParse(atlarMetin)
                        if (atlar.isNotEmpty()) {
                            viewModel.tahminYap(
                                atlar = atlar,
                                pistDurumu = pistDurumu,
                                mesafe = mesafe.toIntOrNull() ?: 1200,
                                kosuId = 0
                            )
                            tahminYapildi = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !yukleniyor
                ) {
                    if (yukleniyor) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("TAHMİN YAP", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sonuçlar
            if (tahminYapildi && tahminSonuclari.isNotEmpty()) {
                item {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📊 $secilenKupon Tahmini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Kupon türüne göre sonuçları filtrele
                val gosterilecekSonuclar = when (secilenKupon) {
                    "Ganyan" -> tahminSonuclari.take(1)
                    "Plase" -> tahminSonuclari.take(3)
                    "Tabela" -> tahminSonuclari.take(4)
                    "Sıralı İkili" -> tahminSonuclari.take(2)
                    else -> tahminSonuclari.take(3)
                }

                items(gosterilecekSonuclar) { skor ->
                    TahminSonucKarti(skor = skor, sira = tahminSonuclari.indexOf(skor) + 1)
                }

                // Altılı için özel gösterim
                if (secilenKupon == "Altılı") {
                    item {
                        AltiliKart(tahminSonuclari = tahminSonuclari)
                    }
                }
            }
        }
    }
}

@Composable
fun TahminSonucKarti(skor: AtSkoru, sira: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (sira) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.secondaryContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$sira.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start: ${skor.startNo} — ${skor.jokey}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Antrenör: ${skor.antrenor}",
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "%${"%.1f".format(skor.kazanmaTahmini)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skor detayları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkorDetay("Derece", skor.gecmisDereceSkoru)
                SkorDetay("Jokey", skor.jokeySkoru)
                SkorDetay("Antrenör", skor.antrenorSkoru)
                SkorDetay("Sıklet", skor.sikletSkoru)
                SkorDetay("Pist", skor.pistUyumSkoru)
                SkorDetay("Mesafe", skor.mesafeUyumSkoru)
            }
        }
    }
}

@Composable
fun SkorDetay(baslik: String, skor: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = baslik, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "%.2f".format(skor),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AltiliKart(tahminSonuclari: List<AtSkoru>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🎰 Altılı Kombinasyon",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ana seçim: ${tahminSonuclari.take(1).joinToString { "Start ${it.startNo}" }}",
                fontSize = 14.sp
            )
            Text(
                text = "Alternatif: ${tahminSonuclari.drop(1).take(1).joinToString { "Start ${it.startNo}" }}",
                fontSize = 14.sp
            )
        }
    }
}

// At metni parse et
fun atlarMetinParse(metin: String): List<AtGirisi> {
    return metin.lines()
        .filter { it.isNotBlank() }
        .mapNotNull { satir ->
            val parcalar = satir.split(",").map { it.trim() }
            if (parcalar.size >= 5) {
                AtGirisi(
                    atId = parcalar[1].toIntOrNull() ?: 0,
                    atIsmi = "At ${parcalar[0]}",
                    startNo = parcalar[0].toIntOrNull() ?: 0,
                    jokey = parcalar[2],
                    antrenor = parcalar[3],
                    siklet = parcalar[4].toFloatOrNull() ?: 0f
                )
            } else null
        }
}