package edu.uchicago.gerber.favs.data.repository

import edu.uchicago.gerber.favs.data.models.PokemonResponse
import edu.uchicago.gerber.favs.data.models.PokemonSpeciesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PokemonApi {


    @GET(value = "pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String
    ): PokemonResponse

    @GET(value = "pokemon-species/{name}")
    suspend fun getPokemonSpecies(
        @Path("name") name: String
    ): PokemonSpeciesResponse

}