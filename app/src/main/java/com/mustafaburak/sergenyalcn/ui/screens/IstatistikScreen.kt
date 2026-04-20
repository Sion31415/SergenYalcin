package com.mustafaburak.sergenyalcn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mustafaburak.sergenyalcn.data.db.entity.KosuSonucu
import com.mustafaburak.sergenyalcn.ui.viewmodel.AtViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.KosuSonucuViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.KosuViewModel
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstatistikScreen(navController: NavController, factory: ViewModelFactory) {
    val atViewModel: AtViewModel = viewModel(factory = factory)
    val kosuViewModel: KosuViewModel = viewModel(factory = factory)
    val sonucViewModel: KosuSonucuViewModel = viewModel(factory = factory)

    val atlar by atViewModel.atlar.collectAsState()
    val kosular by kosuViewModel.kosular.collectAsState()
    val sonuclar by sonucViewModel.sonuclar.collectAsState()

    // Jokey istatistikleri
    val jokeyIstatistikleri = sonuclar
        .groupBy { it.jokey }
        .map { (jokey, sonucListesi) ->
            val toplam = sonucListesi.size
            val birinci = sonucListesi.count { it.sonuc == 1 }
            val ilkUc = sonucListesi.count { it.sonuc <= 3 }
            JokeyIstatistik(
                isim = jokey,
                toplamKosu = toplam,
                birinciler = birinci,
                ilkUcler = ilkUc,
                basariOrani = if (toplam > 0) birinci.toFloat() / toplam * 100 else 0f
            )
        }
        .sortedByDescending { it.basariOrani }
        .take(10)

    // Antrenör istatistikleri
    val antrenorIstatistikleri = sonuclar
        .groupBy { it.antrenor }
        .map { (antrenor, sonucListesi) ->
            val toplam = sonucListesi.size
            val birinci = sonucListesi.count { it.sonuc == 1 }
            val ilkUc = sonucListesi.count { it.sonuc <= 3 }
            AntrenorIstatistik(
                isim = antrenor,
                toplamKosu = toplam,
                birinciler = birinci,
                ilkUcler = ilkUc,
                basariOrani = if (toplam > 0) birinci.toFloat() / toplam * 100 else 0f
            )
        }
        .sortedByDescending { it.basariOrani }
        .take(10)

    var secilenTab by remember { mutableIntStateOf(0) }
    val tablar = listOf("Genel", "Jokeyler", "Antrenörler")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 İstatistikler") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = secilenTab) {
                tablar.forEachIndexed { index, baslik ->
                    Tab(
                        selected = secilenTab == index,
                        onClick = { secilenTab = index },
                        text = { Text(baslik) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (secilenTab) {
                    0 -> {
                        item {
                            GenelIstatistikler(
                                toplamAt = atlar.size,
                                toplamKosu = kosular.size,
                                toplamSonuc = sonuclar.size
                            )
                        }
                    }
                    1 -> {
                        item {
                            Text(
                                text = "En Başarılı Jokeyler",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        items(jokeyIstatistikleri.size) { index ->
                            JokeyKarti(istatistik = jokeyIstatistikleri[index], sira = index + 1)
                        }
                    }
                    2 -> {
                        item {
                            Text(
                                text = "En Başarılı Antrenörler",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        items(antrenorIstatistikleri.size) { index ->
                            AntrenorKarti(istatistik = antrenorIstatistikleri[index], sira = index + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenelIstatistikler(toplamAt: Int, toplamKosu: Int, toplamSonuc: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Genel Özet",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IstatistikKutu(
                modifier = Modifier.weight(1f),
                baslik = "Toplam At",
                deger = toplamAt.toString(),
                emoji = "🐴"
            )
            IstatistikKutu(
                modifier = Modifier.weight(1f),
                baslik = "Toplam Koşu",
                deger = toplamKosu.toString(),
                emoji = "🏁"
            )
            IstatistikKutu(
                modifier = Modifier.weight(1f),
                baslik = "Toplam Sonuç",
                deger = toplamSonuc.toString(),
                emoji = "📋"
            )
        }
        if (toplamSonuc == 0) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz veri yok.\nKoşu sonuçları girdikçe istatistikler burada görünecek.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IstatistikKutu(modifier: Modifier, baslik: String, deger: String, emoji: String) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Text(
                text = deger,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = baslik,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun JokeyKarti(istatistik: JokeyIstatistik, sira: Int) {
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
            Text(
                text = "$sira.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = istatistik.isim, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "${istatistik.toplamKosu} koşu • ${istatistik.birinciler} 1. • ${istatistik.ilkUcler} ilk 3",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "%${"%.1f".format(istatistik.basariOrani)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AntrenorKarti(istatistik: AntrenorIstatistik, sira: Int) {
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
            Text(
                text = "$sira.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = istatistik.isim, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "${istatistik.toplamKosu} koşu • ${istatistik.birinciler} 1. • ${istatistik.ilkUcler} ilk 3",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "%${"%.1f".format(istatistik.basariOrani)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Yardımcı veri sınıfları
data class JokeyIstatistik(
    val isim: String,
    val toplamKosu: Int,
    val birinciler: Int,
    val ilkUcler: Int,
    val basariOrani: Float
)

data class AntrenorIstatistik(
    val isim: String,
    val toplamKosu: Int,
    val birinciler: Int,
    val ilkUcler: Int,
    val basariOrani: Float
)