package com.sukhitha.movieapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                WelcomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movie App", style = MaterialTheme.typography.headlineSmall) },
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Movie App",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Search movies, actors, or build your collection",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                ElevatedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = ""
                            try {
                                val movies = listOf(
                                    Movie(
                                        imdbID = "tt0111161",
                                        title = "The Shawshank Redemption",
                                        year = "1994",
                                        rated = "R",
                                        released = "14 Oct 1994",
                                        runtime = "142 min",
                                        genre = "Drama",
                                        director = "Frank Darabont",
                                        writer = "Stephen King, Frank Darabont",
                                        actors = "Tim Robbins, Morgan Freeman, Bob Gunton",
                                        plot = "Two imprisoned men bond over a number of years...",
                                        language = "English",
                                        country = "USA",
                                        awards = "Nominated for 7 Oscars",
                                        poster = "N/A",
                                        metascore = "80",
                                        imdbRating = "9.3",
                                        imdbVotes = "2,500,000",
                                        type = "movie"
                                    ),
                                    Movie(
                                        imdbID = "tt2310332",
                                        title = "Batman: The Dark Knight Returns, Part 1",
                                        year = "2012",
                                        rated = "PG-13",
                                        released = "25 Sep 2012",
                                        runtime = "76 min",
                                        genre = "Animation, Action, Crime, Drama, Thriller",
                                        director = "Jay Oliva",
                                        writer = "Bob Kane, Frank Miller, Klaus Janson, Bob Goodman",
                                        actors = "Peter Weller, Ariel Winter, David Selby, Wade Williams",
                                        plot = "Batman has not been seen for ten years...",
                                        language = "English",
                                        country = "USA",
                                        awards = "Nominated for 1 Annie Award",
                                        poster = "N/A",
                                        metascore = "N/A",
                                        imdbRating = "8.0",
                                        imdbVotes = "50,000",
                                        type = "movie"
                                    ),
                                    Movie(
                                        imdbID = "tt0167260",
                                        title = "The Lord of the Rings: The Return of the King",
                                        year = "2003",
                                        rated = "PG-13",
                                        released = "17 Dec 2003",
                                        runtime = "201 min",
                                        genre = "Action, Adventure, Drama",
                                        director = "Peter Jackson",
                                        writer = "J.R.R. Tolkien, Fran Walsh, Philippa Boyens",
                                        actors = "Elijah Wood, Viggo Mortensen, Ian McKellen",
                                        plot = "Gandalf and Aragorn lead the World of Men...",
                                        language = "English",
                                        country = "New Zealand, USA",
                                        awards = "Won 11 Oscars",
                                        poster = "N/A",
                                        metascore = "94",
                                        imdbRating = "8.9",
                                        imdbVotes = "1,800,000",
                                        type = "movie"
                                    ),
                                    Movie(
                                        imdbID = "tt1375666",
                                        title = "Inception",
                                        year = "2010",
                                        rated = "PG-13",
                                        released = "16 Jul 2010",
                                        runtime = "148 min",
                                        genre = "Action, Adventure, Sci-Fi",
                                        director = "Christopher Nolan",
                                        writer = "Christopher Nolan",
                                        actors = "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page",
                                        plot = "A thief who steals corporate secrets...",
                                        language = "English",
                                        country = "USA, UK",
                                        awards = "Won 4 Oscars",
                                        poster = "N/A",
                                        metascore = "74",
                                        imdbRating = "8.8",
                                        imdbVotes = "2,300,000",
                                        type = "movie"
                                    ),
                                    Movie(
                                        imdbID = "tt0133093",
                                        title = "The Matrix",
                                        year = "1999",
                                        rated = "R",
                                        released = "31 Mar 1999",
                                        runtime = "136 min",
                                        genre = "Action, Sci-Fi",
                                        director = "Lana Wachowski, Lilly Wachowski",
                                        writer = "Lilly Wachowski, Lana Wachowski",
                                        actors = "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss",
                                        plot = "When a beautiful stranger leads computer hacker Neo...",
                                        language = "English",
                                        country = "USA",
                                        awards = "Won 4 Oscars",
                                        poster = "N/A",
                                        metascore = "73",
                                        imdbRating = "8.7",
                                        imdbVotes = "1,900,000",
                                        type = "movie"
                                    )
                                )
                                movies.forEach {
                                    db.movieDao().insertMovie(it)
                                    Log.d("MainActivity", "Inserted movie: ${it.title} (ID: ${it.imdbID})")
                                }
                                message = "Added ${movies.size} movies to database"
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Error adding movies: ${e.message}", e)
                                message = "Error adding movies: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Add sample movies to database" }
                ) {
                    Text("Add Movies to DB")
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = {
                        context.startActivity(Intent(context, MovieSearchActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Navigate to movie search" }
                ) {
                    Text("Search for Movies")
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = {
                        context.startActivity(Intent(context, ActorSearchActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Navigate to actor search" }
                ) {
                    Text("Search for Actors")
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = {
                        context.startActivity(Intent(context, TitleSearchActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Navigate to title substring search" }
                ) {
                    Text("Search Movies by Title")
                }

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedButton(
                    onClick = {
                        context.startActivity(Intent(context, MovieListActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "View all movies in collection" }
                ) {
                    Text("View All My Movies")
                }

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (message.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}