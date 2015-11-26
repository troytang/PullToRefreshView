package com.tangwy.pulltorefreshview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Created by Troy Tang on 2015-10-13.
 */
public class BallsDrawable extends BaseDrawable {

    private final int STATUS_NARMAL = 0;
    private final int STATUS_IN = 1;
    private final int STATUS_OUT = 2;
    private final int STATUS_RUN = 3;

    private final int BALL_IN_DURATION = 450;
    private final int BALL_OUT_DURATION = 250;
    private final int BALL_RUN_DURATION = 500;

    private int mViewWidth;
    private int mViewHeight;
    private float mInPercent;
    private float mRunPercent;
    private int mBlueLeft;
    private int mOrangeLeft;

    private Bitmap mBlueBall;
    private Bitmap mOrangeBall;

    private Animation mInAnimation;
    private Animation mOutAnimation;
    private Animation mRunAnimation;

    private int mDistance;
    private int mBallSize;

    private int mStatus = STATUS_NARMAL;

    public BallsDrawable(ImageView imageView, final int height) {
        super(imageView);

        mDistance = getContext().getResources().getDimensionPixelSize(R.dimen.custom_balls_distance);
        mBallSize = getContext().getResources().getDimensionPixelSize(R.dimen.custom_ball_size);

        setupAnimations();
        mParentView.post(new Runnable() {
            @Override
            public void run() {
                initDimensions(mParentView.getWidth(), height);
            }
        });
    }

    @Override
    public void setPercent(float percent) {
        ballsIn(Math.min(percent, 1.0f));
    }

    @Override
    public void start() {
        mStatus = STATUS_RUN;
        mRunAnimation.reset();
        mParentView.startAnimation(mRunAnimation);
    }

    @Override
    public void stop() {
//        mStatus = STATUS_OUT;
//        mOutAnimation.reset();
//        mParentView.startAnimation(mOutAnimation);
        mStatus = STATUS_NARMAL;
    }

    @Override
    public void draw(Canvas canvas) {

        final int saveCount = canvas.save();

        drawOrange(canvas);
        drawBlue(canvas);

        canvas.restoreToCount(saveCount);
    }

    private void setupAnimations() {
        mInAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ballsIn(interpolatedTime);
            }
        };
        mInAnimation.setRepeatCount(0);
        mInAnimation.setDuration(BALL_IN_DURATION);
        mInAnimation.setInterpolator(new LinearInterpolator());
        mInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRunAnimation.reset();
                mStatus = STATUS_RUN;
                mParentView.startAnimation(mRunAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mOutAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ballsOut(interpolatedTime);
            }
        };
        mOutAnimation.setRepeatCount(0);
        mOutAnimation.setDuration(BALL_OUT_DURATION);
        mOutAnimation.setInterpolator(new LinearInterpolator());
        mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRunAnimation.reset();
                mStatus = STATUS_NARMAL;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mRunAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                ballsRun(interpolatedTime);
            }
        };
        mRunAnimation.setRepeatCount(Animation.INFINITE);
        mRunAnimation.setRepeatMode(Animation.REVERSE);
        mRunAnimation.setInterpolator(new LinearInterpolator());
        mRunAnimation.setDuration(BALL_RUN_DURATION);
    }

    private void initDimensions(int viewWidth, int viewHeight) {
        if (0 == viewWidth || mViewWidth == viewWidth) {
            return;
        }

        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        mBlueLeft = (viewWidth - mDistance) / 2 - mBallSize / 2;
        mOrangeLeft = viewWidth / 2 + mDistance / 2 - mBallSize / 2;

        createBitmaps();
    }

    private void createBitmaps() {
        mBlueBall = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blue);
        mOrangeBall = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.orange);
        mBlueBall = Bitmap.createScaledBitmap(mBlueBall, mBallSize, mBallSize, true);
        mOrangeBall = Bitmap.createScaledBitmap(mOrangeBall, mBallSize, mBallSize, true);
    }

    private void ballsIn(float percent) {
        mInPercent = percent;
        invalidateSelf();
    }

    private void ballsOut(float percent) {

    }

    private void ballsRun(float percent) {
        mRunPercent = percent;
        invalidateSelf();
    }

    private void drawBlue(Canvas canvas) {
        switch (mStatus) {
            case STATUS_NARMAL:
            case STATUS_IN:{
                Matrix matrix = new Matrix();
                matrix.postTranslate(mBlueLeft * mInPercent,
                        mInPercent * (mViewHeight / 2 - mInPercent * mBallSize / 2) - mBallSize * (1.0f - mInPercent));
                canvas.drawBitmap(mBlueBall, matrix, null);
            }
                break;
            case STATUS_OUT:{

            }
                break;
            case STATUS_RUN:{
                Matrix matrix = new Matrix();
                matrix.postTranslate(mBlueLeft + mDistance * mRunPercent, mViewHeight / 2 - mBallSize / 2);
                canvas.drawBitmap(mBlueBall, matrix, null);
            }
                break;
        }
    }

    private void drawOrange(Canvas canvas) {
        switch (mStatus) {
            case STATUS_NARMAL:
            case STATUS_IN:{
                Matrix matrix = new Matrix();
                matrix.postTranslate(mViewWidth - mInPercent * (mViewWidth - mOrangeLeft) ,
                        mInPercent * (mViewHeight / 2 - mInPercent * mBallSize / 2) - mBallSize * (1.0f - mInPercent));
                canvas.drawBitmap(mOrangeBall, matrix, null);
            }
            break;
            case STATUS_OUT:{

            }
            break;
            case STATUS_RUN:{
                Matrix matrix = new Matrix();
                matrix.postTranslate(mOrangeLeft - mDistance * mRunPercent, mViewHeight / 2 - mBallSize / 2);
                canvas.drawBitmap(mOrangeBall, matrix, null);
            }
            break;
        }
    }
}
