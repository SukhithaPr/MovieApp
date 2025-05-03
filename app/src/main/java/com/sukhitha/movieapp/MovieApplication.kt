package com.sukhitha.movieapp

import android.app.Application

class MovieApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}