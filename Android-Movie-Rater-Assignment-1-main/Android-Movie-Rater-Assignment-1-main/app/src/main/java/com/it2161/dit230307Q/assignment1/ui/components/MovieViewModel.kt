package com.it2161.dit230307Q.assignment1.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit230307Q.assignment1.MovieRaterApplication
import com.it2161.dit230307Q.assignment1.data.MovieItem
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    var selectedMovie: MovieItem? by mutableStateOf(null)
        private set

    fun loadMovie(movieTitle: String) {
        viewModelScope.launch {
            selectedMovie = MovieRaterApplication.instance.data.firstOrNull { it.title == movieTitle }
        }
    }
}