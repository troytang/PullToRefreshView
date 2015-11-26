package com.tangwy.pulltorefreshview;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Created by Troy Tang on 2015-11-19.
 */
public class PullToRefreshListView extends ListView {

    public static final int START_DURATION = 300;

    public static final int SCALE_NORMAL = 2;
    public static final int SCALE_OVER = 3;

    private OnRefreshListener mOnRefreshListener;
    private ImageView mImageView;
    private boolean mIsRefreshOpen = true;
    private boolean mIsRefreshing = false;
    private boolean mIsChanging = false;
    private boolean mIsOutOfItem = false;
    private int mInitialPointerId = -1;
    private float mInitialMotionY;
    private float mLastMotionY;
    private int mImageViewHeight = -1;
    private int mDrawableMaxHeight = -1;
    private int mDefaultImageViewHeight = 0;
    private int mRefreshHeight = -1;

    private int mTotalDragDistance;

    private View mHeader;
    private BaseDrawable mDrawable;

    public PullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void refreshComplete() {
        setRefreshing(false);
        mDrawable.stop();
        if (getChildCount() > 0 && getFirstVisiblePosition() != 0) {
            mImageView.getLayoutParams().height = 0;
            mImageView.requestLayout();
            mIsChanging = false;
        } else {
            ResetAnimation animation = new ResetAnimation(mImageView, mImageViewHeight);
            animation.setDuration(300);
            startAnimation(mImageView, animation);
        }
    }

    public void setOnRefreshListener(OnRefreshListener mOnRefreshListener) {
        this.mOnRefreshListener = mOnRefreshListener;
    }

    public boolean isRefreshOpen() {
        return mIsRefreshOpen;
    }

