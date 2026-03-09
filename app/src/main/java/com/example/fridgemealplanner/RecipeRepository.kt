package com.example.fridgemealplanner

object RecipeRepository {
    val recipes = listOf(
        MealIdea(
            name = "Veggie Omelette",
            ingredients = listOf("eggs", "spinach", "cheese", "onion"),
            type = "Breakfast",
            highProtein = true,
            lowCarb = true,
            vegetarian = true
        ),
        MealIdea(
            name = "Greek Yogurt Bowl",
            ingredients = listOf("greek yogurt", "banana", "berries"),
            type = "Breakfast",
            highProtein = true,
            vegetarian = true
        ),
        MealIdea(
            name = "Overnight Oats",
            ingredients = listOf("oats", "milk", "banana"),
            type = "Breakfast",
            vegetarian = true
        ),
        MealIdea(
            name = "Chicken Rice Bowl",
            ingredients = listOf("chicken", "rice", "broccoli"),
            type = "Lunch",
            highProtein = true
        ),
        MealIdea(
            name = "Turkey Wrap",
            ingredients = listOf("tortilla", "turkey", "lettuce", "tomato"),
            type = "Lunch",
            highProtein = true
        ),
        MealIdea(
            name = "Chicken Salad",
            ingredients = listOf("chicken", "lettuce", "cucumber", "tomato"),
            type = "Lunch",
            highProtein = true,
            lowCarb = true
        ),
        MealIdea(
            name = "Bean Chili",
            ingredients = listOf("beans", "tomato", "onion", "pepper"),
            type = "Dinner",
            vegetarian = true
        ),
        MealIdea(
            name = "Salmon Dinner",
            ingredients = listOf("salmon", "broccoli", "sweet potato"),
            type = "Dinner",
            highProtein = true,
            lowCarb = true
        ),
        MealIdea(
            name = "Stir Fry",
            ingredients = listOf("chicken", "broccoli", "pepper", "rice"),
            type = "Dinner",
            highProtein = true
        )
    )
}
