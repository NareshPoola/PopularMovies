package com.sample.popularmovies.app;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sample.popularmovies.services.databases.MoviesContract;

/**
 * Created by naresh poola on 1/6/16.
 */
class ContentAsyncQueryHandler extends AsyncQueryHandler {

    private static final String TAG = ContentAsyncQueryHandler.class.getSimpleName();
    private ContentResolver contentResolver;

    public ContentAsyncQueryHandler(ContentResolver cr) {
        super(cr);
        contentResolver = cr;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        contentResolver.notifyChange(MoviesContract.MovieEntry.CONTENT_URI, null);
        Log.v(TAG, "Query Completed");
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        contentResolver.notifyChange(MoviesContract.MovieEntry.CONTENT_URI, null);
        Log.v(TAG, "Insert Completed");
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
        contentResolver.notifyChange(MoviesContract.MovieEntry.CONTENT_URI, null);
        Log.v(TAG, "Update Completed");
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
        contentResolver.notifyChange(MoviesContract.MovieEntry.CONTENT_URI, null);
        Log.v(TAG, "Delete Completed");
    }
}