    public void setRefreshOpen(boolean mIsRefreshOpen) {
        this.mIsRefreshOpen = mIsRefreshOpen;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public void startRefresh() {
        if (this.mIsRefreshing) {
            return;
        }
        this.mIsRefreshing = true;
        if (getChildCount() > 0 && getFirstVisiblePosition() != 0) {
            mImageView.getLayoutParams().height = mTotalDragDistance;
            mImageView.requestLayout();
            mIsChanging = true;
            mDrawable.start();
            mOnRefreshListener.onRefresh();
        } else {
            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    mImageView.getLayoutParams().height = (int) (mTotalDragDistance * interpolatedTime);
                    mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
                    mImageView.requestLayout();
                }
            };
            animation.setDuration(300);
            animation.setRepeatCount(0);
            animation.setInterpolator(new LinearInterpolator());
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mIsChanging = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIsChanging = false;
                    mDrawable.start();
                    mOnRefreshListener.onRefresh();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mHeader.startAnimation(animation);
        }
    }

    public void addHeaderAbove(View view) {
        removeHeaderView(mHeader);
        addHeaderView(view);
        addHeaderView(mHeader);
    }

    public void addHeaderAbove(View view, Object data, boolean isSelectable) {
        removeHeaderView(mHeader);
        addHeaderView(view, data, isSelectable);
        removeHeaderView(mHeader);
    }

    private void init(Context context) {
        mDefaultImageViewHeight = context.getResources().getDimensionPixelOffset(R.dimen.size_default_height);
        mTotalDragDistance = context.getResources().getDimensionPixelSize(R.dimen.size_default_height);
        mHeader = LayoutInflater.from(getContext()).inflate(R.layout.refresh_header, null);
        mImageView = (ImageView) mHeader.findViewById(R.id.layout_header_image);
        mDrawable = new BallsDrawable(mImageView, mTotalDragDistance);
        mImageView.setImageDrawable(mDrawable);
        setRefreshImageView(mImageView);
        addHeaderView(mHeader);
        post(new Runnable() {
            @Override
            public void run() {
                setViewBounds();
            }
        });
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                   int maxOverScrollY, boolean isTouchEvent) {
        Log.e(PullToRefreshListView.class.getName(), "overScrollBy");
        boolean isCollapseAnimation = false;

        isCollapseAnimation = onOverScrollByListener.onOverScrollBy(deltaX, deltaY, scrollX,
                scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent)
                || isCollapseAnimation;

        return isCollapseAnimation ? true : super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
                scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        View firstView = (View) mImageView.getParent();
        // firstView.getTop < getPaddingTop means mImageView will be covered by top padding,
        // so we can layout it to make it shorter
        if (firstView.getTop() < getPaddingTop() && mImageView.getHeight() > mImageViewHeight && !isRefreshing()) {
            // to set the firstView.mTop to 0,
            // maybe use View.setTop() is more easy, but it just support from Android 3.0 (API 11)
            if (!mIsChanging) {
                Log.d("findbugs", "scroll changed");
                mImageView.getLayoutParams().height = Math.max(mImageView.getHeight() - (getPaddingTop() - firstView.getTop()), mImageViewHeight);
                firstView.layout(firstView.getLeft(), 0, firstView.getRight(), firstView.getHeight());
                mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
            }
            mImageView.requestLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        onTouchEventListener.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    private void setRefreshImageView(ImageView iv) {
        mImageView = iv;
    }

    private void setViewBounds() {
        if (-1 == mImageViewHeight) {
            mImageViewHeight = mImageView.getHeight();
            if (mImageViewHeight <= 0) {
                mImageViewHeight = 0;
            }
        }
        mDrawableMaxHeight = mTotalDragDistance * 2;
    }

    private OnTouchEventListener onTouchEventListener = new OnTouchEventListener() {
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (MotionEvent.ACTION_UP == event.getAction()) {
                Log.d("findbugs", "action up");
                mIsOutOfItem = false;
                if (!isRefreshing()) {
                    if (mImageView.getHeight() > mTotalDragDistance && isRefreshOpen()) {
                        /* 开始刷新 */
                        setRefreshing(true);
                        mOnRefreshListener.onRefresh();

                        ResetAnimation animation = new ResetAnimation(mImageView, mTotalDragDistance);
                        animation.setDuration(300);
                        startAnimation(mImageView, animation);
                    } else {
                        if (mImageView.getHeight() > mImageViewHeight) {
                            ResetAnimation animation = new ResetAnimation(mImageView,
                                    mImageViewHeight);
                            animation.setDuration(300);
                            startAnimation(mImageView, animation);
                        }
                    }
                }
            } else if (MotionEvent.ACTION_DOWN == event.getAction() && !isRefreshing()) {
                // Find the child view that was touched (perform a hit test)
                mInitialPointerId = MotionEventCompat.getPointerId(event, 0);
                mInitialMotionY = getMotionEventY(event, mInitialPointerId);
                mLastMotionY = mInitialMotionY;
                boolean onItem = false;
                Rect rect = new Rect();
                int childCount = getChildCount();
                int[] listViewCoords = new int[2];
                getLocationOnScreen(listViewCoords);
                int x = (int) event.getRawX() - listViewCoords[0];
                int y = (int) event.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        onItem = true;
                        break;
                    }
                }
                mIsOutOfItem = !onItem;
            } else if (MotionEvent.ACTION_MOVE == event.getAction() && mIsOutOfItem) {
                final int pointerIndex = MotionEventCompat.findPointerIndex(event, mInitialPointerId);
                if (0 <= pointerIndex) {
                    final float y = MotionEventCompat.getY(event, pointerIndex);
                    final float deltaY = y - mLastMotionY;
                    mLastMotionY = y;
                    if (deltaY > 0) {
                        if (mImageView.getHeight() < mTotalDragDistance) {
                            mImageView.getLayoutParams().height = mImageView.getHeight() + (int) deltaY / SCALE_NORMAL;
                        } else {
                            mImageView.getLayoutParams().height = mImageView.getHeight() + (int) deltaY / SCALE_OVER;
                        }
                        mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
                        mImageView.requestLayout();
                    } else {
                        if (mImageView.getHeight() > mImageViewHeight) {
                            mImageView.getLayoutParams().height = mImageView.getHeight() + (int) deltaY
                                    > mImageViewHeight ? mImageView.getHeight() + (int) deltaY
                                    : mImageViewHeight;
                            mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
                            mImageView.requestLayout();
                        }
                    }
                }
            }
        }
    };

    private OnOverScrollByListener onOverScrollByListener = new OnOverScrollByListener() {
        @Override
        public boolean onOverScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                      int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                      int maxOverScrollY, boolean isTouchEvent) {
            if (true == isTouchEvent && !isRefreshing()) {
                if (deltaY < 0) {
                    Log.d("findbugs", "over scroll by");
                    if (mImageView.getHeight() < mTotalDragDistance) {
                        mImageView.getLayoutParams().height = mImageView.getHeight() - deltaY / SCALE_NORMAL;
                    } else {
                        mImageView.getLayoutParams().height = mImageView.getHeight() - deltaY / SCALE_OVER;
                    }
                    mImageView.requestLayout();
                    mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
                } else {
                    if (mImageView.getHeight() > mImageViewHeight) {
                        mImageView.getLayoutParams().height = mImageView.getHeight() - deltaY
                                > mImageViewHeight ? mImageView.getHeight() - deltaY
                                : mImageViewHeight;
                        mDrawable.setPercent((float) mImageView.getHeight() / (float) mTotalDragDistance);
                        mImageView.requestLayout();
                        return true;
                    }
                }
            }
            return false;
        }
    };



    private void setRefreshing(boolean mIsRefreshing) {
        this.mIsRefreshing = mIsRefreshing;
    }

    private void startAnimation(View view, Animation animation) {
        mIsChanging = true;
        view.startAnimation(animation);
    }

    private float getMotionEventY(MotionEvent event, int pointerId) {
        int pointerIndex = MotionEventCompat.findPointerIndex(event, pointerId);
        if (-1 != pointerIndex) {
            return MotionEventCompat.getY(event, pointerIndex);
        } else {
            return -1;
        }
    }

    private interface OnOverScrollByListener {
        boolean onOverScrollBy(int deltaX, int deltaY, int scrollX,
                               int scrollY, int scrollRangeX, int scrollRangeY,
                               int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent);
    }

    private interface OnTouchEventListener {
        void onTouchEvent(MotionEvent event);
    }

    private class ResetAnimation extends Animation {
        int targetHeight;
        int originalHeight;
        int extraHeight;
        View mView;

        protected ResetAnimation(View view, int targetHeight) {
            this.mView = view;
            this.targetHeight = targetHeight;
            originalHeight = view.getHeight();
            extraHeight = this.targetHeight - originalHeight;
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIsChanging = false;
                    if (mIsRefreshing) {
                        mDrawable.start();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, /* interpolatedTime从0到1 */
                                           Transformation t) {
            int newHeight;
            newHeight = (int) (targetHeight - (float) extraHeight * (1f - interpolatedTime));
            mView.getLayoutParams().height = newHeight;
            mView.requestLayout();
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}
