package com.bvc.task

import android.app.Application
import com.bvc.task.data.network.ApiInterface
import com.bvc.task.data.network.NetworkConnectionInterceptor
import com.bvc.task.data.repositories.MovieDetailRepository
import com.bvc.task.data.repositories.MovieListRepository
import com.bvc.task.ui.moviedetail.MovieDetailViewModelFactory
import com.bvc.task.ui.movielist.MovieListViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class MainApplication : Application(), KodeinAware
{
    override val kodein = Kodein.lazy{
        import(androidXModule(this@MainApplication))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { ApiInterface(instance()) }
        bind() from singleton { MovieDetailRepository(instance()) }
        bind() from provider { MovieDetailViewModelFactory(instance()) }
        bind() from singleton { MovieListRepository(instance()) }
        bind() from provider { MovieListViewModelFactory(instance()) }
    }
}