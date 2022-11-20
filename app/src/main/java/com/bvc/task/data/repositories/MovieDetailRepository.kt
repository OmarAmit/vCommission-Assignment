package com.bvc.task.data.repositories

import com.bvc.task.data.model.MovieDetail
import com.bvc.task.data.network.ApiInterface
import com.bvc.task.data.network.SafeApiRequest

class MovieDetailRepository(private val api: ApiInterface) : SafeApiRequest()
{
    suspend fun getMovieDetail(title: String, apiKey: String): MovieDetail
    {
        return apiRequest { api.getMovieDetailData(title, apiKey) }
    }
}