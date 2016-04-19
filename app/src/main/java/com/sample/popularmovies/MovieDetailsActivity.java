package com.sample.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.popularmovies.Services.RestInterface;
import com.sample.popularmovies.Services.models.movieapi.Result;
import com.sample.popularmovies.Services.models.videoapi.Videos;
import com.squareup.picasso.Picasso;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by naresh poola on 16/4/16.
 */
public class MovieDetailsActivity extends BaseActivity {

    private ImageView mMovieBannerImage;
    private ImageView mMovieImage;
    private TextView mMovieReleasedDate;
    private TextView mMovieVoteAvg;
    private TextView mMovieOverView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Result movie;
    private String youTubeVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setBackNavigationIcon();
        init();
    }

    private void init() {
        initViews();
        initListeners();
        initObjects();
        getMovieTrailers();
    }


    private void initViews() {
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mMovieBannerImage = (ImageView) findViewById(R.id.movie_image_banner);
        mMovieImage = (ImageView) findViewById(R.id.movie_image);
        mMovieReleasedDate = (TextView) findViewById(R.id.movie_released_date);
        mMovieVoteAvg = (TextView) findViewById(R.id.movie_vote_avg);
        mMovieOverView = (TextView) findViewById(R.id.movie_overview);


        movie = (Result) getIntent().getSerializableExtra(IBundleParams.RESULT_OBJ);
        mCollapsingToolbarLayout.setTitle(movie.getOriginalTitle());
        mMovieReleasedDate.setText(getString(R.string.format_release_date, Utils.formateDate(movie.getReleaseDate())));
        mMovieVoteAvg.setText(getString(R.string.format_vote_avg, String.valueOf(movie.getVoteAverage())));
        mMovieOverView.setText(movie.getOverview());
        Picasso.with(this).load(BuildConfig.IMAGE_BASE_URL_342 + movie.getBackdropPath()).into(mMovieBannerImage);
        Picasso.with(this).load(BuildConfig.IMAGE_BASE_URL_342 + movie.getPosterPath()).into(mMovieImage);
    }

    private void initListeners() {
        mCollapsingToolbarLayout.setOnClickListener(onClickListener);
    }

    private void initObjects() {
    }

    private void getMovieTrailers() {
        RestInterface restInterface = PopularMoviesApplication.getInstance().getNetworkService().getRestInterface();
        restInterface.getMovieTrailers(movie.getId(), new Callback<Videos>() {
            @Override
            public void success(final Videos videos, Response response) {
                for (int i = 0; i < videos.getResults().size(); i++) {
                    com.sample.popularmovies.Services.models.videoapi.Result video = videos.getResults().get(i);
                    if (video.getType().equalsIgnoreCase("Trailer")) {
                        youTubeVideoId = videos.getResults().get(0).getKey();
                        break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (youTubeVideoId != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + youTubeVideoId));
                startActivity(intent);
            }
        }
    };


}
