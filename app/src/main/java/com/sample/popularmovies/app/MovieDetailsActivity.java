package com.sample.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sample.popularmovies.BuildConfig;
import com.sample.popularmovies.R;
import com.sample.popularmovies.services.models.movieapi.Result;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by naresh poola on 16/4/16.
 */
public class MovieDetailsActivity extends BaseActivity implements MovieDetailsFragment.IMovieTrailerSetListener {

    MovieDetailsFragment movieDetailsFragment;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.movie_image_banner)
    ImageView mMovieBannerImage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton favouriteFab;
    private Result movie;
    private String youTubeVideoId;
    private FavoriteMoviesManager favoriteMoviesManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setBackNavigationIcon();
        init();
        if (savedInstanceState == null) {
            movieDetailsFragment = new MovieDetailsFragment();
            Bundle bundle = new Bundle();
            movieDetailsFragment.setArguments(bundle);
            bundle.putParcelable(IBundleParams.RESULT_OBJ, getIntent().getParcelableExtra(IBundleParams.RESULT_OBJ));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, movieDetailsFragment)
                    .commit();
        }
    }

    void init() {
        favoriteMoviesManager = FavoriteMoviesManager.create(this);
        setDataToViews();
    }

    void setDataToViews() {
        movie = (Result) getIntent().getParcelableExtra(IBundleParams.RESULT_OBJ);
        if (favoriteMoviesManager.isFavorite(movie)) {
            updateFavouriteFab(true);
        }
        mCollapsingToolbarLayout.setTitle(movie.getOriginalTitle());
        Picasso.with(this).load(BuildConfig.IMAGE_BASE_URL_342 + movie.getBackdropPath()).into(mMovieBannerImage, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap myBitmap = ((BitmapDrawable) mMovieBannerImage.getDrawable()).getBitmap();
                if (myBitmap != null && !myBitmap.isRecycled()) {
                    Palette.from(myBitmap).generate(paletteListener);
                }
            }

            @Override
            public void onError() {

            }
        });

    }

    Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
        public void onGenerated(Palette palette) {
            int defaultColor = 0x000000;
            int vibrant = palette.getVibrantColor(defaultColor);
            int vibrantLight = palette.getLightVibrantColor(defaultColor);
            int vibrantDark = palette.getDarkVibrantColor(defaultColor);
            mCollapsingToolbarLayout.setBackgroundColor(vibrant);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCollapsingToolbarLayout.setStatusBarScrimColor(vibrant);
                mCollapsingToolbarLayout.setContentScrimColor(vibrant);
            }
        }
    };

    @OnClick({R.id.collapsing_toolbar, R.id.fab})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collapsing_toolbar:
                if (youTubeVideoId != null) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + youTubeVideoId));
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // launch default browser if youtube app is not installed in device
                        openInDefaultBrowser();
                    }
                }
                break;
            case R.id.fab:
                if (favoriteMoviesManager.isFavorite(movie)) {
                    favoriteMoviesManager.remove(movie);
                    updateFavouriteFab(false);
                    Toast.makeText(MovieDetailsActivity.this, movie.getOriginalTitle() + " has been removed from your favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MovieDetailsActivity.this, movie.getOriginalTitle() + " has been added to your favorites", Toast.LENGTH_SHORT).show();
                    favoriteMoviesManager.add(movie);
                    updateFavouriteFab(true);
                }
                break;
        }
    }

    private void openInDefaultBrowser() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(" https://www.youtube.com/watch?v=" + youTubeVideoId));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onLoad(String youTubeVideoId) {
        this.youTubeVideoId = youTubeVideoId;
    }

    private void updateFavouriteFab(boolean isFavourite) {
        if (isFavourite) {
            favouriteFab.setImageResource(R.drawable.icon_favourite);
        } else {
            favouriteFab.setImageResource(R.drawable.icon_unfavourite);
        }
    }


}
