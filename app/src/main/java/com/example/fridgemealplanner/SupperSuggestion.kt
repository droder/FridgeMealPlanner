package com.example.fridgemealplanner

data class SupperSuggestion(
    val recipe: SupperRecipe,
    val owned: List<String>,
    val missing: List<String>
)
