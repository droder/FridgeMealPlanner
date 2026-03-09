package com.example.fridgemealplanner

data class MealIdea(
    val name: String,
    val ingredients: List<String>,
    val type: String,
    val highProtein: Boolean = false,
    val lowCarb: Boolean = false,
    val vegetarian: Boolean = false
