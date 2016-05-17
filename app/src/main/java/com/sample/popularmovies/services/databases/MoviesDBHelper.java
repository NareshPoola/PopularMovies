package com.sample.popularmovies.services.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by sriram on 11/5/16.
 */
public class MoviesDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movies.db";

    public MoviesDBHelper(Context context) {
        //Context context, String name, SQLiteDatabase.CursorFactory factory, int version
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = generateCreateTableQuery(MoviesContract.MovieEntry.TABLE_NAME, new String[]{
                MoviesContract.MovieEntry._ID,
                MoviesContract.MovieEntry.COLUMN_ID,
                MoviesContract.MovieEntry.COLUMN_TITLE,
                MoviesContract.MovieEntry.COLUMN_RATING,
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                MoviesContract.MovieEntry.COLUMN_PLOT,
                MoviesContract.MovieEntry.COLUMN_POSTER_URL,
                MoviesContract.MovieEntry.COLUMN_BACKDROP_URL});
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private String generateCreateTableQuery(String tableName, String[] columnNames) {
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append(tableName).append("(");
        builder.append(BaseColumns._ID);
        builder.append(" INT PRIMARY KEY, ");
        for (int i = 1; i < columnNames.length; i++) {
            builder.append(columnNames[i]);
            if (i != columnNames.length - 1) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }
}
