package com.example.fridgemealplanner

object RecipeRepository {
    val recipes = listOf(
        MealIdea("Veggie Omelette", listOf("eggs", "spinach", "cheese", "onion"), "Breakfast"),
        MealIdea("Chicken Rice Bowl", listOf("chicken", "rice", "broccoli"), "Lunch"),
        MealIdea("Greek Yogurt Bowl", listOf("greek yogurt", "banana", "berries"), "Breakfast"),
        MealIdea("Turkey Wrap", listOf("tortilla", "turkey", "lettuce", "tomato"), "Lunch"),
        MealIdea("Salmon Dinner", listOf("salmon", "broccoli", "sweet potato"), "Dinner"),
        MealIdea("Bean Chili", listOf("beans", "tomato", "onion", "pepper"), "Dinner"),
        MealIdea("Chicken Salad", listOf("chicken", "lettuce", "cucumber", "tomato"), "Lunch"),
        MealIdea("Overnight Oats", listOf("oats", "milk", "banana"), "Breakfast")
    )
}
