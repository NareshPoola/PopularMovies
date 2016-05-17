package com.sample.popularmovies.app;

import android.app.Application;

import com.sample.popularmovies.services.NetworkService;

/**
 * Created by naresh poola on 15/4/16.
 */
public class PopularMoviesApplication extends Application {

    private static PopularMoviesApplication instance;
    private NetworkService networkService;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static PopularMoviesApplication getInstance() {
        if (instance == null) {
            instance = new PopularMoviesApplication();
        }
        return instance;
    }

    public NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = new NetworkService();
        }
        return networkService;
    }


}
