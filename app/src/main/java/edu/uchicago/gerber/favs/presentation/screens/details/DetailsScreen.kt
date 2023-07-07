package edu.uchicago.gerber.favs.presentation.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.uchicago.gerber.favs.R
import edu.uchicago.gerber.favs.presentation.viewmodels.PokemonViewModel
import com.skydoves.landscapist.glide.GlideImage
import edu.uchicago.gerber.favs.data.models.Pokemon
import edu.uchicago.gerber.favs.ui.theme.HunterGreen
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavController,
    pokemonViewModel: PokemonViewModel
) {
    //this method allows us to fire a coroutine upon launch of this Composable
    LaunchedEffect(pokemonViewModel.name) {
        if (pokemonViewModel.name.value.isNotBlank()) {
            pokemonViewModel.setPokemon(pokemonViewModel.name.value)
        }
    }

    val pokemon = pokemonViewModel.pokemon.value
    val state = pokemonViewModel.searchState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        pokemon.name.replaceFirstChar {
                            it.titlecase(
                                Locale.ENGLISH
                            )
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(paddingValues = PaddingValues( horizontal = 16.dp))
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = colorResource(id = R.color.text)
                ),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp, 24.dp)
                            .clickable {
                                navController.navigateUp()
                            },
                        tint = colorResource(id = R.color.text)
                    )
                }
            )
        },
        content = { paddingValues ->
            Box(Modifier.fillMaxSize()) {
                when (state.searchOperation) {
                    SearchOperation.LOADING -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }

                    SearchOperation.DONE -> {
                        DetailsView(pokemon, navController, paddingValues)
                    }

                    SearchOperation.ERROR -> {
                        Text(
                            text = state.errorMessage,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {}
                }
            }
        }
    )
}

@Composable
fun DetailsView(
    pokemon: Pokemon,
    navController: NavController,
    paddingValues: PaddingValues
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background))
            .padding(paddingValues)
    ) {

        val interiorPadding = PaddingValues(16.dp, 0.dp, 16.dp, 0.dp)

        GlideImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            imageModel = pokemon.image,
            alignment = Alignment.Center,
            contentDescription = "",
            contentScale = ContentScale.FillHeight
        )


        Spacer(modifier = Modifier.height(24.dp))
        Title(
            title = "Description"
        )
        Text(
            text = pokemon.description,
            modifier = Modifier
                .fillMaxWidth()
                .padding(interiorPadding),
            color = colorResource(id = R.color.text),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false),
        ) {
            Title(title = "Pokemon info")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(interiorPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoCard(title = "Base Happiness", value = pokemon.baseHappiness.toString())
                InfoCard(title = "Base Experience", value = pokemon.baseExperience.toString())
                InfoCard(title = "Weight", value = pokemon.weight.toString())
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(

                modifier = Modifier
                    .padding(interiorPadding)
                    .fillMaxWidth())
            {
                Button(
                    shape = RectangleShape,
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = { navController.navigateUp() }) {
                    Text(text = "Dismiss")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    shape = RectangleShape,
                    modifier = Modifier
                        .weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = HunterGreen),
                    onClick = {
                        //todo: future use. For example, may add a pokemon (or other model) to your service on AWS

                    }) {
                    Text(text = "Add to Favorites")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }


    }
}


@Composable
fun Title(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp, 0.dp, 0.dp),
        color = colorResource(id = R.color.text),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.W600,
        textAlign = TextAlign.Start
    )
}