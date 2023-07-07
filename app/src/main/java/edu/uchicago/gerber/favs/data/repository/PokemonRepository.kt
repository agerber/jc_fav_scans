package edu.uchicago.gerber.favs.data.repository

import edu.uchicago.gerber.favs.data.models.PokemonResponse
import edu.uchicago.gerber.favs.data.models.PokemonSpeciesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository (private val pokemonApi: PokemonApi) {


    suspend fun getPokemon(
        name: String
    ): PokemonResponse {
        return withContext(Dispatchers.IO) {
            pokemonApi.getPokemon(
                name = name,
            )
        }
    }

    suspend fun getPokemonSpecies(
        name: String
    ): PokemonSpeciesResponse {
        return withContext(Dispatchers.IO) {
            pokemonApi.getPokemonSpecies(
                name = name,
            )
        }
    }
}