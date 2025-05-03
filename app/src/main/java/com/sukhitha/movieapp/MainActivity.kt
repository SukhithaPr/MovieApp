package com.sukhitha.movieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
                    // Hardcoded movies as per assignment
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
                            poster = "",
                            metascore = "80",
                            imdbRating = "9.3",
                            imdbVotes = "2,500,000",
                            type = "movie"
                        ),
                        // Add more movies as needed
                    )
                    movies.forEach { db.movieDao().insertMovie(it) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Movies to DB")
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