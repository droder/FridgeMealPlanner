package com.example.fridgemealplanner

import android.content.Context
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
                FridgeMealPlannerApp(context = applicationContext)
            }
        }
    }
}

data class PlannedMeal(
    val day: Int,
    val mealType: String,
    val meal: MealIdea,
    val owned: List<String>,
    val missing: List<String>
)

private const val PREFS_NAME = "fridge_meal_planner"
private const val KEY_FRIDGE_ITEMS = "fridge_items"

private fun saveFridgeItems(context: Context, items: List<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_FRIDGE_ITEMS, items.joinToString("||"))
        .apply()
}

private fun loadFridgeItems(context: Context): List<String> {
    val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_FRIDGE_ITEMS, "")
        .orEmpty()

    if (saved.isBlank()) return emptyList()

    return saved.split("||")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

private fun filterRecipes(
    highProtein: Boolean,
    lowCarb: Boolean,
    vegetarian: Boolean
): List<MealIdea> {
    return RecipeRepository.recipes.filter { recipe ->
        (!highProtein || recipe.highProtein) &&
        (!lowCarb || recipe.lowCarb) &&
        (!vegetarian || recipe.vegetarian)
    }
}

private fun buildMealPlan(
    fridgeItems: List<String>,
    recipes: List<MealIdea>
): Pair<List<PlannedMeal>, List<String>> {
    val normalizedFridge = fridgeItems.map { it.lowercase() }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner")
    val plan = mutableListOf<PlannedMeal>()
    val groceryList = linkedSetOf<String>()

    for (day in 1..7) {
        for (mealType in mealTypes) {
            val candidates = recipes
                .filter { it.type == mealType }
                .sortedByDescending { recipe ->
                    recipe.ingredients.count { ingredient ->
                        normalizedFridge.contains(ingredient.lowercase())
                    }
                }

            val chosen = candidates.firstOrNull() ?: continue
            val owned = chosen.ingredients.filter { ingredient ->
                normalizedFridge.contains(ingredient.lowercase())
            }
            val missing = chosen.ingredients.filterNot { ingredient ->
                normalizedFridge.contains(ingredient.lowercase())
            }

            groceryList.addAll(missing)
            plan.add(
                PlannedMeal(
                    day = day,
                    mealType = mealType,
                    meal = chosen,
                    owned = owned,
                    missing = missing
                )
            )
        }
    }

    return plan to groceryList.toList()
}

@Composable
fun FridgeMealPlannerApp(context: Context) {
    var foodInput by remember { mutableStateOf("") }
    var fridgeItems by remember { mutableStateOf(loadFridgeItems(context)) }
    var highProteinOnly by remember { mutableStateOf(false) }
    var lowCarbOnly by remember { mutableStateOf(false) }
    var vegetarianOnly by remember { mutableStateOf(false) }

    val filteredRecipes = remember(highProteinOnly, lowCarbOnly, vegetarianOnly) {
        filterRecipes(
            highProtein = highProteinOnly,
            lowCarb = lowCarbOnly,
            vegetarian = vegetarianOnly
        )
    }

    val planAndGroceries = remember(fridgeItems, filteredRecipes) {
        buildMealPlan(fridgeItems, filteredRecipes)
    }
    val plannedMeals = planAndGroceries.first
    val groceryList = planAndGroceries.second

    fun persist(items: List<String>) {
        fridgeItems = items
        saveFridgeItems(context, items)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Fridge Meal Planner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Save your fridge items, filter healthy meals, and generate a 7-day meal plan.")
            }

            item {
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
                            persist(fridgeItems + item)
                            foodInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Item")
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Healthy Filters", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = highProteinOnly,
                                onClick = { highProteinOnly = !highProteinOnly },
                                label = { Text("High protein") }
                            )
                            FilterChip(
                                selected = lowCarbOnly,
                                onClick = { lowCarbOnly = !lowCarbOnly },
                                label = { Text("Low carb") }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FilterChip(
                            selected = vegetarianOnly,
                            onClick = { vegetarianOnly = !vegetarianOnly },
                            label = { Text("Vegetarian") }
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Saved Fridge Items", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (fridgeItems.isEmpty()) {
                            Text("No items saved yet.")
                        } else {
                            fridgeItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item)
                                    TextButton(onClick = {
                                        persist(fridgeItems - item)
                                    }) {
                                        Text("Remove")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            item {
                Text("7-Day Meal Plan", style = MaterialTheme.typography.titleLarge)
            }

            items(plannedMeals) { planned ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Day ${planned.day} • ${planned.mealType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(planned.meal.name)
                        Text("Ingredients: ${planned.meal.ingredients.joinToString(", ")}")
                        Text("You have: ${planned.owned.joinToString(", ").ifBlank { "None" }}")
                        Text("Missing: ${planned.missing.joinToString(", ").ifBlank { "Nothing" }}")
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Grocery List", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (groceryList.isEmpty()) {
                            Text("You have everything needed for the current plan.")
                        } else {
                            groceryList.forEach { item ->
                                Text("• $item")
                            }
                        }
                    }
                }
            }
        }
    }
}
