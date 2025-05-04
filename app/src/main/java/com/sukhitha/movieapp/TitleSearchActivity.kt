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
    var message by remember { mutableStateOf("") } // For feedback
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.trim() }, // Trim input
            label = { Text("Title Substring") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    message = ""
                    try {
                        Log.d("TitleSearch", "Searching for title: '$searchQuery'")
                        searchResults = fetchMoviesByTitle(searchQuery)
                        message = if (searchResults.isNotEmpty()) {
                            "Found ${searchResults.size} movies for '$searchQuery'"
                        } else {
                            "No movies found for '$searchQuery'"
                        }
                    } catch (e: Exception) {
                        Log.e("TitleSearch", "Error searching titles: ${e.message}")
                        message = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = searchQuery.isNotBlank() && !isLoading
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
                }
            }
        }
    }
}

private suspend fun fetchMoviesByTitle(title: String): List<String> {
    val apiKey = "5f1ba1ac" // Your OMDB API key
    val encodedTitle = title.replace(" ", "+") // Encode spaces for URL
    val url = URL("https://www.omdbapi.com/?s=$encodedTitle&apikey=$apiKey")
    val connection = url.openConnection() as HttpURLConnection
    val results = mutableListOf<String>()

    return try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000 // 5 seconds
        connection.readTimeout = 5000
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = connection.inputStream
            val reader = inputStream.bufferedReader()
            val response = reader.use { it.readText() }
            val json = JSONObject(response)
            Log.d("TitleSearch", "API response: $response")

            if (json.getString("Response") == "True") {
                val moviesArray = json.getJSONArray("Search")
                for (i in 0 until moviesArray.length()) {
                    val movie = moviesArray.getJSONObject(i)
                    results.add("${movie.getString("Title")} (${movie.getString("Year")})")
                }
            } else {
                Log.d("TitleSearch", "API error: ${json.getString("Error")}")
            }
            results
        } else {
            throw Exception("HTTP error code: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        throw e // Rethrow for coroutine to handle
    } finally {
        connection.disconnect()
    }
}