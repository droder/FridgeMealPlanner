package com.example.fridgemealplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FridgeMealPlannerApp()
            }
        }
    }
}

@Composable
fun FridgeMealPlannerApp() {
    var foodInput by remember { mutableStateOf("") }
    var fridgeItems by remember { mutableStateOf(listOf<String>()) }
    var mealIdeas by remember { mutableStateOf(listOf<MealIdea>()) }

    fun generateMealIdeas() {
        mealIdeas = RecipeRepository.recipes
            .map { recipe ->
                recipe to recipe.ingredients.count { ingredient ->
                    fridgeItems.contains(ingredient)
                }
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Fridge Meal Planner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add the food you have and get healthy meal ideas.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = foodInput,
                onValueChange = { foodInput = it },
                label = { Text("Add fridge item") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val item = foodInput.trim().lowercase()
                    if (item.isNotEmpty() && !fridgeItems.contains(item)) {
                        fridgeItems = fridgeItems + item
                        foodInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Item")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Fridge Items", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                items(fridgeItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item)
                        TextButton(onClick = {
                            fridgeItems = fridgeItems - item
                        }) {
                            Text("Remove")
                        }
                    }
                    Divider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { generateMealIdeas() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Healthy Meals")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Meal Suggestions", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(mealIdeas) { meal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = meal.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Type: ${meal.type}")
                            Text("Ingredients: ${meal.ingredients.joinToString(", ")}")
                            val owned = meal.ingredients.filter { fridgeItems.contains(it) }
                            Text("You have: ${owned.joinToString(", ")}")
                        }
                    }
                }
            }
        }
    }
}
