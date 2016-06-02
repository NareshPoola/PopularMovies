package com.sample.popularmovies.app;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.sample.popularmovies.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by naresh poola on 16/4/16.
 */
public class ReviewDetailsActivity extends BaseActivity {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_details);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setBackNavigationIcon();
        if (savedInstanceState == null) {
            ReviewDetailsFragment reviewDetailsFragment = new ReviewDetailsFragment();
            Bundle bundle = new Bundle();
            reviewDetailsFragment.setArguments(bundle);
            bundle.putParcelable(IBundleParams.RESULT_OBJ, getIntent().getParcelableExtra(IBundleParams.RESULT_OBJ));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.review_details_container, reviewDetailsFragment)
                    .commit();
        }
    }
}
