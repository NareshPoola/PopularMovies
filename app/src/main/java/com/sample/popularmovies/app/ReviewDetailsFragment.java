package com.sample.popularmovies.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.popularmovies.R;
import com.sample.popularmovies.services.models.reviewapi.Result;
import com.sample.popularmovies.utils.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReviewDetailsFragment extends Fragment implements AppConstants {


    private View rootView;
    @BindView(R.id.reviewer_name)
    TextView mReviewerName;
    @BindView(R.id.review_description)
    TextView mReviewDesc;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_review_detail, container, false);
            init();
        } else {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        }
        return rootView;
    }

    private void init() {
        ButterKnife.bind(this, rootView);
        Bundle bundle = getArguments();
        Result review = (Result) bundle.getParcelable(IBundleParams.RESULT_OBJ);
        mReviewerName.setText(review.getAuthor());
        mReviewDesc.setText(review.getContent());
    }


}
