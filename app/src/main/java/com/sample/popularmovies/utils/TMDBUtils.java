package com.sample.popularmovies.utils;

import android.util.DisplayMetrics;

import com.sample.popularmovies.BuildConfig;

/**
 * Created by sriram on 28/4/16.
 */
public class TMDBUtils {

    private static String YOU_TUBE = "YouTube";

    public static String getImageSizePath(int dpi) {
        switch (dpi) {
            case DisplayMetrics.DENSITY_LOW:
                return BuildConfig.IMAGE_BASE_URL_185;
            case DisplayMetrics.DENSITY_MEDIUM:
                return BuildConfig.IMAGE_BASE_URL_185;
            case DisplayMetrics.DENSITY_HIGH:
                return BuildConfig.IMAGE_BASE_URL_185;
            case DisplayMetrics.DENSITY_XHIGH:
                return BuildConfig.IMAGE_BASE_URL_342;
            case DisplayMetrics.DENSITY_XXHIGH:
                return BuildConfig.IMAGE_BASE_URL_500;
        }
        return BuildConfig.IMAGE_BASE_URL_185;
    }

    public static String getYouTubeImageThumbnail(int dpi, String site, String videoId) {
        if (YOU_TUBE.equals(site)) {
            switch (dpi) {
                case DisplayMetrics.DENSITY_LOW:
                    return String.format("https://i.ytimg.com/vi/%1$s/default.jpg", videoId);
                case DisplayMetrics.DENSITY_MEDIUM:
                    return String.format("https://i.ytimg.com/vi/%1$s/mqdefault.jpg", videoId);
                case DisplayMetrics.DENSITY_HIGH:
                    return String.format("https://i.ytimg.com/vi/%1$s/hqdefault.jpg", videoId);
                case DisplayMetrics.DENSITY_XHIGH:
                    return String.format("https://i.ytimg.com/vi/%1$s/sddefault.jpg", videoId);
                case DisplayMetrics.DENSITY_XXHIGH:
                    return String.format("https://i.ytimg.com/vi/%1$s/sddefault.jpg", videoId);
            }
            return String.format("https://i.ytimg.com/vi/%1$s/sddefault.jpg", videoId);
        } else {
            throw new UnsupportedOperationException("Only YouTube is supported!");
        }
    }
}
