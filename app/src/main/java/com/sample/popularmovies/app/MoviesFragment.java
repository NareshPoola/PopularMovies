package com.sample.popularmovies.app;

import android.app.Activity;
import android.content.IntentFilter;
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
import com.sample.popularmovies.utils.NetworkStateReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AppConstants, NetworkStateReceiver.NetworkStateReceiverListener {

    private int visibleThreshold = 5;
    private int lastVisibleItem;
    private int totalItemCount;
    private boolean isLoading;

    @BindView(R.id.movies_recycler_view)
    public RecyclerView mMoviesRecyclerView;
    private View rootView;
    private GridLayoutManager gridLayoutManager;
    private MoviesAdapter moviesAdapter;
    private int pageNumber = 1;
    private Handler handler = new Handler();

    private String POPULAR_MOVIES = "popular";
    private String TOP_RATED_MOVIES = "top_rated";
    private String SORT_BY = POPULAR_MOVIES;
    private String titleName;

    private List<Result> moviesList = new ArrayList<>();

    private OnMovieSelectListener onMovieSelectListener;
    private boolean isFavoriteMoviesLoaded = false;
    private List<Result> favouriteMovies = null;
    private NetworkStateReceiver networkStateReceiver;

    // To save the instances when orientation change
    private String SORTED_VALUE_KEY = "sortby";
    private String SCREEN_TITLE = "screen_title";

    private boolean isNetworkFailure = false;


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

    @Override
    public void onNetworkAvailable() {
        if (isNetworkFailure) {
            getMoviesData(pageNumber);
        }
    }

    @Override
    public void onNetworkUnavailable() {
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
        public void onLoadMore(int pNumber) {
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
                pageNumber = pNumber;
                getMoviesData(pageNumber);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SORTED_VALUE_KEY, SORT_BY);
        outState.putString(SCREEN_TITLE, ((MoviesActivity) getActivity()).getToolbarTitle());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        networkStateReceiver = new NetworkStateReceiver(getActivity());
        networkStateReceiver.addListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(networkStateReceiver);
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
                setToolbarTitle(getString(R.string.popular_movies));
                resetMoviesData();
                break;

            case R.id.action_top_rated_movies:
                SORT_BY = TOP_RATED_MOVIES;
                setToolbarTitle(getString(R.string.top_rated_movies));
                resetMoviesData();
                break;

            case R.id.action_favorite_movies:
                isFavoriteMoviesLoaded = true;
                moviesList.clear();
                setToolbarTitle(getString(R.string.favorite_movies));
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
     * Set Toolbar Title
     *
     * @param title
     */
    private void setToolbarTitle(String title) {
        ((MoviesActivity) getActivity()).setToolbarTitle(title);
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
        if (savedInstanceState != null) {
            SORT_BY = savedInstanceState.getString(SORTED_VALUE_KEY);
            titleName = savedInstanceState.getString(SCREEN_TITLE);
        }
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
        ButterKnife.bind(this, rootView);
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
        isNetworkFailure = false;
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
                isNetworkFailure = true;
                ((BaseActivity) getActivity()).showToast(getString(R.string.no_internet_connection));
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
