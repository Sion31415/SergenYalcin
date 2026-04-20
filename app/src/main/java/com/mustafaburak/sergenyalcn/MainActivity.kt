package com.mustafaburak.sergenyalcn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mustafaburak.sergenyalcn.data.repository.AtRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuRepository
import com.mustafaburak.sergenyalcn.data.repository.KosuSonucuRepository
import com.mustafaburak.sergenyalcn.ui.navigation.Screen
import com.mustafaburak.sergenyalcn.ui.screens.AnaSayfaScreen
import com.mustafaburak.sergenyalcn.ui.screens.AtListesiScreen
import com.mustafaburak.sergenyalcn.ui.screens.KosuListesiScreen
import com.mustafaburak.sergenyalcn.ui.screens.TahminScreen
import com.mustafaburak.sergenyalcn.ui.screens.IstatistikScreen
import com.mustafaburak.sergenyalcn.ui.theme.SergenYalcinTheme
import com.mustafaburak.sergenyalcn.ui.viewmodel.ViewModelFactory
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.mustafaburak.sergenyalcn.ui.screens.KosuDetayScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SergenApp
        val factory = ViewModelFactory(
            AtRepository(app.database.atDao()),
            KosuRepository(app.database.kosuDao()),
            KosuSonucuRepository(app.database.kosuSonucuDao()),
            applicationContext // bunu ekle
        )


        setContent {
            SergenYalcinTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.AnaSayfa.route
                ) {

                    composable(
                        route = Screen.KosuDetay.route,
                        arguments = listOf(navArgument("kosuId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val kosuId = backStackEntry.arguments?.getInt("kosuId") ?: 0
                        KosuDetayScreen(navController, factory, kosuId)
                    }

                    composable(Screen.AnaSayfa.route) {
                        AnaSayfaScreen(navController, factory)
                    }
                    composable(Screen.AtListesi.route) {
                        AtListesiScreen(navController, factory)
                    }
                    composable(Screen.KosuListesi.route) {
                        KosuListesiScreen(navController, factory)
                    }
                    composable(Screen.Tahmin.route) {
                        TahminScreen(navController, factory)
                    }
                    composable(Screen.Istatistik.route) {
                        IstatistikScreen(navController, factory)
                    }
                }
            }
        }
    }
}