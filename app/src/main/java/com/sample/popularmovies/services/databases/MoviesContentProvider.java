package com.sample.popularmovies.services.databases;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sample.popularmovies.services.databases.MoviesContract.MovieEntry;

/**
 * Created by sriram on 12/5/16.
 */
public class MoviesContentProvider extends ContentProvider {
    public static final int CODE_MOVIES = 101;
    public static final int CODE_MOVIE = 102;
    private static final SQLiteQueryBuilder sQueryBuilder;
    public static final String TAG = "MoviesContentProvider";

    static {
        sQueryBuilder = new SQLiteQueryBuilder();
        sQueryBuilder.setTables(MovieEntry.TABLE_NAME);
    }

    private UriMatcher mUriMatcher = buildUriMatcher();
    private MoviesDBHelper mMoviesDBHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIE, CODE_MOVIES);
        matcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIE + "/#", CODE_MOVIE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMoviesDBHelper = new MoviesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "Attempting to query for Uri: " + uri);
        // FIXME: fix the selection parameter
        final String singleMovieQuerySelection = MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ID + " = " + extractMovieId(uri);
        int match = mUriMatcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case CODE_MOVIE:
                Log.v(TAG, "matched for a single movie with id: " + extractMovieId(uri));
                cursor = sQueryBuilder.query(mMoviesDBHelper.getReadableDatabase(), null, singleMovieQuerySelection, new String[]{}, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case CODE_MOVIES:
                Log.v(TAG, "matched for a all movies");
                cursor = sQueryBuilder.query(mMoviesDBHelper.getReadableDatabase(), null, null, null, null, null, null);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                Log.w(TAG, "No match found for uri: " + uri);
                return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case CODE_MOVIE:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case CODE_MOVIES:
                return MovieEntry.CONTENT_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mMoviesDBHelper.getWritableDatabase().insert(MovieEntry.TABLE_NAME, null, values);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return mMoviesDBHelper.getWritableDatabase().delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String extractMovieId(Uri uri) {
        return uri.getLastPathSegment();
    }
}
