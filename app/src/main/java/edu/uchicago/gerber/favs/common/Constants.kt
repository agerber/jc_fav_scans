package edu.uchicago.gerber.favs.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.uchicago.gerber.favs.data.models.Pokemon

object Constants {

    val modifier = Modifier.padding(paddingValues = PaddingValues(all = 0.dp))

    const val pokemonAPIBaseURL = "https://pokeapi.co/api/v2/"
    val pokemon = Pokemon(
        name= "bulbasaur",
        description= "some description",
        image= "https://someimage.html",
        weight= 50,
        baseExperience= 20,
        baseHappiness=12
    )

}