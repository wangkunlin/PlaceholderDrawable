package com.wkl.drawabledemo;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * <p>
 * On 2019-06-28
 * <p>
 * 可以指定背景色以及圆角，并且中间有 logo 的 Drawable
 * 自定义 Drawable 的 xml 需要 build tools 的版本大于等于 24, 否则编译报错
 */
public class PlaceholderDrawable extends Drawable implements Drawable.Callback {

    private PlaceholderState mPlaceholderState;
    private boolean mMutated;

    private final Path mPath = new Path();
    private final RectF mRect = new RectF();

    /**
     * 必须 public 否则 api >= 24 的系统 反射会失败
     */
    public PlaceholderDrawable() {
        this(null, null);
    }

    private PlaceholderDrawable(PlaceholderState state, Resources res) {
        mPlaceholderState = createConstantState(state, res);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        PlaceholderState state = mPlaceholderState;
        Paint paint = state.mPaint;
        mRect.set(bounds);

        // 绘制圆角背景，如果需要的话
        if (state.mRadiusArray != null) {
            mPath.reset();
            mPath.addRoundRect(mRect, state.mRadiusArray, Path.Direction.CW);
            canvas.drawPath(mPath, paint);
        } else if (state.mRadius > 0.0f) {
            float rad = Math.min(state.mRadius,
                    Math.min(mRect.width(), mRect.height()) * 0.5f);
            canvas.drawRoundRect(mRect, rad, rad, paint);
        } else {
            if (paint.getColor() != 0 || paint.getShader() != null) {
                canvas.drawRect(mRect, paint);
            }
        }

        /*
         * 绘制中间的 logo, 如果有的话
         */
        if (state.mChild != null) {
            Drawable dr = state.mChild.mDrawable;
            if (dr == null) {
                return;
            }
            int w = dr.getIntrinsicWidth();
            int h = dr.getIntrinsicHeight();
            int left = bounds.centerX() - w / 2;
            int top = bounds.centerY() - h / 2;
            dr.setBounds(left, top, left + w, top + h);
            dr.draw(canvas);
        }

    }

    @Override
    public void setAlpha(int alpha) {
        final int oldAlpha = mPlaceholderState.mPaint.getAlpha();
        if (alpha != oldAlpha) {
            mPlaceholderState.mPaint.setAlpha(alpha);
            if (mPlaceholderState.mChild != null) {
                if (mPlaceholderState.mChild.mDrawable != null) {
                    mPlaceholderState.mChild.mDrawable.setAlpha(alpha);
                }
            }
            invalidateSelf();
        }
    }

    @Override
    public int getAlpha() {
        return mPlaceholderState.mPaint.getAlpha();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPlaceholderState.mPaint.setColorFilter(colorFilter);
        if (mPlaceholderState.mChild != null) {
            if (mPlaceholderState.mChild.mDrawable != null) {
                mPlaceholderState.mChild.mDrawable.setColorFilter(colorFilter);
            }
        }
        invalidateSelf();
    }

    @Nullable
    @Override
    public ColorFilter getColorFilter() {
        return mPlaceholderState.mPaint.getColorFilter();
    }

