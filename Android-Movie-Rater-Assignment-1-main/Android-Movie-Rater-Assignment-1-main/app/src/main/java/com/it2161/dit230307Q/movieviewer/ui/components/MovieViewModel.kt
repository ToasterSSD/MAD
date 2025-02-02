package com.it2161.dit230307Q.movieviewer.ui.components

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit230307Q.movieviewer.MovieRaterApplication
import com.it2161.dit230307Q.movieviewer.data.FavoriteMovie
import com.it2161.dit230307Q.movieviewer.data.repository.FavoriteMovieRepository
import com.it2161.dit230307Q.movieviewer.data.repository.MovieRepository
import com.it2161.dit230307Q.movieviewer.model.ConfigurationResponse
import com.it2161.dit230307Q.movieviewer.model.MovieImagesResponse
import com.it2161.dit230307Q.movieviewer.model.MovieDetailResponse
import com.it2161.dit230307Q.movieviewer.model.MovieResponse
import com.it2161.dit230307Q.movieviewer.model.MovieReviewsResponse
import com.it2161.dit230307Q.movieviewer.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)
    private val favoriteMovieRepository: FavoriteMovieRepository

    var selectedMovie: MovieResponse? by mutableStateOf(null)
        private set

    var selectedMovieDetails: MovieDetailResponse? by mutableStateOf(null)
        private set

    private val _movies = MutableStateFlow<List<MovieResponse>>(emptyList())
    val movies: StateFlow<List<MovieResponse>> = _movies

    var configuration: ConfigurationResponse? by mutableStateOf(null)
        private set

    private val _movieImages = MutableStateFlow<Map<Int, MovieImagesResponse>>(emptyMap())
    val movieImages: StateFlow<Map<Int, MovieImagesResponse>> = _movieImages

    private val _reviews = MutableStateFlow<MovieReviewsResponse?>(null)
    val reviews: StateFlow<MovieReviewsResponse?> = _reviews

    var selectedReview: Review? by mutableStateOf(null)
        private set

    private val _favoriteMovies = MutableStateFlow<List<FavoriteMovie>>(emptyList())
    val favoriteMovies: StateFlow<List<FavoriteMovie>> = _favoriteMovies

    init {
        val favoriteMovieDao = (application as MovieRaterApplication).database.favoriteMovieDao()
        favoriteMovieRepository = FavoriteMovieRepository(favoriteMovieDao)
    }

    suspend fun isFavoriteMovie(movieId: Int, userName: String): Boolean {
        return favoriteMovieRepository.getFavoriteMovies(userName).any { it.movieId == movieId }
    }

    fun addFavoriteMovie(movie: MovieResponse, userName: String, file_path: String?) {
        viewModelScope.launch {
            val favoriteMovie = FavoriteMovie(
                movieId = movie.id,
                userName = userName,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.poster_path,
                voteAverage = movie.vote_average,
                file_path = file_path ?: ""
            )
            favoriteMovieRepository.insertFavoriteMovie(favoriteMovie)
            loadFavoriteMovies(userName)
        }
    }

    fun removeFavoriteMovie(movieId: Int, userName: String) {
        viewModelScope.launch {
            favoriteMovieRepository.deleteFavoriteMovie(movieId, userName)
            loadFavoriteMovies(userName)
        }
    }

    fun loadFavoriteMovies(userName: String) {
        viewModelScope.launch {
            _favoriteMovies.value = favoriteMovieRepository.getFavoriteMovies(userName)
        }
    }

    fun getMovieById(movieId: Int): MovieResponse? {
        return _movies.value.find { it.id == movieId }
    }

    init {
        fetchMovies("Popular")
        fetchConfiguration()
    }

    fun loadMovie(movieTitle: String) {
        viewModelScope.launch {
            selectedMovie =
                _movies.value.firstOrNull { it.title == movieTitle }
        }
    }

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            selectedMovieDetails = repository.getMovieDetails(movieId)
        }
    }

    fun loadMovieReviews(movieId: Int) {
        viewModelScope.launch {
            _reviews.value = repository.getMovieReviews(movieId)
        }
    }

    fun loadReviewById(reviewId: String) {
        viewModelScope.launch {
            val reviewsResponse = reviews.value
            selectedReview = reviewsResponse?.results?.firstOrNull { it.id == reviewId }
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
            _movies.value = response
            response.forEach { movie ->
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

    fun loadReviewById(movieId: Int, reviewId: String) {
        viewModelScope.launch {
            val reviewsResponse = repository.getMovieReviews(movieId)
            selectedReview = reviewsResponse.results.firstOrNull { it.id == reviewId }
        }
    }
}