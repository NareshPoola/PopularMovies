package com.sample.popularmovies.services;

import com.sample.popularmovies.BuildConfig;

import retrofit.RestAdapter;

/**
 * Created by sriram on 15/4/16.
 */
public class NetworkService {

    private String BASE_URL = BuildConfig.TMDB_SERVER_BASE_URL;
    private RestInterface restInterface;

    public NetworkService() {
        //making object of RestAdapter
        RestAdapter adapter = new RestAdapter.Builder().setEndpoint(BASE_URL).build();

        //Creating Rest Services
        restInterface = adapter.create(RestInterface.class);
    }

    public RestInterface getRestInterface() {
        return restInterface;
    }

}
