package com.sukhitha.movieapp

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var message by remember { mutableStateOf("") } // For user feedback

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                scope.launch {
                    val movies = listOf(
                        Movie(
                            imdbID = "tt0111161", // From OMDB API
                            title = "The Shawshank Redemption",
                            year = "1994",
                            rated = "R",
                            released = "14 Oct 1994",
                            runtime = "142 min",
                            genre = "Drama",
                            director = "Frank Darabont",
                            writer = "Stephen King, Frank Darabont",
                            actors = "Tim Robbins, Morgan Freeman, Bob Gunton",
                            plot = "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                            language = "English", // Inferred
                            country = "USA", // Inferred
                            awards = "Nominated for 7 Oscars", // Inferred
                            poster = "https://m.media-amazon.com/images/M/MV5BNDE3ODcxYzMtY2YzZC00NmNlLWJiNDMtZDViZWM2MzIxZDYwXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_SX300.jpg", // Inferred
                            metascore = "80", // Inferred
                            imdbRating = "9.3", // Inferred
                            imdbVotes = "2,500,000", // Inferred
                            type = "movie" // Inferred
                        ),
                        Movie(
                            imdbID = "tt2310332", // From OMDB API
                            title = "Batman: The Dark Knight Returns, Part 1",
                            year = "2012",
                            rated = "PG-13",
                            released = "25 Sep 2012",
                            runtime = "76 min",
                            genre = "Animation, Action, Crime, Drama, Thriller",
                            director = "Jay Oliva",
                            writer = "Bob Kane, Frank Miller, Klaus Janson, Bob Goodman",
                            actors = "Peter Weller, Ariel Winter, David Selby, Wade Williams",
                            plot = "Batman has not been seen for ten years. A new breed of criminal ravages Gotham City, forcing 55-year-old Bruce Wayne back into the cape and cowl. But, does he still have what it takes to fight crime in a new era?",
                            language = "English", // Inferred
                            country = "USA", // Inferred
                            awards = "Nominated for 1 Annie Award", // Inferred
                            poster = "https://m.media-amazon.com/images/M/MV5BMzIxMDkxNDM2M15BMl5BanBnXkFtZTcwMDA5ODY1OA@@._V1_SX300.jpg", // Inferred
                            metascore = "N/A", // Inferred
                            imdbRating = "8.0", // Inferred
                            imdbVotes = "50,000", // Inferred
                            type = "movie" // Inferred
                        ),
                        Movie(
                            imdbID = "tt0167260", // From OMDB API
                            title = "The Lord of the Rings: The Return of the King",
                            year = "2003",
                            rated = "PG-13",
                            released = "17 Dec 2003",
                            runtime = "201 min",
                            genre = "Action, Adventure, Drama",
                            director = "Peter Jackson",
                            writer = "J.R.R. Tolkien, Fran Walsh, Philippa Boyens",
                            actors = "Elijah Wood, Viggo Mortensen, Ian McKellen",
                            plot = "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam as they approach Mount Doom with the One Ring.",
                            language = "English", // Inferred
                            country = "New Zealand, USA", // Inferred
                            awards = "Won 11 Oscars", // Inferred
                            poster = "https://m.media-amazon.com/images/M/MV5BNzA5ZDNlZWMtM2NhNS00NDJjLTk4NDItYTRmNzY3YzA1ZDAwXkEyXkFqcGdeQXVyMTc4Mzg5NjI@._V1_SX300.jpg", // Inferred
                            metascore = "94", // Inferred
                            imdbRating = "8.9", // Inferred
                            imdbVotes = "1,800,000", // Inferred
                            type = "movie" // Inferred
                        ),
                        Movie(
                            imdbID = "tt1375666", // From OMDB API
                            title = "Inception",
                            year = "2010",
                            rated = "PG-13",
                            released = "16 Jul 2010",
                            runtime = "148 min",
                            genre = "Action, Adventure, Sci-Fi",
                            director = "Christopher Nolan",
                            writer = "Christopher Nolan",
                            actors = "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page",
                            plot = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.",
                            language = "English", // Inferred
                            country = "USA, UK", // Inferred
                            awards = "Won 4 Oscars", // Inferred
                            poster = "https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_SX300.jpg", // Inferred
                            metascore = "74", // Inferred
                            imdbRating = "8.8", // Inferred
                            imdbVotes = "2,300,000", // Inferred
                            type = "movie" // Inferred
                        ),
                        Movie(
                            imdbID = "tt0133093", // From OMDB API
                            title = "The Matrix",
                            year = "1999",
                            rated = "R",
                            released = "31 Mar 1999",
                            runtime = "136 min",
                            genre = "Action, Sci-Fi",
                            director = "Lana Wachowski, Lilly Wachowski",
                            writer = "Lilly Wachowski, Lana Wachowski",
                            actors = "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss",
                            plot = "When a beautiful stranger leads computer hacker Neo to a forbidding underworld, he discovers the shocking truth--the life he knows is the elaborate deception of an evil cyber-intelligence.",
                            language = "English", // Inferred
                            country = "USA", // Inferred
                            awards = "Won 4 Oscars", // Inferred
                            poster = "https://m.media-amazon.com/images/M/MV5BNzQzOTk3OTAtNDQ0Zi00ZTVkLWI0MTEtMDllZjNkYzNjNTc4L2ltYWdlXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_SX300.jpg", // Inferred
                            metascore = "73", // Inferred
                            imdbRating = "8.7", // Inferred
                            imdbVotes = "1,900,000", // Inferred
                            type = "movie" // Inferred
                        )
                    )
                    try {
                        movies.forEach {
                            db.movieDao().insertMovie(it)
                            Log.d("MainActivity", "Inserted movie: ${it.title}")
                        }
                        message = "Added ${movies.size} movies to database"
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error inserting movies: ${e.message}")
                        message = "Error adding movies: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Movies to DB")
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = if (message.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val allMovies = db.movieDao().getAllMovies()
                        Log.d("MainActivity", "Movies in DB: ${allMovies.size}")
                        allMovies.forEach {
                            Log.d("MainActivity", "Movie: ${it.title}, ID: ${it.imdbID}")
                        }
                        message = "Found ${allMovies.size} movies in database"
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error querying database: ${e.message}")
                        message = "Error querying database: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check Database")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, MovieSearchActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search for Movies")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, ActorSearchActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search for Actors")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, TitleSearchActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search by Title Substring")
        }
    }
}