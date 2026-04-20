package com.mustafaburak.sergenyalcn.ui.navigation

sealed class Screen(val route: String) {
    object AnaSayfa : Screen("ana_sayfa")
    object AtListesi : Screen("at_listesi")
    object AtEkle : Screen("at_ekle")
    object KosuListesi : Screen("kosu_listesi")
    object KosuEkle : Screen("kosu_ekle")
    object KosuDetay : Screen("kosu_detay/{kosuId}") {
        fun createRoute(kosuId: Int) = "kosu_detay/$kosuId"
    }
    object Tahmin : Screen("tahmin")
    object Istatistik : Screen("istatistik")
}