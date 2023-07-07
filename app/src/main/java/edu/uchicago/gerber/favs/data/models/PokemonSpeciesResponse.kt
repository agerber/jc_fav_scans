package edu.uchicago.gerber.favs.data.models

import com.google.gson.annotations.SerializedName

data class PokemonSpeciesResponse(
    @SerializedName("flavor_text_entries")
    val flavorTextEntries: List<FlavorTextEntry>,
    @SerializedName("base_happiness")
    val baseHappiness: Int
)
