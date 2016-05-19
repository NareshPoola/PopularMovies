package com.sample.popularmovies.app;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sample.popularmovies.R;
import com.sample.popularmovies.services.RestInterface;
import com.sample.popularmovies.services.databases.MoviesContract;
import com.sample.popularmovies.services.models.movieapi.Movies;
import com.sample.popularmovies.services.models.movieapi.Result;
import com.sample.popularmovies.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AppConstants {

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
    private OnMovieSelectListener onMovieSelectListener;
    private boolean isFavoriteMoviesLoaded = false;
    private List<Result> favouriteMovies = null;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri CONTENT_URI = MoviesContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(getActivity(), CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        favouriteMovies = FavoriteMoviesManager.create(getActivity()).getAllFavoriteMovies(data);
        if (isFavoriteMoviesLoaded) {
            moviesList.clear();
            moviesList.addAll(favouriteMovies);
            moviesAdapter.updateData(moviesList);
            if (((MoviesActivity) getActivity()).isTwoPaneContainer()) {
                onMovieSelectListener.onMovieSelected(null, moviesList.get(0));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public interface OnMovieSelectListener {
        void onMovieSelected(View v, Result item);
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
            if (!isFavoriteMoviesLoaded) {
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
        isFavoriteMoviesLoaded = false;
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

            case R.id.action_favorite_movies:
                isFavoriteMoviesLoaded = true;
                moviesList.clear();
                ((MoviesActivity) getActivity()).setToolbarTitle(getString(R.string.favorite_movies));
                moviesList.addAll(favouriteMovies);
                moviesAdapter.updateData(moviesList);
                if (((MoviesActivity) getActivity()).isTwoPaneContainer()) {
                    onMovieSelectListener.onMovieSelected(null, moviesList.get(0));
                }
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onMovieSelectListener = (OnMovieSelectListener) getActivity();

        } catch (ClassCastException ex) {
            Log.v("", "Casting the activity as a OnMovieSelectListener  failed"
                    + ex);
            onMovieSelectListener = null;
        }
    }


    public void init() {
        initViews();
        initListeners();
        initObjects();
        getMoviesData(pageNumber);
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
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
        moviesAdapter = new MoviesAdapter(getActivity(), onMovieSelectListener);
        mMoviesRecyclerView.setAdapter(moviesAdapter);
    }

    /**
     * getMoviesData : GET method - get movie data from api
     *
     * @param pageNumber - pagination
     */
    private void getMoviesData(final int pageNumber) {
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
                if (pageNumber == 1) {
                    if (((MoviesActivity) getActivity()).isTwoPaneContainer()) {
                        onMovieSelectListener.onMovieSelected(null, moviesList.get(0));
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                ((BaseActivity) getActivity()).showToast(error.getMessage());
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
