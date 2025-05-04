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

@Composable
fun TitleSearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.trim() },
            label = { Text("Title Substring") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    message = ""
                    searchResults = emptyList()
                    try {
                        Log.d("TitleSearch", "Searching for title: '$searchQuery'")
                        searchResults = fetchMoviesByTitle(searchQuery)
                        message = if (searchResults.isNotEmpty()) {
                            "Found ${searchResults.size} movies for '$searchQuery'"
                        } else {
                            "No movies found for '$searchQuery'"
                        }
                    } catch (e: SocketTimeoutException) {
                        Log.e("TitleSearch", "Timeout error: ${e.message}", e)
                        message = "Error: Connection timed out. Check your internet or try again."
                    } catch (e: IOException) {
                        Log.e("TitleSearch", "Network error: ${e.message}", e)
                        message = "Error: Check your internet connection or OMDB server availability"
                    } catch (e: Exception) {
                        Log.e("TitleSearch", "Search error: ${e.message}", e)
                        message = when (e.message) {
                            "Invalid API key" -> "Error: Invalid OMDB API key (5f1ba1ac). Test in browser: https://www.omdbapi.com/?s=Matrix&apikey=5f1ba1ac or request a new key at http://www.omdbapi.com/apikey.aspx."
                            "Request limit reached" -> "Error: API rate limit exceeded for key 5f1ba1ac. Wait a few hours or request a new key."
                            "Empty response from API" -> "Error: No response from OMDB API. Test the API key or server status."
                            "Server error" -> "Error: OMDB server error. Try again later or check server status."
                            else -> "Error: ${e.message ?: "Failed to search movies"}"
                        }
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
                color = if (message.contains("Error") || message.contains("No movies found")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
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
    return withContext(Dispatchers.IO) {
        val apiKey = "5f1ba1ac" // Verify this key by testing in browser: https://www.omdbapi.com/?s=Matrix&apikey=5f1ba1ac
        val encodedTitle = URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val urlString = "https://www.omdbapi.com/?s=$encodedTitle&apikey=$apiKey"
        Log.d("TitleSearch", "Request URL: $urlString")
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        val results = mutableListOf<String>()

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20000 // 20 seconds
            connection.readTimeout = 20000
            connection.setRequestProperty("Accept", "application/json")
            Log.d("TitleSearch", "Attempting to connect to OMDB API")
            connection.connect()
            val responseCode = connection.responseCode
            Log.d("TitleSearch", "HTTP response code: $responseCode")
            val responseHeaders = connection.headerFields.entries.joinToString { "${it.key}: ${it.value}" }
            Log.d("TitleSearch", "Response headers: $responseHeaders")
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = inputStream.bufferedReader()
                reader.use { it.readText() }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.d("TitleSearch", "Error stream: $errorStream")
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
            Log.d("TitleSearch", "Raw API response: $response")
            val json = JSONObject(response)
            if (json.getString("Response") == "True") {
                val moviesArray = json.getJSONArray("Search")
                for (i in 0 until moviesArray.length()) {
                    val movie = moviesArray.getJSONObject(i)
                    results.add("${movie.getString("Title")} (${movie.getString("Year")})")
                }
            } else {
                Log.d("TitleSearch", "API error: ${json.optString("Error", "Unknown error")}")
            }
            results
        } catch (e: Exception) {
            Log.e("TitleSearch", "Network error: ${e.message}", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}