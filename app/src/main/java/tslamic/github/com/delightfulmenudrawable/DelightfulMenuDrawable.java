/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Tadej Slamic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package tslamic.github.com.delightfulmenudrawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/*
 * To understand how it works, read the following blog post:
 *      http://tslamic.github.io/x
 */
public class DelightfulMenuDrawable extends Drawable {

    private static final int DEFAULT_DRAWABLE_SIZE = 48;
    private static final float LINE_LENGTH = 0.60f;
    private static final float LINE_WIDTH = .05f;
    private static final float LINE_GAP = 0.15f;

    private final Paint mPaint;

    private Animation mAnimation = Animation.BOTTOM_FULL;
    private float mArrowFixFactor;
    private float mHalfLineLength;
    private float mProgress;
    private float mLineGap;
    private float mCenterY;
    private float mBottomY;
    private float mStartX;
    private float mEndX;
    private float mTopY;

    private boolean mIsRightToLeft;
    private boolean mIsBack;

    public DelightfulMenuDrawable(Context context) {
        this(getDefaultDpSize(context));
    }

    public DelightfulMenuDrawable(int edge) {
        mPaint = getDefaultPaint();
        setBounds(0, 0, edge, edge);
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (null == bounds) {
            throw new IllegalArgumentException("bounds are null");
        }
        measure();
    }

    @Override
    public void draw(Canvas canvas) {
        final float dx = mProgress * mHalfLineLength;
        final float dy = mProgress * (mHalfLineLength - mLineGap);
        final float dg = mProgress * mLineGap;
        final float dz = mProgress * mArrowFixFactor;

        final float rotationProgress = mIsRightToLeft ? 1 - mProgress : mProgress;
        final float rotation = rotationProgress * mAnimation.toDegrees(mIsBack);
        final float pivotX = getBounds().centerX();
        final float pivotY = getBounds().centerY();

        canvas.save();
        canvas.rotate(rotation, pivotX, pivotY);
        canvas.drawLine(mStartX + dx, mTopY - dy, mEndX + dz, mTopY + dg + dz, mPaint);
        canvas.drawLine(mStartX, mCenterY, mEndX, mCenterY, mPaint);
        canvas.drawLine(mStartX + dx, mBottomY + dy, mEndX + dz, mBottomY - dg - dz, mPaint);
        canvas.restore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpacity() {
        final int pixelFormat;
        switch (mPaint.getAlpha()) {
            case 0:
                pixelFormat = PixelFormat.TRANSPARENT;
                break;
            case 255:
                pixelFormat = PixelFormat.OPAQUE;
                break;
            default:
                pixelFormat = PixelFormat.TRANSLUCENT;
                break;
        }
        return pixelFormat;
    }

    /**
     * Sets the current animation progress.
     * <p/>
     * The progress should be between 0 and 1, starting at 0 to go from menu to back-arrow,
     * or 1 to 0 to go from back-arrow to menu.
     * <p/>
     * To obtain the appropriate Animator instance, call <code>getAnimator</code>.
     *
     * @param progress current progress
     */
    public void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    /**
     * Returns the current animation progress.
     *
     * @return current animation progress.
     */
    public float getProgress() {
        return mProgress;
    }

    /**
     * Returns the appropriate Animator for this instance.
     *
     * @param duration animation duration in millis.
     * @return the Animator instance.
     */
    public Animator getAnimator(long duration) {
        final Animator animator;
        if (mIsBack) {
            animator = ObjectAnimator.ofFloat(this, "progress", 1, 0);
        } else {
            animator = ObjectAnimator.ofFloat(this, "progress", 0, 1);
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsBack = !mIsBack;
            }
        });
        animator.setDuration(duration);
        return animator;
    }

    /**
     * Determines if the back arrow is currently shown.
     *
     * @return true if back arrow is currently shown, false otherwise.
     */
    public boolean isShowingBackArrow() {
        return mIsBack;
    }

    /**
     * Sets the animation type.
     *
     * @param animation animation type.
     */
    public void setAnimation(Animation animation) {
        if (null == animation) {
            throw new IllegalArgumentException("animation is null");
        }
        mAnimation = animation;
    }

    /**
     * Sets this drawable to adhere to right-to-left layout direction.
     *
     * @param isRtl true if this drawable should be right-to-left, false otherwise.
     */
    protected void setRtlLayoutDirection(boolean isRtl) {
        mIsRightToLeft = isRtl;
    }

    /**
     * Measures the drawable components.
     */
    private void measure() {
        final Rect bounds = getBounds();
        final float w = bounds.width();
        final float h = bounds.height();

        final float lineLength = LINE_LENGTH * w;
        mHalfLineLength = lineLength / 2;
        mLineGap = LINE_GAP * h;

        final float strokeWidth = LINE_WIDTH * h;
        mPaint.setStrokeWidth(strokeWidth);

        mArrowFixFactor = (float) ((Math.sqrt(2) / 4) * strokeWidth);
        mCenterY = w / 2;
        mStartX = bounds.left + (w - lineLength) / 2;
        mEndX = mStartX + lineLength;
        mTopY = mCenterY - mLineGap;
        mBottomY = mCenterY + mLineGap;

        invalidateSelf();
    }

    /**
     * Returns the default width and height for this instance, if none is given,
     * in density independent pixels.
     *
     * @param context Context instance.
     * @return the default width/height for this instance in dp.
     */
    private static int getDefaultDpSize(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (DEFAULT_DRAWABLE_SIZE * scale + 0.5f);
    }

    /**
     * Returns the default Paint object for any DelightfulMenuDrawable instance.
     *
     * @return the Paint object.
     */
    private static Paint getDefaultPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeJoin(Paint.Join.MITER);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    public static enum Animation {

        /**
         * Rotates between 0 and 180 when flipping.
         */
        TOP,

        /**
         * Rotates between 0 and 360 when flipping.
         */
        TOP_FULL,

        /**
         * Rotates between 360 and 180 when flipping.
         */
        BOTTOM,

        /**
         * Rotates between 360 and 0 when flipping.
         */
        BOTTOM_FULL;

        /**
         * Returns the degrees to animate to.
         *
         * @param isShowingBackArrow true if a drawable is currently showing the back icon.
         * @return the degrees to animate to.
         */
        private int toDegrees(boolean isShowingBackArrow) {
            final int degrees;
            if (isShowingBackArrow) {
                switch (this) {
                    case TOP:
                    case BOTTOM_FULL:
                        degrees = -180;
                        break;
                    default:
                        degrees = 180;
                        break;
                }
            } else {
                switch (this) {
                    case TOP:
                    case TOP_FULL:
                        degrees = -180;
                        break;
                    default:
                        degrees = 180;
                        break;
                }
            }
            return degrees;
        }

    }

}
