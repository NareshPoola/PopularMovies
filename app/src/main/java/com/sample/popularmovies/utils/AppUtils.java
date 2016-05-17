package com.sample.popularmovies.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sriram on 19/4/16.
 */
public class AppUtils {

    static SimpleDateFormat readFormat = new SimpleDateFormat("yyyy-mm-dd");
    static SimpleDateFormat writeFormat = new SimpleDateFormat("MMM dd, yyyy");

    public static String formateDate(String dateFormat){
        Date date = null;
        try {
            date = readFormat.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return writeFormat.format(date);
    }
}
