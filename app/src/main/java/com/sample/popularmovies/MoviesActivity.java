package com.sample.popularmovies;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

/**
 * Created by naresh poola on 16/4/16.
 */
public class MoviesActivity extends BaseActivity {


    private Toolbar mToolbar;
    private TextView mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(mToolbar);
    }

    /**
     * Method used to set the toolbar title
     * @param title
     */
    public void setToolbarTitle(String title){
        mToolbarTitle.setText(title);
    }

}
