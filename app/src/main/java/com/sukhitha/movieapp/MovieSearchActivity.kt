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

@Composable
fun MovieSearchScreen() {
    var movieTitle by remember { mutableStateOf("") }
    var movieData by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = movieTitle,
            onValueChange = { movieTitle = it.trim() },
            label = { Text("Movie Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        message = ""
                        movieData = ""
                        try {
                            Log.d("MovieSearch", "Fetching data for title: '$movieTitle'")
                            movieData = fetchMovieData(movieTitle)
                            Log.d("MovieSearch", "Raw API response: $movieData")
                            val json = JSONObject(movieData)
                            message = if (json.getString("Response") == "True") {
                                "Movie '${json.optString("Title", "N/A")}' retrieved"
                            } else {
                                "No movie found: ${json.optString("Error", "Unknown error")}"
                            }
                        } catch (e: SocketTimeoutException) {
                            Log.e("MovieSearch", "Timeout error: ${e.message}", e)
                            message = "Error: Connection timed out. Check your internet or try again."
                        } catch (e: IOException) {
                            Log.e("MovieSearch", "Network error: ${e.message}", e)
                            message = "Error: Check your internet connection or OMDB server availability"
                        } catch (e: Exception) {
                            Log.e("MovieSearch", "Fetch error: ${e.message}", e)
                            message = when (e.message) {
                                "Invalid API key" -> "Error: Invalid OMDB API key (5f1ba1ac). Test in browser: https://www.omdbapi.com/?t=The+Matrix&apikey=5f1ba1ac or request a new key at http://www.omdbapi.com/apikey.aspx."
                                "Request limit reached" -> "Error: API rate limit exceeded for key 5f1ba1ac. Wait a few hours or request a new key."
                                "Empty response from API" -> "Error: No response from OMDB API. Test the API key or server status."
                                "Server error" -> "Error: OMDB server error. Try again later or check server status."
                                else -> "Error: ${e.message ?: "Failed to fetch movie"}"
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = movieTitle.isNotBlank() && !isLoading
            ) {
                Text("Retrieve Movie")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            Log.d("MovieSearch", "Attempting to save movie: $movieData")
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
                                Log.d("MovieSearch", "Saved movie: ${movie.title}")
                                message = "Saved '${movie.title}' to database"
                            } else {
                                message = "Cannot save: ${json.optString("Error", "No movie data")}"
                            }
                        } catch (e: Exception) {
                            Log.e("MovieSearch", "Save error: ${e.message}", e)
                            message = "Error saving: ${e.message ?: "Failed to save movie"}"
                        }
                    }
                },
                enabled = movieData.isNotBlank() && !isLoading
            ) {
                Text("Save to DB")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = if (message.contains("Error") || message.contains("No movie found")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else if (movieData.isNotBlank()) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                val json = JSONObject(movieData)
                if (json.getString("Response") == "True") {
                    Text("Title: ${json.optString("Title", "N/A")}", fontWeight = FontWeight.Bold)
                    Text("Year: ${json.optString("Year", "N/A")}")
                    Text("Rated: ${json.optString("Rated", "N/A")}")
                    Text("Released: ${json.optString("Released", "N/A")}")
                    Text("Runtime: ${json.optString("Runtime", "N/A")}")
                    Text("Genre: ${json.optString("Genre", "N/A")}")
                    Text("Director: ${json.optString("Director", "N/A")}")
                    Text("Writer: ${json.optString("Writer", "N/A")}")
                    Text("Actors: ${json.optString("Actors", "N/A")}")
                    Text("Plot: ${json.optString("Plot", "N/A")}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: Click 'Save to DB' to add this movie to the database")
                } else {
                    Text("No movie found: ${json.optString("Error", "Unknown error")}")
                }
            }
        } else if (message.isBlank() && movieTitle.isNotBlank()) {
            Text("Enter a movie title and click 'Retrieve Movie' to search")
        }
    }
}

private suspend fun fetchMovieData(title: String): String {
    return withContext(Dispatchers.IO) {
        val apiKey = "5f1ba1ac" // Verify this key by testing in browser: https://www.omdbapi.com/?t=The+Matrix&apikey=5f1ba1ac
        val encodedTitle = URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val urlString = "https://www.omdbapi.com/?t=$encodedTitle&apikey=$apiKey"
        Log.d("MovieSearch", "Request URL: $urlString")
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20000 // 20 seconds
            connection.readTimeout = 20000
            connection.setRequestProperty("Accept", "application/json")
            Log.d("MovieSearch", "Attempting to connect to OMDB API")
            connection.connect()
            val responseCode = connection.responseCode
            Log.d("MovieSearch", "HTTP response code: $responseCode")
            val responseHeaders = connection.headerFields.entries.joinToString { "${it.key}: ${it.value}" }
            Log.d("MovieSearch", "Response headers: $responseHeaders")
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = inputStream.bufferedReader()
                reader.use { it.readText() }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.d("MovieSearch", "Error stream: $errorStream")
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
            Log.d("MovieSearch", "Raw API response: $response")
            val json = JSONObject(response)
            if (json.getString("Response") == "False") {
                throw Exception(json.optString("Error", "Unknown API error"))
            }
            response
        } catch (e: Exception) {
            Log.e("MovieSearch", "Network error: ${e.message}", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}