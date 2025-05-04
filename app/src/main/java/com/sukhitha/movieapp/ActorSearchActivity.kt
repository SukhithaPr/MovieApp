package com.sukhitha.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorSearchScreen() {
    var actorName by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Actors", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = actorName,
                onValueChange = { actorName = it.trim() },
                label = { Text("Actor Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Enter actor name to search" },
                singleLine = true,
                isError = message.contains("Error") && actorName.isNotBlank(),
                supportingText = {
                    if (message.contains("Error") && actorName.isNotBlank()) {
                        Text("Check input or try again", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        message = ""
                        searchResults = emptyList()
                        try {
                            searchResults = db.movieDao().searchByActor(actorName)
                            message = if (searchResults.isNotEmpty()) {
                                "Found ${searchResults.size} movies for '$actorName'"
                            } else {
                                "No movies found for '$actorName'"
                            }
                        } catch (e: Exception) {
                            message = "Error searching: ${e.message ?: "Failed to search"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = actorName.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Search for movies by actor" }
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.contains("Error") || message.contains("No movies found")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp)
                )
            } else if (searchResults.isNotEmpty()) {
                LazyColumn {
                    items(searchResults) { movie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Year: ${movie.year}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Actors: ${movie.actors}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}