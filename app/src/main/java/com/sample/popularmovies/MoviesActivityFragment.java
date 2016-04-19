package com.sample.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sample.popularmovies.Services.RestInterface;
import com.sample.popularmovies.Services.models.movieapi.Movies;
import com.sample.popularmovies.Services.models.movieapi.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesActivityFragment extends Fragment implements AppConstants {

    private int visibleThreshold = 5;
    private int lastVisibleItem;
    private int totalItemCount;
    private boolean isLoading;


    private View rootView;
    private RecyclerView mMoviesRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private MoviesAdapter moviesAdapter;
    private int pageNumber = 1;
    private Handler handler = new Handler();

    private String POPULAR_MOVIES = "popular";
    private String TOP_RATED_MOVIES = "top_rated";
    private String SORT_BY = POPULAR_MOVIES;

    private List<Result> moviesList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(View v, Result item);
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            totalItemCount = gridLayoutManager.getItemCount();
            lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

            if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                if (onLoadMoreListener != null) {
                    pageNumber++;
                    onLoadMoreListener.onLoadMore(pageNumber);
                }
                isLoading = true;
            }
        }
    };

    OnLoadMoreListener onLoadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore(int pageNumber) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (moviesList.size() > 0) {
                        moviesList.add(null);
                        moviesAdapter.notifyItemInserted(moviesList.size() - 1);
                    }
                }
            });

            getMoviesData(pageNumber);
        }
    };

    OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View v, Result item) {
            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
            String transitionName = getString(R.string.transition_string);

            ImageView viewStart = (ImageView) v.findViewById(R.id.movie_image);
            intent.putExtra(IBundleParams.RESULT_OBJ, (Serializable) item);

            ActivityOptionsCompat options =

                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                            viewStart,
                            transitionName
                    );
            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movies, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular_movies:
                SORT_BY = POPULAR_MOVIES;
                ((MoviesActivity) getActivity()).setToolbarTitle(getString(R.string.popular_movies));
                resetMoviesData();
                break;

            case R.id.action_top_rated_movies:
                SORT_BY = TOP_RATED_MOVIES;
                ((MoviesActivity) getActivity()).setToolbarTitle(getString(R.string.top_rated_movies));
                resetMoviesData();
                break;
        }

        return true;
    }

    /**
     * To reset the when user changed the menu option - popular/toprated movies
     */
    private void resetMoviesData() {
        pageNumber = 1;
        getMoviesData(pageNumber);
        moviesList.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_movies, container, false);
            init();
        } else {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        }
        return rootView;
    }


    public void init() {
        initViews();
        initListeners();
        initObjects();
        getMoviesData(pageNumber);
    }

    /**
     * Initializing views
     */
    private void initViews() {
        mMoviesRecyclerView = (RecyclerView) rootView.findViewById(R.id.movies_recycler_view);
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Initializing listeners
     */
    private void initListeners() {
        mMoviesRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Initializing objects
     */
    private void initObjects() {
        moviesAdapter = new MoviesAdapter(getActivity(), onItemClickListener);
        mMoviesRecyclerView.setAdapter(moviesAdapter);
    }

    /**
     * getMoviesData : GET method - get movie data from api
     *
     * @param pageNumber - pagination
     */
    private void getMoviesData(int pageNumber) {
        RestInterface restInterface = PopularMoviesApplication.getInstance().getNetworkService().getRestInterface();
        restInterface.getPopularMovies(SORT_BY, pageNumber, new Callback<Movies>() {
            @Override
            public void success(final Movies movies, Response response) {
                if (moviesList.size() > 0) {
                    moviesList.remove(moviesList.size() - 1);
                    moviesAdapter.notifyItemRemoved(moviesList.size());
                }
                moviesList.addAll(movies.getResults());
                moviesAdapter.updateData(moviesList);
                isLoading = false;
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMoviesRecyclerView.removeOnScrollListener(onScrollListener);
        mMoviesRecyclerView.setAdapter(null);
    }
}
