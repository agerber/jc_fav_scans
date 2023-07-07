package edu.uchicago.gerber.favs.presentation.screens.search.paging

data class Paginate(
    val query: String = "",
    val maxResult: Int = 10,
    val startIndex: Int = 1,
)
