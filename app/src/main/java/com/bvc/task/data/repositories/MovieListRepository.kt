package com.bvc.task.data.repositories

import com.bvc.task.data.model.SearchResults
import com.bvc.task.data.network.ApiInterface
import com.bvc.task.data.network.SafeApiRequest

class MovieListRepository(private val api: ApiInterface) : SafeApiRequest()
{
    suspend fun getMovies(searchTitle: String, apiKey: String, pageIndex: Int): SearchResults
    {
        return apiRequest{ api.getSearchResultData(searchTitle, apiKey, pageIndex) }
    }
}