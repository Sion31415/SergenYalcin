package com.mustafaburak.sergenyalcn.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mustafaburak.sergenyalcn.ml.AtSkoru
import com.mustafaburak.sergenyalcn.ui.viewmodel.TahminViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TahminScreen(navController: NavController, factory: ViewModelFactory) {
    val viewModel: TahminViewModel = viewModel(factory = factory)
    val tahminSonuclari by viewModel.tahminSonuclari.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    // PDF Seçici
    val pdfSecici = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.programdanTahminEt(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Tahmin Sıralaması") },
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Yarış Programı Analizi", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { pdfSecici.launch("application/pdf") },
                            enabled = !yukleniyor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (yukleniyor) "ANALİZ EDİLİYOR..." else "PROGRAM PDF YÜKLE")
                        }
                    }
                }
            }

            if (tahminSonuclari.isNotEmpty()) {
                item {
                    Text(
                        text = "Kazanma İhtimaline Göre Sıralama",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Atları direkt listele (Kupon falan yok)
                items(tahminSonuclari) { skor ->
                    TahminSonucKarti(skor = skor, sira = tahminSonuclari.indexOf(skor) + 1)
                }
            } else if (!yukleniyor) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("Tahminleri görmek için PDF yükleyin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (sira) {
                1 -> MaterialTheme.colorScheme.primaryContainer // 1. At Renkli
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$sira.", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(skor.atIsmi, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Start: ${skor.startNo} | Jokey: ${skor.jokey}", fontSize = 12.sp)
            }
            // Sadece Yüzde Şans Gösterimi
            Text(
                "%${"%.1f".format(skor.kazanmaTahmini)}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}