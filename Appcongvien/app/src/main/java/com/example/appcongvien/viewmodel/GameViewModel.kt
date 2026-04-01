package com.example.appcongvien.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appcongvien.data.model.*
import com.example.appcongvien.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val gameRepository: GameRepository) : ViewModel() {

    private val _gamesState = MutableStateFlow<Resource<PaginatedData<GameDTO>>?>(null)
    val gamesState: StateFlow<Resource<PaginatedData<GameDTO>>?> = _gamesState

    private val _featuredGamesState = MutableStateFlow<Resource<List<GameDTO>>?>(null)
    val featuredGamesState: StateFlow<Resource<List<GameDTO>>?> = _featuredGamesState

    private val _categoriesState = MutableStateFlow<Resource<List<String>>?>(null)
    val categoriesState: StateFlow<Resource<List<String>>?> = _categoriesState

    private val _gameDetailState = MutableStateFlow<Resource<GameDTO>?>(null)
    val gameDetailState: StateFlow<Resource<GameDTO>?> = _gameDetailState

    private val _reviewsState = MutableStateFlow<Resource<PaginatedData<GameReviewDTO>>?>(null)
    val reviewsState: StateFlow<Resource<PaginatedData<GameReviewDTO>>?> = _reviewsState

    private val _createReviewState = MutableStateFlow<Resource<GameReviewDTO>?>(null)
    val createReviewState: StateFlow<Resource<GameReviewDTO>?> = _createReviewState

    private val _myReviewState = MutableStateFlow<Resource<GameReviewDTO?>?>(null)
    val myReviewState: StateFlow<Resource<GameReviewDTO?>?> = _myReviewState

    private val _updateReviewState = MutableStateFlow<Resource<GameReviewDTO>?>(null)
    val updateReviewState: StateFlow<Resource<GameReviewDTO>?> = _updateReviewState

    fun loadGames(page: Int = 1, size: Int = 10, category: String? = null, search: String? = null) {
        viewModelScope.launch {
            _gamesState.value = Resource.Loading
            _gamesState.value = gameRepository.getGames(page, size, category, search)
        }
    }

    fun loadFeaturedGames(limit: Int = 5) {
        viewModelScope.launch {
            _featuredGamesState.value = Resource.Loading
            _featuredGamesState.value = gameRepository.getFeaturedGames(limit)
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = Resource.Loading
            _categoriesState.value = gameRepository.getCategories()
        }
    }

    fun loadGameDetail(gameId: String) {
        viewModelScope.launch {
            _gameDetailState.value = Resource.Loading
            _gameDetailState.value = gameRepository.getGameDetail(gameId)
        }
    }

    fun loadGameReviews(gameId: String, page: Int = 1) {
        viewModelScope.launch {
            _reviewsState.value = Resource.Loading
            _reviewsState.value = gameRepository.getGameReviews(gameId, page)
        }
    }

    fun createReview(gameId: String, rating: Int, comment: String? = null) {
        viewModelScope.launch {
            _createReviewState.value = Resource.Loading
            _createReviewState.value = gameRepository.createReview(
                CreateReviewRequest(gameId, rating, comment)
            )
        }
    }

    fun loadMyReview(gameId: String) {
        viewModelScope.launch {
            _myReviewState.value = Resource.Loading
            _myReviewState.value = gameRepository.getMyReview(gameId)
        }
    }

    fun updateReview(reviewId: String, rating: Int, comment: String? = null) {
        viewModelScope.launch {
            _updateReviewState.value = Resource.Loading
            _updateReviewState.value = gameRepository.updateReview(
                reviewId, UpdateReviewRequest(rating, comment)
            )
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            gameRepository.deleteReview(reviewId)
        }
    }

    fun resetCreateReviewState() { _createReviewState.value = null }
    fun resetUpdateReviewState() { _updateReviewState.value = null }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository) as T
        }
    }
}
