package com.it2161.dit230307Q.movieviewer.ui.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit230307Q.movieviewer.MovieRaterApplication
import com.it2161.dit230307Q.movieviewer.data.MovieItem
import com.it2161.dit230307Q.movieviewer.data.repository.MovieRepository
import com.it2161.dit230307Q.movieviewer.model.ConfigurationResponse
import com.it2161.dit230307Q.movieviewer.model.MovieImagesResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.it2161.dit230307Q.movieviewer.model.MovieDetailResponse

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    var selectedMovie: MovieItem? by mutableStateOf(null)
        private set

    var selectedMovieDetails: MovieDetailResponse? by mutableStateOf(null)
        private set

    private val _movies = MutableStateFlow<List<MovieItem>>(emptyList())
    val movies: StateFlow<List<MovieItem>> = _movies

    var configuration: ConfigurationResponse? by mutableStateOf(null)
        private set

    private val _movieImages = MutableStateFlow<Map<Int, MovieImagesResponse>>(emptyMap())
    val movieImages: StateFlow<Map<Int, MovieImagesResponse>> = _movieImages

    init {
        fetchMovies("Popular")
        fetchConfiguration()
    }

    fun loadMovie(movieTitle: String) {
        viewModelScope.launch {
            selectedMovie = MovieRaterApplication.instance.data.firstOrNull { it.title == movieTitle }
        }
    }

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            selectedMovieDetails = repository.getMovieDetails(movieId)
        }
    }

    fun fetchMovies(category: String) {
        viewModelScope.launch {
            val response = when (category) {
                "Popular" -> repository.getPopularMovies()
                "Top Rated" -> repository.getTopRatedMovies()
                "Now Playing" -> repository.getNowPlayingMovies()
                "Upcoming" -> repository.getUpcomingMovies()
                else -> repository.getPopularMovies()
            }
            _movies.value = response.results
            response.results.forEach { movie ->
                fetchMovieImages(movie.id)
            }
        }
    }

    private fun fetchConfiguration() {
        viewModelScope.launch {
            configuration = repository.getConfiguration()
        }
    }

    private fun fetchMovieImages(movieId: Int) {
        viewModelScope.launch {
            val imagesResponse = repository.getMovieImages(movieId)
            _movieImages.value = _movieImages.value + (movieId to imagesResponse)
        }
    }
}