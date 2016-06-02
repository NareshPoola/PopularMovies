package com.sample.popularmovies.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.popularmovies.R;
import com.sample.popularmovies.services.models.movieapi.Result;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by naresh poola on 16/4/16.
 */
public class MoviesActivity extends BaseActivity implements MoviesFragment.OnMovieSelectListener {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @Nullable
    boolean isTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        isTwoPane = findViewById(R.id.movie_details_container) != null;
    }

    /**
     * Method used to set the toolbar title
     *
     * @param title
     */
    public void setToolbarTitle(String title) {
        mToolbarTitle.setText(title);
    }


    public String getToolbarTitle() {
        return mToolbarTitle.getText().toString();
    }

    @Override
    public void onMovieSelected(View v, Result item) {
        String transitionName = getString(R.string.transition_string);
        ImageView viewStart = null;
        if (v != null) {
            viewStart = (ImageView) v.findViewById(R.id.movie_image);
        }
        if (!isTwoPane) {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(IBundleParams.RESULT_OBJ, (Parcelable) item);
            if (viewStart != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options =

                        ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                viewStart,
                                transitionName
                        );
                ActivityCompat.startActivity(this, intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else {
            MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(IBundleParams.RESULT_OBJ, (Parcelable) item);
            movieDetailsFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, movieDetailsFragment)
                    .commit();
        }

    }

    public boolean isTwoPaneContainer() {
        return isTwoPane;
    }
}
