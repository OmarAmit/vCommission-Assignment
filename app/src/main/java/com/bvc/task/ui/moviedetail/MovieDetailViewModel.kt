package com.bvc.task.ui.moviedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bvc.task.data.model.MovieDetail
import com.bvc.task.data.repositories.MovieDetailRepository
import com.bvc.task.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailViewModel(private val repository: MovieDetailRepository) : ViewModel()
{
    private val _movieDetailLiveData = MutableLiveData<State<MovieDetail>>()
    val movieDetailLiveData: LiveData<State<MovieDetail>> get() = _movieDetailLiveData
    private lateinit var movieDetailResponse: MovieDetail

    fun getMovieDetail(movieTitle: String)
    {
        _movieDetailLiveData.postValue(State.loading())
        viewModelScope.launch(Dispatchers.IO)
        {
            try
            {
                movieDetailResponse = repository.getMovieDetail(movieTitle, AppConstant.API_KEY)
                withContext(Dispatchers.Main)
                {
                    _movieDetailLiveData.postValue(State.success(movieDetailResponse))
                }
            }
            catch (e: ApiException)
            {
                withContext(Dispatchers.Main)
                {
                    _movieDetailLiveData.postValue(State.error(e.message!!))
                }
            }
            catch (e: NoInternetException)
            {
                withContext(Dispatchers.Main)
                {
                    _movieDetailLiveData.postValue(State.error(e.message!!))
                }
            }
        }
    }
}