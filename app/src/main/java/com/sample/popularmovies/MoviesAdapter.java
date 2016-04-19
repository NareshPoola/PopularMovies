package com.sample.popularmovies;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sample.popularmovies.Services.models.movieapi.Result;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by naresh poola on 15/4/16.
 */
public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private List<Result> moviesList = new ArrayList<>();
    private Context mContext;
    MoviesActivityFragment.OnItemClickListener onItemClickListener;

    public MoviesAdapter(Context context, MoviesActivityFragment.OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.onItemClickListener = onItemClickListener;
    }


    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public ImageView mImage;
        private CardView mCardView;

        public MovieViewHolder(View view) {
            super(view);
            mCardView = (CardView) view.findViewById(R.id.movie_card_view);
            mTitle = (TextView) view.findViewById(R.id.movie_name);
            mImage = (ImageView) view.findViewById(R.id.movie_image);
            mCardView.setOnClickListener(onClickListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, (Result) v.getTag());
        }
    };


    public void updateData(List<Result> moviesList) {
        this.moviesList = moviesList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return moviesList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_movie_item, parent, false);
            return new MovieViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            //        The base URL will look like: http://image.tmdb.org/t/p/.
//        Then you will need a ‘size’, which will be one of the following: "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
//        And finally the poster path returned by the query, in this case “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;
            Result movie = moviesList.get(position);
            movieViewHolder.mTitle.setText(movie.getOriginalTitle());
            Picasso.with(mContext).load(BuildConfig.IMAGE_BASE_URL_342+ movie.getPosterPath())
            .networkPolicy(NetworkPolicy.OFFLINE).
                    into(movieViewHolder.mImage);
            movieViewHolder.mCardView.setTag(movie);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
