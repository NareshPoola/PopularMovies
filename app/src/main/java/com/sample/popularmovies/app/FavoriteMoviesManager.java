package com.sample.popularmovies.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import com.sample.popularmovies.services.databases.MoviesContract;
import com.sample.popularmovies.services.models.movieapi.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sriram on 12/5/16.
 */
public class FavoriteMoviesManager {
    private static final SQLiteQueryBuilder sQueryBuilder;
    private static FavoriteMoviesManager sMoviesManager;

    static {
        sQueryBuilder = new SQLiteQueryBuilder();
        sQueryBuilder.setTables(MoviesContract.MovieEntry.TABLE_NAME);
    }

    private final ContentResolver mContentResolver;

    private final ContentAsyncQueryHandler contentAsyncQueryHandler;

    private FavoriteMoviesManager(Context context) {
        mContentResolver = context.getContentResolver();
        contentAsyncQueryHandler = new ContentAsyncQueryHandler(mContentResolver);
    }

    public static FavoriteMoviesManager create(Context context) {
        if (sMoviesManager == null) {
            sMoviesManager = new FavoriteMoviesManager(context);
        }
        return sMoviesManager;
    }

    public void add(Result movie) {
        ContentValues values = new ContentValues();
        values.put(MoviesContract.MovieEntry.COLUMN_ID, movie.getId());
        values.put(MoviesContract.MovieEntry.COLUMN_TITLE, movie.getOriginalTitle());
        values.put(MoviesContract.MovieEntry.COLUMN_RATING, movie.getVoteAverage());
        values.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(MoviesContract.MovieEntry.COLUMN_PLOT, movie.getOverview());
        values.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, movie.getPosterPath());
        values.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL, movie.getBackdropPath());
        contentAsyncQueryHandler.startInsert(1, null, MoviesContract.MovieEntry.CONTENT_URI, values);
    }

    public void remove(Result movie) {
        contentAsyncQueryHandler.startDelete(1, null,MoviesContract.MovieEntry.CONTENT_URI, MoviesContract.MovieEntry.COLUMN_ID + " = " + movie.getId(), null);
    }

    public boolean isFavorite(Result movie) {
        Cursor cursor = mContentResolver.query(MoviesContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(movie.getId())).build(), null, null, null, null);
        try {
            if (cursor != null) {
                if (cursor.getCount() == 1) {
                    cursor.close();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //always close your curser to avoid memory leaks.
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public List<Result> getAllFavoriteMovies(Cursor cursor) {
        List<Result> movies = new ArrayList<>();
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Result movie = new Result();
                    movie.setOriginalTitle(cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE)));
                    movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RATING)));
                    movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE)));
                    movie.setOverview(cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_PLOT)));
                    movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER_URL)));
                    movie.setBackdropPath(cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL)));
                    movie.setId(cursor.getInt(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_ID)));
                    movies.add(movie);
                }
            }
            Collections.reverse(movies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movies;
    }

}