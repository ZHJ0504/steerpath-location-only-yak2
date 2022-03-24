package com.example.yak2.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandAnimation extends Animation {

    public final static int COLLAPSE = 1;
    public final static int EXPAND = 0;

    private View mView;
    private int mEndHeight;
    private int mType;
    private LinearLayout.LayoutParams mLayoutParams;

    public ExpandAnimation(View view, int duration, int type) {

        setDuration(duration);
        mView = view;

        mView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mEndHeight = mView.getMeasuredHeight();

        mLayoutParams = ((LinearLayout.LayoutParams) view.getLayoutParams());
        mType = type;
        if(mType == EXPAND) {
            mLayoutParams.height = 0;
        } else {
            mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        view.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if (interpolatedTime < 1.0f) {
            if(mType == EXPAND) {
                mLayoutParams.height =  (int)(mEndHeight * interpolatedTime);
            } else {
                mLayoutParams.height = (int) (mEndHeight * (1 - interpolatedTime));
            }
            mView.requestLayout();
        } else {
            if(mType == EXPAND) {
                mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                mView.requestLayout();
            }else{
                mView.setVisibility(View.GONE);
            }
        }
    }
}
