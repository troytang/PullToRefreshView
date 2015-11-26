package com.tangwy.pulltorefreshview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

/**
 * Created by Troy Tang on 2015-10-13.
 */
public abstract class BaseDrawable extends Drawable implements Drawable.Callback, Animatable {

    public ImageView mParentView;

    public BaseDrawable(ImageView imageView) {
        mParentView = imageView;
    }

    public Context getContext() {
        return null != mParentView ? mParentView.getContext() : null;
    }

    public abstract void setPercent(float percent);

    @Override
    public boolean isRunning() {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void invalidateDrawable(Drawable who) {
        Callback callback = getCallback();
        if (null != callback) {
            callback.invalidateDrawable(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Callback callback = getCallback();
        if (null != callback) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Callback callback = getCallback();
        if (null != callback) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
