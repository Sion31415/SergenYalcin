package com.mustafaburak.sergenyalcn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mustafaburak.sergenyalcn.ui.navigation.Screen
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfaScreen(navController: NavController, factory: ViewModelFactory) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🐎 Sergen Yalcin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ne yapmak istersiniz?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnaSayfaKart(
                        baslik = "Koşular",
                        aciklama = "Koşu ekle ve yönet",
                        emoji = "🏁",
                        onClick = { navController.navigate(Screen.KosuListesi.route) }
                    )
                }
                item {
                    AnaSayfaKart(
                        baslik = "Atlar",
                        aciklama = "At bilgilerini yönet",
                        emoji = "🐴",
                        onClick = { navController.navigate(Screen.AtListesi.route) }
                    )
                }
                item {
                    AnaSayfaKart(
                        baslik = "Tahmin",
                        aciklama = "Kupon tahminleri al",
                        emoji = "🎯",
                        onClick = { navController.navigate(Screen.Tahmin.route) }
                    )
                }
                item {
                    AnaSayfaKart(
                        baslik = "İstatistik",
                        aciklama = "Başarı oranlarını gör",
                        emoji = "📊",
                        onClick = { navController.navigate(Screen.Istatistik.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnaSayfaKart(
    baslik: String,
    aciklama: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = baslik,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = aciklama,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}