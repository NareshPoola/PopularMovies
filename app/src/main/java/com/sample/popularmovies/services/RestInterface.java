package com.sample.popularmovies.services;

import com.sample.popularmovies.BuildConfig;
import com.sample.popularmovies.services.models.movieapi.Movies;
import com.sample.popularmovies.services.models.reviewapi.Reviews;
import com.sample.popularmovies.services.models.videoapi.Videos;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by sriram on 15/4/16.
 */
public interface RestInterface {

    @GET("/movie/{sort}?api_key=" + BuildConfig.MOVIE_API_KEY)
    void getPopularMovies(@Path("sort") String sort, @Query("page") int pageNumber, Callback<Movies> cb);

    @GET("/movie/{id}/videos?api_key=" + BuildConfig.MOVIE_API_KEY)
    void getMovieTrailers(@Path("id") int id, Callback<Videos> cb);

    @GET("/movie/{id}/reviews?api_key=" + BuildConfig.MOVIE_API_KEY)
    void getMovieReviews(@Path("id") int id, Callback<Reviews> cb);

}
