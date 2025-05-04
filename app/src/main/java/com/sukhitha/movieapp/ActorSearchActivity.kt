package com.sukhitha.movieapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class ActorSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                ActorSearchScreen()
            }
        }
    }
}

@Composable
fun ActorSearchScreen() {
    var actorName by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var message by remember { mutableStateOf("") } // For feedback
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = actorName,
            onValueChange = { actorName = it.trim() }, // Trim input
            label = { Text("Actor Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d("ActorSearch", "Searching for actor: '$actorName'")
                scope.launch {
                    try {
                        searchResults = db.movieDao().searchByActor(actorName)
                        Log.d("ActorSearch", "Query: '$actorName', Results: ${searchResults.size}")
                        searchResults.forEach {
                            Log.d("ActorSearch", "Found movie: ${it.title}, Actors: ${it.actors}")
                        }
                        message = if (searchResults.isNotEmpty()) {
                            "Found ${searchResults.size} actors for '$actorName'"
                        } else {
                            "No movies found for '$actorName'"
                        }
                    } catch (e: Exception) {
                        Log.e("ActorSearch", "Error searching for actor: ${e.message}")
                        message = "Error searching: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = actorName.isNotBlank()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = if (message.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
        ) {
            if (searchResults.isNotEmpty()) {
                searchResults.forEach { movie ->
                    Text("Title: ${movie.title}", fontWeight = FontWeight.Bold)
                    Text("Year: ${movie.year}")
                    Text("Actors: ${movie.actors}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}