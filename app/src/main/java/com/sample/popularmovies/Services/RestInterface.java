package com.sample.popularmovies.Services;

import com.sample.popularmovies.BuildConfig;
import com.sample.popularmovies.Services.models.movieapi.Movies;
import com.sample.popularmovies.Services.models.videoapi.Videos;

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


}
