package com.example.fridgemealplanner

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onRecipeClick = { recipeId, detectedItems ->
                    val encodedItems = detectedItems.joinToString("|")
                    navController.navigate("recipe/$recipeId/$encodedItems")
                }
            )
        }

        composable(
            route = "recipe/{recipeId}/{detectedItems}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.IntType },
                navArgument("detectedItems") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0
            val detectedItemsArg = backStackEntry.arguments?.getString("detectedItems").orEmpty()
            val detectedItems = if (detectedItemsArg.isBlank()) {
                emptyList()
            } else {
                detectedItemsArg.split("|").map { it.trim() }.filter { it.isNotBlank() }
            }

            RecipeDetailScreen(
                recipeId = recipeId,
                detectedItems = detectedItems,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onRecipeClick: (Int, List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var detectedItems by remember { mutableStateOf(emptyList<String>()) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        detectedItems = emptyList()
    }

    val suggestions = remember(detectedItems) {
        RecipeRepository.suppers
            .map { recipe ->
                val owned = recipe.ingredients.filter { ingredient ->
                    detectedItems.contains(ingredient)
                }
                val missing = recipe.ingredients.filterNot { ingredient ->
                    detectedItems.contains(ingredient)
                }
                SupperSuggestion(recipe, owned, missing)
            }
            .filter { it.owned.isNotEmpty() }
            .sortedWith(
                compareByDescending<SupperSuggestion> { it.owned.size }
                    .thenBy { it.missing.size }
                    .thenBy { it.recipe.name }
            )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Fridge Supper Finder",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Pick a fridge photo, detect items, and get supper ideas.")
        }

        item {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Fridge Photo")
            }
        }

        item {
            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected fridge image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        item {
            Button(
                onClick = {
                    val uri = selectedImageUri ?: return@Button
                    scope.launch {
                        isAnalyzing = true
                        detectedItems = VisionHelper.detectItemsFromImage(context, uri)
                        isAnalyzing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageUri != null && !isAnalyzing
            ) {
                Text(if (isAnalyzing) "Analyzing..." else "Analyze Fridge")
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Detected Items", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (detectedItems.isEmpty()) {
                        Text("No items detected yet.")
                    } else {
                        detectedItems.forEach { item ->
                            Text("• $item")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Suggested Suppers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (suggestions.isEmpty()) {
            item {
                Text("No supper matches yet. Pick a photo and analyze it.")
            }
        } else {
            items(suggestions) { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecipeClick(suggestion.recipe.id, detectedItems) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = suggestion.recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You have: " + if (suggestion.owned.isEmpty()) {
                                "Nothing"
                            } else {
                                suggestion.owned.joinToString(", ")
                            }
                        )
                        Text(
                            text = "Missing: " + if (suggestion.missing.isEmpty()) {
                                "Nothing"
                            } else {
                                suggestion.missing.joinToString(", ")
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tap to view full recipe")
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    detectedItems: List<String>,
    onBack: () -> Unit
) {
    val recipe = RecipeRepository.suppers.firstOrNull { it.id == recipeId }

    if (recipe == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Recipe not found.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        return
    }

    val owned = recipe.ingredients.filter { detectedItems.contains(it) }
    val missing = recipe.ingredients.filterNot { detectedItems.contains(it) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(onClick = onBack) {
                Text("Back")
            }
        }

        item {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("You Have", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (owned.isEmpty()) {
                        Text("Nothing")
                    } else {
                        owned.forEach { item ->
                            Text("• $item")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Missing", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (missing.isEmpty()) {
                        Text("Nothing")
                    } else {
                        missing.forEach { item ->
                            Text("• $item")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.ingredients.forEach { ingredient ->
                        Text("• $ingredient")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Directions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.directions.forEachIndexed { index, step ->
                        Text("${index + 1}. $step")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
