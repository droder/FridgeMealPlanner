package com.example.fridgemealplanner

data class MealIdea(
    val name: String,
    val ingredients: List<String>,
    val type: String,
    val healthy: Boolean = true
)
