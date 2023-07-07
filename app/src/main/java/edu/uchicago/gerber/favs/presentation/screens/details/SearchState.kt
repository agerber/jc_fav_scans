package edu.uchicago.gerber.favs.presentation.screens.details

data class SearchState(
    val searchOperation: SearchOperation = SearchOperation.INITIAL,
    val errorMessage: String = ""
)

enum class SearchOperation { LOADING, INITIAL, DONE, ERROR }