package com.sukhitha.movieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.IOException
import java.net.SocketTimeoutException

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleSearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search by Title", style = MaterialTheme.typography.headlineSmall) },
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
                value = searchQuery,
                onValueChange = { searchQuery = it.trim() },
                label = { Text("Title Substring") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Enter title substring to search" },
                singleLine = true,
                isError = message.contains("Error") && searchQuery.isNotBlank(),
                supportingText = {
                    if (message.contains("Error") && searchQuery.isNotBlank()) {
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
                            searchResults = fetchMoviesByTitle(searchQuery)
                            message = if (searchResults.isNotEmpty()) {
                                "Found ${searchResults.size} movies for '$searchQuery'"
                            } else {
                                "No movies found for '$searchQuery'"
                            }
                        } catch (e: SocketTimeoutException) {
                            message = "Error: Connection timed out. Check internet."
                        } catch (e: IOException) {
                            message = "Error: Check internet or OMDB server."
                        } catch (e: Exception) {
                            message = when (e.message) {
                                "Invalid API key" -> "Error: Invalid API key. Test: https://www.omdbapi.com/?s=Matrix&apikey=5f1ba1ac"
                                "Request limit reached" -> "Error: API rate limit exceeded."
                                "Empty response from API" -> "Error: No response from OMDB."
                                "Server error" -> "Error: OMDB server error."
                                else -> "Error: ${e.message ?: "Failed to search"}"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = searchQuery.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Search for movies by title substring" }
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
                    item {
                        Text(
                            text = "Search Results:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(searchResults) { title ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val intent = Intent(context, MovieSearchActivity::class.java)
                                    intent.putExtra("MOVIE_TITLE", title.substringBeforeLast(" ("))
                                    context.startActivity(intent)
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun fetchMoviesByTitle(title: String): List<String> {
    return withContext(Dispatchers.IO) {
        val apiKey = "5f1ba1ac"
        val encodedTitle = URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val urlString = "https://www.omdbapi.com/?s=$encodedTitle&apikey=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        val results = mutableListOf<String>()

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20000
            connection.readTimeout = 20000
            connection.setRequestProperty("Accept", "application/json")
            connection.connect()
            val responseCode = connection.responseCode
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = inputStream.bufferedReader()
                reader.use { it.readText() }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMessage = when (responseCode) {
                    401 -> "Invalid API key"
                    429 -> "Request limit reached"
                    500 -> "Server error"
                    else -> "HTTP $responseCode: ${errorStream.ifBlank { "Unknown error" }}"
                }
                throw Exception(errorMessage)
            }
            if (response.isBlank()) {
                throw Exception("Empty response from API")
            }
            val json = JSONObject(response)
            if (json.getString("Response") == "True") {
                val moviesArray = json.getJSONArray("Search")
                for (i in 0 until moviesArray.length()) {
                    val movie = moviesArray.getJSONObject(i)
                    results.add("${movie.getString("Title")} (${movie.getString("Year")})")
                }
            }
            results
        } catch (e: Exception) {
            throw e
        } finally {
            connection.disconnect()
        }
    }
}