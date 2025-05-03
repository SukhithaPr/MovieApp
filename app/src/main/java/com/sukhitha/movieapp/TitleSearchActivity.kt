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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TitleSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                TitleSearchScreen()
            }
        }
    }
}

@Composable
fun TitleSearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Title Substring") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    searchResults = fetchMoviesByTitle(searchQuery)
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotBlank() && !isLoading
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                if (searchResults.isNotEmpty()) {
                    Text("Search Results:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    searchResults.forEach { title ->
                        Text(title)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else if (searchQuery.isNotBlank()) {
                    Text("No movies found with this title substring")
                }
            }
        }
    }
}

private suspend fun fetchMoviesByTitle(title: String): List<String> {
    val apiKey = "5f1ba1ac" // Your OMDB API key
    val url = URL("https://www.omdbapi.com/?s=${title}&apikey=$apiKey")
    val connection = url.openConnection() as HttpURLConnection
    val results = mutableListOf<String>()

    return try {
        connection.connect()
        val inputStream = connection.inputStream
        val reader = inputStream.bufferedReader()
        val response = reader.use { it.readText() }
        val json = JSONObject(response)

        if (json.getString("Response") == "True") {
            val moviesArray = json.getJSONArray("Search")
            for (i in 0 until moviesArray.length()) {
                val movie = moviesArray.getJSONObject(i)
                results.add(movie.getString("Title") + " (" + movie.getString("Year") + ")")
            }
        }
        results
    } finally {
        connection.disconnect()
    }
}