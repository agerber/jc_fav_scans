package edu.uchicago.gerber.favs.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.uchicago.gerber.favs.common.Constants
import edu.uchicago.gerber.favs.data.models.Pokemon
import edu.uchicago.gerber.favs.data.models.PokemonResponse
import edu.uchicago.gerber.favs.data.models.PokemonSpeciesResponse
import edu.uchicago.gerber.favs.data.repository.ApiProvider
import edu.uchicago.gerber.favs.data.repository.PokemonRepository
import edu.uchicago.gerber.favs.presentation.screens.details.SearchOperation
import edu.uchicago.gerber.favs.presentation.screens.details.SearchState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class PokemonViewModel : ViewModel() {

    private val pokemonRepository: PokemonRepository = PokemonRepository(ApiProvider.pokemonApi())

    private val _searchState = mutableStateOf(SearchState())
    val searchState: State<SearchState> = _searchState

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _pokemon = mutableStateOf(Constants.pokemon)
    val pokemon: State<Pokemon> = _pokemon

    private val _confidence = mutableStateOf(75)
    val confidence: State<Int> = _confidence

    //////////////////////////////////////////
    // FUNCTIONS
    //////////////////////////////////////////

    fun setConfidence(confidence: Int){
        _confidence.value = confidence
    }

    fun setName(name: String){
        _name.value = name
    }

    fun setPokemon(name: String) = viewModelScope.launch {
        _searchState.value = SearchState(SearchOperation.LOADING, "")
        try {

            //the data we want to populate our pokemon model comes from two different endpoints
            //so we queue-up two separate deferred objects
            val pokemonResponseDeferred = async {
                pokemonRepository.getPokemon(name)
            }
            val pokemonSpeciesResponseDeferred = async {
                pokemonRepository.getPokemonSpecies(name)
            }

            //awaitAll will execute both network-operations and wait for both coroutines to finish
            val (pokemonResponseAny, pokemonSpeciesResponseAny) = awaitAll(pokemonResponseDeferred , pokemonSpeciesResponseDeferred)

            //awaitAll will store the returned objects in an Any superclass reference (roughly equivalent to Object in java) if the
            // objects specified in right parentheses are of different types (which they are), so we must explicitly cast
            val pokemonResponse: PokemonResponse = pokemonResponseAny as PokemonResponse
            val pokemonSpeciesResponse: PokemonSpeciesResponse = pokemonSpeciesResponseAny as PokemonSpeciesResponse

            //now we cobble together our pokemon object from both responses above
            //note: if you are fetching from only one endpoint, then you can use a simple deferred with async/await. See https://github.com/agerber/jc_fav_books for example
            val pokemon = Pokemon(
                name = name,
                image = pokemonResponse.imageUrl,

                description = pokemonSpeciesResponse
                    .flavorTextEntries
                    .filter { it.language.name == "en" }
                    .random()
                    .flavorText
                    .replace("\n", " ")
                    .replace("\\f", " "),

                weight = pokemonResponse.weight,
                baseExperience = pokemonResponse.baseExperience,
                baseHappiness = pokemonSpeciesResponse.baseHappiness
            )
            _pokemon.value = pokemon
            _searchState.value = SearchState(SearchOperation.DONE, "")

        } catch (e: Exception){
            _searchState.value = SearchState(SearchOperation.ERROR, e.message.toString())
        }

    }
}