    @Override
    public int getOpacity() {
        return mPlaceholderState.getOpacity();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean changed = false;
        if (mPlaceholderState.mChild != null) {
            Drawable dr = mPlaceholderState.mChild.mDrawable;
            if (dr != null && dr.isStateful() && dr.setState(state)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        boolean changed = false;
        if (mPlaceholderState.mChild != null) {
            Drawable dr = mPlaceholderState.mChild.mDrawable;
            if (dr != null && dr.setLevel(level)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        invalidateSelf();
    }

    private void setCornerRadius(float radius) {
        mPlaceholderState.setCornerRadius(radius);
        invalidateSelf();
    }

    private void setCornerRadii(@Nullable float[] radii) {
        mPlaceholderState.setCornerRadii(radii);
        invalidateSelf();
    }

    @NonNull
    public static PlaceholderDrawable createFromXmlInner(@NonNull Resources r, @NonNull XmlPullParser parser,
                                                         @NonNull AttributeSet attrs, Resources.Theme theme) {
        final PlaceholderDrawable drawable = new PlaceholderDrawable();
        drawable.inflate(r, parser, attrs, theme);
        return drawable;
    }

    private static @NonNull
    TypedArray makeAttributes(@NonNull Resources res, @Nullable Resources.Theme theme,
                              @NonNull AttributeSet set) {
        if (theme == null) {
            return res.obtainAttributes(set, R.styleable.PlaceholderDrawable);
        }
        return theme.obtainStyledAttributes(set, R.styleable.PlaceholderDrawable, 0, 0);
    }

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs,
                        @Nullable Resources.Theme theme) {
        final PlaceholderState state = mPlaceholderState;
        final int density = resolveDensity2(r, 0);
        state.setDensity(density);

        TypedArray a = makeAttributes(r, theme, attrs);

        int radius = a.getDimensionPixelSize(R.styleable.PlaceholderDrawable_android_radius, 0);
        setCornerRadius(radius);

        final int topLeftRadius = a.getDimensionPixelSize(
                R.styleable.PlaceholderDrawable_android_topLeftRadius, radius);
        final int topRightRadius = a.getDimensionPixelSize(
                R.styleable.PlaceholderDrawable_android_topRightRadius, radius);
        final int bottomLeftRadius = a.getDimensionPixelSize(
                R.styleable.PlaceholderDrawable_android_bottomLeftRadius, radius);
        final int bottomRightRadius = a.getDimensionPixelSize(
                R.styleable.PlaceholderDrawable_android_bottomRightRadius, radius);
        if (topLeftRadius != radius || topRightRadius != radius ||
                bottomLeftRadius != radius || bottomRightRadius != radius) {
            // The corner radii are specified in clockwise order (see Path.addRoundRect())
            setCornerRadii(new float[]{
                    topLeftRadius, topLeftRadius,
                    topRightRadius, topRightRadius,
                    bottomRightRadius, bottomRightRadius,
                    bottomLeftRadius, bottomLeftRadius
            });
        }

        Drawable dr = a.getDrawable(R.styleable.PlaceholderDrawable_android_drawable);
        if (dr != null) {
            ChildDrawable childDrawable = new ChildDrawable(density);
            childDrawable.mDrawable = dr;
            state.mChild = childDrawable;
        }

        final Paint paint = state.mPaint;
        int color = paint.getColor();
        color = a.getColor(R.styleable.PlaceholderDrawable_android_color, color);
        paint.setColor(color);

        a.recycle();
    }

    @Nullable
    @Override
    public ConstantState getConstantState() {
        mPlaceholderState.mChangingConfigurations = getChangingConfigurations();
        return mPlaceholderState;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mPlaceholderState.getChangingConfigurations();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        mPlaceholderState.invalidateCache();
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (mPlaceholderState.mChild != null) {
            if (mPlaceholderState.mChild.mDrawable != null) {
                mPlaceholderState.mChild.mDrawable.setVisible(visible, restart);
            }
        }
        return changed;
    }

    @NonNull
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mPlaceholderState = createConstantState(mPlaceholderState, null);
            final ChildDrawable child = mPlaceholderState.mChild;
            Drawable dr = child.mDrawable;
            if (dr != null) {
                dr.mutate();
            }
            mMutated = true;
        }
        return this;
    }

    private PlaceholderState createConstantState(@Nullable PlaceholderState state, @Nullable Resources res) {
        return new PlaceholderState(state, this, res);
    }

    final static class PlaceholderState extends ConstantState {

        int mChangingConfigurations;
        ChildDrawable mChild;
        int mDensity;
        boolean mCheckedOpacity;
        int mOpacity;
        int mChildChangingConfigurations;
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float mRadius;
        private float[] mRadiusArray;

        PlaceholderState(@Nullable PlaceholderState orig, @NonNull PlaceholderDrawable owner,
                         @Nullable Resources res) {
            mDensity = resolveDensity2(res, orig != null ? orig.mDensity : 0);

            if (orig != null) {
                mChild = new ChildDrawable(orig.mChild, owner, res);
                mChangingConfigurations = orig.mChangingConfigurations;
                mChildChangingConfigurations = orig.mChildChangingConfigurations;
                mOpacity = orig.mOpacity;
                mRadius = orig.mRadius;
                if (orig.mRadiusArray != null) {
                    mRadiusArray = orig.mRadiusArray.clone();
                }
                if (orig.mDensity != mDensity) {
                    applyDensityScaling(orig.mDensity, mDensity);
                }

                mPaint.setColor(orig.mPaint.getColor());
                mPaint.setColorFilter(orig.mPaint.getColorFilter());
                mPaint.setAlpha(orig.mPaint.getAlpha());

            } else {
                mChild = null;
            }

            mPaint.setStyle(Paint.Style.FILL);

        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            if (mRadius > 0) {
                mRadius = scaleFromDensity3(mRadius, sourceDensity, targetDensity);
            }
            if (mRadiusArray != null) {
                mRadiusArray[0] = scaleFromDensity2(
                        (int) mRadiusArray[0], sourceDensity, targetDensity);
                mRadiusArray[1] = scaleFromDensity2(
                        (int) mRadiusArray[1], sourceDensity, targetDensity);
                mRadiusArray[2] = scaleFromDensity2(
                        (int) mRadiusArray[2], sourceDensity, targetDensity);
                mRadiusArray[3] = scaleFromDensity2(
                        (int) mRadiusArray[3], sourceDensity, targetDensity);
            }
        }

        void setCornerRadius(float radius) {
            if (radius < 0) {
                radius = 0;
            }
            mRadius = radius;
            mRadiusArray = null;
        }

        void setCornerRadii(float[] radii) {
            mRadiusArray = radii;
            if (radii == null) {
                mRadius = 0;
            }
        }

        final void setDensity(int targetDensity) {
            if (mDensity != targetDensity) {
                final int sourceDensity = mDensity;
                mDensity = targetDensity;
                applyDensityScaling(sourceDensity, targetDensity);
            }
        }

        @Override
        public boolean canApplyTheme() {
            return false;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new PlaceholderDrawable(this, null);
        }

        @NonNull
        @Override
        public Drawable newDrawable(@Nullable Resources res) {
            return new PlaceholderDrawable(this, res);
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations | mChildChangingConfigurations;
        }

        int getOpacity() {
            if (mCheckedOpacity) {
                return mOpacity;
            }

            int op;
            if (mChild != null && mChild.mDrawable != null) {
                op = mChild.mDrawable.getOpacity();
            } else {
                op = PixelFormat.TRANSPARENT;
            }

            mOpacity = Drawable.resolveOpacity(op, mOpacity);
            mCheckedOpacity = true;
            return op;
        }

        void invalidateCache() {
            mCheckedOpacity = false;
        }
    }

    final static class ChildDrawable {
        Drawable mDrawable;

        int mDensity;

        ChildDrawable(int density) {
            mDensity = density;
        }

        ChildDrawable(@NonNull ChildDrawable orig, @NonNull PlaceholderDrawable owner,
                      @Nullable Resources res) {
            final Drawable dr = orig.mDrawable;
            final Drawable clone;
            if (dr != null) {
                final ConstantState cs = dr.getConstantState();
                if (cs == null) {
                    clone = dr;
                    if (dr.getCallback() != null) {
                        // This drawable already has an owner.
                        Log.w("PlaceholderDrawable", "Invalid drawable added to PlaceholderDrawable! Drawable already "
                                        + "belongs to another owner but does not expose a constant state.",
                                new RuntimeException());
                    }
                } else if (res != null) {
                    clone = cs.newDrawable(res);
                } else {
                    clone = cs.newDrawable();
                }
                clone.setBounds(dr.getBounds());
                clone.setLevel(dr.getLevel());

                // Set the callback last to prevent invalidation from
                // propagating before the constant state has been set.
                clone.setCallback(owner);
            } else {
                clone = null;
            }

            mDrawable = clone;

            mDensity = resolveDensity2(res, orig.mDensity);
        }
    }

    private static int resolveDensity2(@Nullable Resources r, int parentDensity) {
        final int densityDpi = r == null ? parentDensity : r.getDisplayMetrics().densityDpi;
        return densityDpi == 0 ? DisplayMetrics.DENSITY_DEFAULT : densityDpi;
    }

    private static int scaleFromDensity2(int pixels, int sourceDensity, int targetDensity) {
        if (pixels == 0 || sourceDensity == targetDensity) {
            return pixels;
        }

        final float result = pixels * targetDensity / (float) sourceDensity;

        final int rounded = Math.round(result);
        if (rounded != 0) {
            return rounded;
        } else if (pixels > 0) {
            return 1;
        } else {
            return -1;
        }
    }

    private static float scaleFromDensity3(float pixels, int sourceDensity, int targetDensity) {
        return pixels * targetDensity / sourceDensity;
    }
}
