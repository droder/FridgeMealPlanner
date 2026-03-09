package com.example.fridgemealplanner

data class SupperRecipe(
    val id: Int,
    val name: String,
    val ingredients: List<String>,
    val directions: List<String>
)
