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
            onValueChange = { movieTitle = it },
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
                        movieData = fetchMovieData(movieTitle)
                        isLoading = false
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
                            val json = JSONObject(movieData)
                            if (json.getString("Response") == "True") {
                                val movie = Movie(
                                    imdbID = json.getString("imdbID"),
                                    title = json.getString("Title"),
                                    year = json.getString("Year"),
                                    rated = json.getString("Rated"),
                                    released = json.getString("Released"),
                                    runtime = json.getString("Runtime"),
                                    genre = json.getString("Genre"),
                                    director = json.getString("Director"),
                                    writer = json.getString("Writer"),
                                    actors = json.getString("Actors"),
                                    plot = json.getString("Plot"),
                                    language = json.getString("Language"),
                                    country = json.getString("Country"),
                                    awards = json.getString("Awards"),
                                    poster = json.getString("Poster"),
                                    metascore = json.getString("Metascore"),
                                    imdbRating = json.getString("imdbRating"),
                                    imdbVotes = json.getString("imdbVotes"),
                                    type = json.getString("Type")
                                )
                                db.movieDao().insertMovie(movie)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                enabled = movieData.isNotBlank() && !isLoading
            ) {
                Text("Save to DB")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Title: ${json.getString("Title")}", fontWeight = FontWeight.Bold)
                        Text("Year: ${json.getString("Year")}")
                        Text("Rated: ${json.getString("Rated")}")
                        Text("Released: ${json.getString("Released")}")
                        Text("Runtime: ${json.getString("Runtime")}")
                        Text("Genre: ${json.getString("Genre")}")
                        Text("Director: ${json.getString("Director")}")
                        Text("Writer: ${json.getString("Writer")}")
                        Text("Actors: ${json.getString("Actors")}")
                        Text("Plot: ${json.getString("Plot")}")
                    } else {
                        Text("Movie not found!")
                    }
                }
            }
        }
    }

private suspend fun fetchMovieData(title: String): String {
    val apiKey = "5f1ba1ac" // Your OMDB API key
    val url = URL("https://www.omdbapi.com/?t=${title}&apikey=$apiKey")
    val connection = url.openConnection() as HttpURLConnection

    return try {
        connection.connect()
        val inputStream = connection.inputStream
        val reader = inputStream.bufferedReader()
        reader.use { it.readText() }
    } finally {
        connection.disconnect()
    }
}