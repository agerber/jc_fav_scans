package edu.uchicago.gerber.favs.presentation.navigation

import edu.uchicago.gerber.favs.R

sealed class Screen(var route: String, var icon: Int, var title: String) {
    object Scan : Screen("scan", R.drawable.ic_scan, "Scan")
    object Favorites : Screen("favorites", R.drawable.ic_favorite, "Favorites")
    object Contact : Screen("contact", R.drawable.ic_contact, "Contact")
    object Detail : Screen("detail", 0, "Detail")
}