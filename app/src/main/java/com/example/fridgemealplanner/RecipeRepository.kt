package com.example.fridgemealplanner

object RecipeRepository {
    val suppers = listOf(
        SupperRecipe(
            id = 1,
            name = "Chicken Stir Fry",
            ingredients = listOf("chicken", "broccoli", "carrots", "rice", "soy sauce"),
            directions = listOf(
                "Cut the chicken and vegetables into bite-sized pieces.",
                "Cook the rice according to package directions.",
                "Cook the chicken in a pan until fully done.",
                "Add broccoli and carrots to the pan.",
                "Stir in soy sauce and cook for a few more minutes.",
                "Serve over rice."
            )
        ),
        SupperRecipe(
            id = 2,
            name = "Omelette",
            ingredients = listOf("eggs", "cheese", "onion", "pepper"),
            directions = listOf(
                "Beat the eggs in a bowl.",
                "Dice the onion and pepper.",
                "Cook the onion and pepper in a pan until soft.",
                "Pour in the eggs.",
                "Add cheese and fold the omelette once set.",
                "Cook until done and serve."
            )
        ),
        SupperRecipe(
            id = 3,
            name = "Bean Chili",
            ingredients = listOf("beans", "tomato", "onion", "pepper"),
            directions = listOf(
                "Chop the onion and pepper.",
                "Cook onion and pepper in a pot until soft.",
                "Add beans and tomato.",
                "Simmer for 15 to 20 minutes.",
                "Serve hot."
            )
        ),
        SupperRecipe(
            id = 4,
            name = "Salmon and Veggies",
            ingredients = listOf("salmon", "broccoli", "sweet potato"),
            directions = listOf(
                "Preheat oven to 400 degrees F.",
                "Cut sweet potato into pieces.",
                "Place salmon and vegetables on a tray.",
                "Bake until salmon is cooked and vegetables are tender.",
                "Serve warm."
            )
        ),
        SupperRecipe(
            id = 5,
            name = "Chicken Salad",
            ingredients = listOf("chicken", "lettuce", "cucumber", "tomato"),
            directions = listOf(
                "Cook or shred the chicken.",
                "Chop the lettuce, cucumber, and tomato.",
                "Combine everything in a bowl.",
                "Add dressing if desired.",
                "Serve chilled."
            )
        ),
        SupperRecipe(
            id = 6,
            name = "Fried Rice",
            ingredients = listOf("rice", "eggs", "carrots", "onion", "soy sauce"),
            directions = listOf(
                "Cook the rice and let it cool slightly.",
                "Chop carrots and onion.",
                "Cook vegetables in a pan until softened.",
                "Add eggs and scramble them in the pan.",
                "Stir in rice and soy sauce.",
                "Cook until heated through and serve."
            )
        )
    )
}
