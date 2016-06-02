package com.sample.popularmovies.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.popularmovies.BuildConfig;
import com.sample.popularmovies.R;
import com.sample.popularmovies.services.RestInterface;
import com.sample.popularmovies.services.models.movieapi.Result;
import com.sample.popularmovies.services.models.reviewapi.Reviews;
import com.sample.popularmovies.services.models.videoapi.Videos;
import com.sample.popularmovies.utils.AppConstants;
import com.sample.popularmovies.utils.AppUtils;
import com.sample.popularmovies.utils.NetworkStateReceiver;
import com.sample.popularmovies.utils.TMDBUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment implements AppConstants, NetworkStateReceiver.NetworkStateReceiverListener {

    private View rootView;
    @BindView(R.id.movie_image)
    ImageView mMovieImage;
    @BindView(R.id.movie_released_date)
    TextView mMovieReleasedDate;
    @BindView(R.id.movie_vote_avg)
    TextView mMovieVoteAvg;
    @BindView(R.id.movie_overview)
    TextView mMovieOverView;
    @BindView(R.id.movie_image_banner)
    ImageView mMovieBannerImage;
    @BindView(R.id.content_movie_details_header)
    LinearLayout mHeader;
    @BindView(R.id.movies_video_recycler_view)
    RecyclerView mVideoRecyclerView;
    @BindView(R.id.trailers_view_layout)
    LinearLayout mTrailersViewLayout;
    @BindView(R.id.reviews_view_layout)
    LinearLayout mReviewsViewLayout;
    @BindView(R.id.dynamic_reviews_layout)
    LinearLayout mDynamicReviewsLayout;
    @BindView(R.id.view_see_all_reviews)
    TextView mAllReviews;
    @BindView(R.id.fab)
    FloatingActionButton favouriteFab;
    Result movie;
    String youTubeVideoId;
    IMovieTrailerSetListener iMovieTrailerSetListener;
    LinearLayoutManager linearLayoutManager;
    private MovieTrailersAdapter movieTrailersAdapter;
    private FavoriteMoviesManager favoriteMoviesManager;
    private NetworkStateReceiver networkStateReceiver;
    private boolean isNetworkFailure = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkStateReceiver = new NetworkStateReceiver(getActivity());
        networkStateReceiver.addListener(this);
    }

    @Override
    public void onNetworkAvailable() {
        if (isNetworkFailure) {
            getMovieTrailers();
            getMovieReviews();
        }
    }

    @Override
    public void onNetworkUnavailable() {

    }

    public interface OnVideoSelectListener {
        void onVideoSelected(View v, com.sample.popularmovies.services.models.videoapi.Result item);
    }

    OnVideoSelectListener onVideoSelectListener = new OnVideoSelectListener() {
        @Override
        public void onVideoSelected(View v, com.sample.popularmovies.services.models.videoapi.Result item) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + item.getKey()));
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                // launch default browser if youtube app is not installed in device
                openInDefaultBrowser();
            }
        }
    };

    private void openInDefaultBrowser() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(" https://www.youtube.com/watch?v=" + youTubeVideoId));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
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
            iMovieTrailerSetListener = (IMovieTrailerSetListener) activity;

        } catch (ClassCastException ex) {
            Log.v("", "Casting the activity as a IMovieTrailerSetListener  failed"
                    + ex);
            iMovieTrailerSetListener = null;
        }
    }


    void init() {
        favoriteMoviesManager = FavoriteMoviesManager.create(getActivity());
        ButterKnife.bind(this, rootView);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mVideoRecyclerView.setLayoutManager(linearLayoutManager);
        movieTrailersAdapter = new MovieTrailersAdapter(getActivity(), onVideoSelectListener);
        mVideoRecyclerView.setAdapter(movieTrailersAdapter);
        setDataToViews();
        getMovieTrailers();
        getMovieReviews();
    }


    void setDataToViews() {
        Bundle bundle = getArguments();
        movie = (Result) bundle.getParcelable(IBundleParams.RESULT_OBJ);
        mMovieReleasedDate.setText(getString(R.string.format_release_date, AppUtils.formateDate(movie.getReleaseDate())));
        mMovieVoteAvg.setText(getString(R.string.format_vote_avg, String.valueOf(movie.getVoteAverage())));
        mMovieOverView.setText(movie.getOverview());
        Picasso.with(getActivity()).load(TMDBUtils.getImageSizePath(getResources().getDisplayMetrics().densityDpi) + movie.getPosterPath()).into(mMovieImage);
        if (getActivity() instanceof MoviesActivity) {
            mHeader.setVisibility(View.VISIBLE);
            Picasso.with(getActivity()).load(BuildConfig.IMAGE_BASE_URL_342 + movie.getBackdropPath()).into(mMovieBannerImage);
            favouriteFab.setVisibility(View.VISIBLE);
        }

        if (favoriteMoviesManager.isFavorite(movie)) {
            updateFavouriteFab(true);
        }

    }

    void getMovieTrailers() {
        isNetworkFailure = false;
        RestInterface restInterface = PopularMoviesApplication.getInstance().getNetworkService().getRestInterface();
        restInterface.getMovieTrailers(movie.getId(), new Callback<Videos>() {
            @Override
            public void success(final Videos videos, Response response) {
                if (videos.getResults().size() > 0) {
                    mTrailersViewLayout.setVisibility(View.VISIBLE);
                    movieTrailersAdapter.updateData(videos.getResults());
                    for (int i = 0; i < videos.getResults().size(); i++) {
                        com.sample.popularmovies.services.models.videoapi.Result video = videos.getResults().get(i);
                        if (video.getType().equalsIgnoreCase("Trailer")) {
                            youTubeVideoId = videos.getResults().get(0).getKey();
                            if (iMovieTrailerSetListener != null) {
                                iMovieTrailerSetListener.onLoad(youTubeVideoId);
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                isNetworkFailure = true;
            }
        });
    }

    void getMovieReviews() {
        isNetworkFailure = false;
        RestInterface restInterface = PopularMoviesApplication.getInstance().getNetworkService().getRestInterface();
        restInterface.getMovieReviews(movie.getId(), new Callback<Reviews>() {
            @Override
            public void success(final Reviews reviews, Response response) {
                if (reviews.getResults().size() > 0) {
                    mDynamicReviewsLayout.removeAllViews();
                    mReviewsViewLayout.setVisibility(View.VISIBLE);
                    if (getActivity() != null) {
                        for (int i = 0; i < reviews.getResults().size(); i++) {
                            com.sample.popularmovies.services.models.reviewapi.Result review = reviews.getResults().get(i);
                            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View mReviewRowItem = inflater.inflate(R.layout.list_movie_review_item, null);
                            TextView mReviewerName = (TextView) mReviewRowItem.findViewById(R.id.reviewer_name);
                            TextView mReviewDesc = (TextView) mReviewRowItem.findViewById(R.id.review_description);
                            mReviewerName.setText(review.getAuthor());
                            mReviewDesc.setText(review.getContent());
                            mReviewRowItem.setTag(review);
                            mReviewRowItem.setOnClickListener(reviewSelectedListener);
                            mDynamicReviewsLayout.addView(mReviewRowItem);
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                isNetworkFailure = true;
            }
        });
    }

    @OnClick({R.id.movie_image_banner})
    public void onBannerClick(View v) {

        if (youTubeVideoId != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + youTubeVideoId));
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                // launch default browser if youtube app is not installed in device
                openInDefaultBrowser();
            }
        }
    }

    @OnClick({R.id.fab})
    public void fabClick(View v) {
        if (favoriteMoviesManager.isFavorite(movie)) {
            favoriteMoviesManager.remove(movie);
            updateFavouriteFab(false);
            Toast.makeText(getActivity(), movie.getOriginalTitle() + " has been removed from your favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), movie.getOriginalTitle() + " has been added to your favorites", Toast.LENGTH_SHORT).show();
            favoriteMoviesManager.add(movie);
            updateFavouriteFab(true);
        }
    }

    View.OnClickListener reviewSelectedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            com.sample.popularmovies.services.models.reviewapi.Result result = (com.sample.popularmovies.services.models.reviewapi.Result) v.getTag();
            Intent intent = new Intent(getActivity(), ReviewDetailsActivity.class);
            intent.putExtra(IBundleParams.RESULT_OBJ, (Parcelable) result);
            View name = v.findViewById(R.id.reviewer_name);
            View desc = v.findViewById(R.id.review_description);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Pair<View, String> pair1 = Pair.create(name, name.getTransitionName());
                Pair<View, String> pair2 = Pair.create(desc, desc.getTransitionName());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), pair1, pair2);

                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            } else {
                startActivity(intent);
            }

        }
    };


    public interface IMovieTrailerSetListener {
        void onLoad(String youTubeVideoId);
    }

    private void updateFavouriteFab(boolean isFavourite) {
        if (isFavourite) {
            favouriteFab.setImageResource(R.drawable.icon_favourite);
        } else {
            favouriteFab.setImageResource(R.drawable.icon_unfavourite);
        }
    }


}
