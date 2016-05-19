package com.sample.popularmovies.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sample.popularmovies.R;
import com.sample.popularmovies.services.models.videoapi.Result;
import com.sample.popularmovies.utils.TMDBUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by naresh poola on 15/4/16.
 */
public class MovieTrailersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<Result> videoList = new ArrayList<>();
    private Context mContext;
    MovieDetailsFragment.OnVideoSelectListener onVideoSelectListener;

    public MovieTrailersAdapter(Context context, MovieDetailsFragment.OnVideoSelectListener onVideoSelectListener) {
        this.mContext = context;
        this.onVideoSelectListener = onVideoSelectListener;
    }


    public class MovieTrailerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_trailer_image)
        public ImageView mImage;

        public MovieTrailerViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mImage.setOnClickListener(onClickListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onVideoSelectListener.onVideoSelected(v, (Result) v.getTag());
        }
    };


    public void updateData(List<Result> moviesList) {
        this.videoList = moviesList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return /*videoList.get(position)*/position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_movie_trailer_item, parent, false);
            return new MovieTrailerViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MovieTrailerViewHolder movieTrailerViewHolder = (MovieTrailerViewHolder) holder;
            Result video = videoList.get(position);
            Picasso.with(mContext).load(TMDBUtils.getYouTubeImageThumbnail(mContext.getResources().getDisplayMetrics().densityDpi, video.getSite(), video.getKey()))
                    .resizeDimen(R.dimen.video_width, R.dimen.video_height)
                    .into(movieTrailerViewHolder.mImage);

            movieTrailerViewHolder.mImage.setTag(video);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}
