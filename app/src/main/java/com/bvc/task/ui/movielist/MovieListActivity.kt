package com.bvc.task.ui.movielist

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bvc.task.R
import com.bvc.task.databinding.ActivityMovielistBinding
import com.bvc.task.ui.adapter.CustomAdapterMovies
import com.bvc.task.ui.moviedetail.MovieDetailScrollingActivity
import com.bvc.task.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MovieListActivity : AppCompatActivity(), KodeinAware {
    companion object {
        const val ANIMATION_DURATION = 1000.toLong()
    }


    override val kodein by kodein()
    private lateinit var dataBind: ActivityMovielistBinding
    private lateinit var viewModel: MovieListViewModel
    private val factory: MovieListViewModelFactory by instance()
    private lateinit var customAdapterMovies: CustomAdapterMovies

    private var query: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBind = DataBindingUtil.setContentView(this, R.layout.activity_movielist)

        setupViewModel()
        setupUI()
        initializeObserver()
        handleNetworkChanges()
        search(dataBind.searchView);
    }

    private fun search(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(key: String): Boolean {
                dismissKeyboard(searchView)
                searchView.clearFocus()
                // viewModel.searchMovie(query)
//                query = key
//                Log.e("query",query+"-")
//                setupAPICall()

                return true
            }

            override fun onQueryTextChange(key: String): Boolean {
                Log.e("query", query + "-")

                query = key
                if (key.length >= 3) {
                    viewModel.searchMovie(query)
                    setupAPICall()
                } else {
                    showToast("please search atleast 3 words !!")
                    dataBind.recyclerViewMovies.hide()
                    dataBind.progressBar.hide()
                    dataBind.imageViewNotFound.show()

//                    dataBind.imageViewNotFound.show()
                }
                return true
            }
        })
    }

    private fun setupUI() {
        customAdapterMovies = CustomAdapterMovies()
        dataBind.recyclerViewMovies.apply {
            layoutManager = GridLayoutManager(context,2)
            itemAnimator = DefaultItemAnimator()
            adapter = customAdapterMovies
            addOnItemTouchListener(
                RecyclerItemClickListener(
                    applicationContext,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            if (customAdapterMovies.getData().isNotEmpty()) {
                                val searchItem = customAdapterMovies.getData()[position]
                                searchItem?.let {
                                    val intent =
                                        Intent(
                                            applicationContext,
                                            MovieDetailScrollingActivity::class.java
                                        )
                                    intent.putExtra(AppConstant.INTENT_POSTER, it.poster)
                                    intent.putExtra(AppConstant.INTENT_TITLE, it.title)
                                    startActivity(intent)
                                }
                            }
                        }
                    })
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    val visibleItemCount = layoutManager!!.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    viewModel.checkForLoadMoreItems(
                        visibleItemCount,
                        totalItemCount,
                        firstVisibleItemPosition
                    )
                }
            })
        }

        //getWindow().setStatusBarColor(Color.WHITE);

        // search(searchView);
//        viewModel.searchMovie(query)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, factory).get(MovieListViewModel::class.java)
    }

    private fun initializeObserver() {
        viewModel.movieNameLiveData.observe(this, Observer {
            Log.i("Info", "Movie Name = $it")
        })
        viewModel.loadMoreListLiveData.observe(this, Observer {
            if (it) {
                customAdapterMovies.setData(null)
                Handler().postDelayed({
                    viewModel.loadMore()
                }, 2000)
            }
        })
    }

    private fun setupAPICall() {
        viewModel.moviesLiveData.observe(this, Observer { state ->
            when (state) {
                is State.Loading -> {
                    dataBind.recyclerViewMovies.hide()
                    dataBind.progressBar.show()
                    dataBind.imageViewNotFound.hide()
                }
                is State.Success -> {
                    dataBind.recyclerViewMovies.show()
                    dataBind.progressBar.hide()
                    dataBind.imageViewNotFound.hide()
                    customAdapterMovies.setData(state.data)
                }
                is State.Error -> {
                    dataBind.progressBar.hide()
                    dataBind.imageViewNotFound.show()
                    showToast(state.message)
                }
            }
        })
    }

    private fun handleNetworkChanges() {
        NetworkUtils.getNetworkLiveData(applicationContext).observe(this, Observer { isConnected ->
            if (!isConnected) {
                dataBind.textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                dataBind.networkStatusLayout.apply {
                    show()
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                }
            } else {
                if (viewModel.moviesLiveData.value is State.Error || customAdapterMovies.itemCount == 0)
                    viewModel.getMovies()

                dataBind.textViewNetworkStatus.text = getString(R.string.text_connectivity)
                dataBind.networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(1f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                hide()
                            }
                        })
                }
            }
        })
    }

}