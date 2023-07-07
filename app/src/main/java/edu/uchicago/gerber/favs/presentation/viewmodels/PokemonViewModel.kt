package edu.uchicago.gerber.favs.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PokemonViewModel : ViewModel() {

    private val _confidence = mutableStateOf(75)
    val confidence: State<Int> = _confidence

    //////////////////////////////////////////
    // FUNCTIONS
    //////////////////////////////////////////

    fun setConfidence(confidence: Int){
        _confidence.value = confidence
    }
}