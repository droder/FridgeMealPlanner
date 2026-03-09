package com.example.fridgemealplanner

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavType
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
                onRecipeClick = { recipeId ->
                    navController.navigate("recipe/$recipeId")
                }
            )
        }

        composable(
            route = "recipe/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0
            RecipeDetailScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onRecipeClick: (Int) -> Unit
) {
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var detectedItems by remember {
        mutableStateOf(listOf("chicken", "broccoli", "carrots"))
    }

    var isAnalyzing by remember { mutableStateOf(false) }

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    val suggestions = remember(detectedItems) {
        RecipeRepository.suppers
            .map { recipe ->
                val owned = recipe.ingredients.filter { detectedItems.contains(it) }
                val missing = recipe.ingredients.filterNot { detectedItems.contains(it) }
                SupperSuggestion(recipe, owned, missing)
            }
            .filter { it.owned.isNotEmpty() }
            .sortedByDescending { it.owned.size }
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
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
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
                    isAnalyzing = true

                    // Temporary fake detection for testing.
                    // Later this will be replaced with real AI image analysis.
                    detectedItems = listOf("chicken", "broccoli", "carrots", "rice")

                    isAnalyzing = false
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageUri != null
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
                        detectedItems.forEach {
                            Text("• $it")
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

        items(suggestions) { suggestion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecipeClick(suggestion.recipe.id) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = suggestion.recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("You have: ${suggestion.owned.joinToString(", ")}")
                    Text(
                        "Missing: ${
                            if (suggestion.missing.isEmpty()) "Nothing"
                            else suggestion.missing.joinToString(", ")
                        }"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap to view full recipe")
                }
            }
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit
) {
    val detectedItems = listOf("chicken", "broccoli", "carrots", "rice")

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
                        owned.forEach { Text("• $it") }
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
                        missing.forEach { Text("• $it") }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.ingredients.forEach { Text("• $it") }
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
