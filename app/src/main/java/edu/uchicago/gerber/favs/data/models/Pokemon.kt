package edu.uchicago.gerber.favs.data.models

data class Pokemon(
    val name: String,
    val description: String,
    val image: String,
    val weight: Int,
    val baseExperience: Int,
    val baseHappiness: Int
)