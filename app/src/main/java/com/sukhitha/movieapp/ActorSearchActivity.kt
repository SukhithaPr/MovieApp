package com.sukhitha.movieapp

import android.os.Bundle
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
            onValueChange = { actorName = it },
            label = { Text("Actor Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    searchResults = db.movieDao().searchByActor(actorName)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = actorName.isNotBlank()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            } else if (actorName.isNotBlank()) {
                Text("No movies found with this actor")
            }
        }
    }
}