package com.sukhitha.movieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

class MovieSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                MovieSearchScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieSearchScreen() {
    var movieTitle by remember { mutableStateOf("") }
    var movieData by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Movies", style = MaterialTheme.typography.headlineSmall) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = movieTitle,
                onValueChange = { movieTitle = it.trim() },
                label = { Text("Movie Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Enter movie title to search" },
                singleLine = true,
                isError = message.contains("Error") && movieTitle.isNotBlank(),
                supportingText = {
                    if (message.contains("Error") && movieTitle.isNotBlank()) {
                        Text("Check input or try again", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ElevatedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = ""
                            movieData = ""
                            try {
                                movieData = fetchMovieData(movieTitle)
                                val json = JSONObject(movieData)
                                message = if (json.getString("Response") == "True") {
                                    "Movie '${json.optString("Title", "N/A")}' retrieved"
                                } else {
                                    "No movie found: ${json.optString("Error", "Unknown error")}"
                                }
                            } catch (e: SocketTimeoutException) {
                                message = "Error: Connection timed out. Check internet."
                            } catch (e: IOException) {
                                message = "Error: Check internet or OMDB server."
                            } catch (e: Exception) {
                                message = when (e.message) {
                                    "Invalid API key" -> "Error: Invalid API key. Test: https://www.omdbapi.com/?t=The+Matrix&apikey=5f1ba1ac"
                                    "Request limit reached" -> "Error: API rate limit exceeded."
                                    "Empty response from API" -> "Error: No response from OMDB."
                                    "Server error" -> "Error: OMDB server error."
                                    else -> "Error: ${e.message ?: "Failed to fetch movie"}"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = movieTitle.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(56.dp)
                        .semantics { contentDescription = "Retrieve movie details" }
                ) {
                    Text("Retrieve Movie")
                }

                ElevatedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val json = JSONObject(movieData)
                                if (json.getString("Response") == "True") {
                                    val movie = Movie(
                                        imdbID = json.optString("imdbID", "N/A"),
                                        title = json.optString("Title", "N/A"),
                                        year = json.optString("Year", "N/A"),
                                        rated = json.optString("Rated", "N/A"),
                                        released = json.optString("Released", "N/A"),
                                        runtime = json.optString("Runtime", "N/A"),
                                        genre = json.optString("Genre", "N/A"),
                                        director = json.optString("Director", "N/A"),
                                        writer = json.optString("Writer", "N/A"),
                                        actors = json.optString("Actors", "N/A"),
                                        plot = json.optString("Plot", "N/A"),
                                        language = json.optString("Language", "N/A"),
                                        country = json.optString("Country", "N/A"),
                                        awards = json.optString("Awards", "N/A"),
                                        poster = json.optString("Poster", "N/A"),
                                        metascore = json.optString("Metascore", "N/A"),
                                        imdbRating = json.optString("imdbRating", "N/A"),
                                        imdbVotes = json.optString("imdbVotes", "N/A"),
                                        type = json.optString("Type", "N/A")
                                    )
                                    db.movieDao().insertMovie(movie)
                                    message = "Saved '${movie.title}' to database"
                                } else {
                                    message = "Cannot save: ${json.optString("Error", "No movie data")}"
                                }
                            } catch (e: Exception) {
                                message = "Error saving: ${e.message ?: "Failed to save"}"
                            }
                        }
                    },
                    enabled = movieData.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(56.dp)
                        .semantics { contentDescription = "Save movie to database" }
                ) {
                    Text("Save to DB")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (message.isNotBlank()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.contains("Error") || message.contains("No movie found")) {
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
            } else if (movieData.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val json = JSONObject(movieData)
                        if (json.getString("Response") == "True") {
                            Text(
                                text = json.optString("Title", "N/A"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Year: ${json.optString("Year", "N/A")}")
                            Text("Rated: ${json.optString("Rated", "N/A")}")
                            Text("Released: ${json.optString("Released", "N/A")}")
                            Text("Runtime: ${json.optString("Runtime", "N/A")}")
                            Text("Genre: ${json.optString("Genre", "N/A")}")
                            Text("Director: ${json.optString("Director", "N/A")}")
                            Text("Actors: ${json.optString("Actors", "N/A")}")
                            Text("Plot: ${json.optString("Plot", "N/A")}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click 'Save to DB' to add this movie",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "No movie found: ${json.optString("Error", "Unknown error")}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else if (message.isBlank() && movieTitle.isNotBlank()) {
                Text(
                    text = "Enter a title and click 'Retrieve Movie'",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private suspend fun fetchMovieData(title: String): String {
    return withContext(Dispatchers.IO) {
        val apiKey = "5f1ba1ac"
        val encodedTitle = URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val urlString = "https://www.omdbapi.com/?t=$encodedTitle&apikey=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

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
            response
        } catch (e: Exception) {
            throw e
        } finally {
            connection.disconnect()
        }
    }
}