package com.github.fafaldo.blurzoomgallery.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v8.renderscript.RenderScript;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.github.fafaldo.blurzoomgallery.R;
import com.github.fafaldo.blurzoomgallery.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by fafik on 2015-08-22.
 */
public class BlurZoomCoordinatorLayout extends CoordinatorLayout {
    private float collapsedListHeight = 112;

    private AppBarLayout appBarLayout;
    private View toolbar;
    private View placeholder;
    private View scrollView;
    private View galleryView;
    private View galleryContainerView;

    private View collapseView;

    private float toolbarCollapseHeight;
    private float listCollapseHeight;
    private float galleryContainerHeight;
    private float galleryContainerWidth;

    private int currentOffset = 0;
    private boolean isExpandedViaClick = false;
    private boolean isCollapsedViaClick = false;

    private RenderScript renderScript;
    private ArrayList<Bitmap> blurBitmaps = new ArrayList<>();
    private ArrayList<ImageView> blurViews = new ArrayList<>();
    private float maxBlurRadius = 4f;
    private int blurSteps = 5;

    private int bitmapSizeDivide = 5;
    private boolean blurEnable = true;
    private boolean scaleEnable = true;
    private float maxScale = 1.15f;
    private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;

    private OnStateChangedListener listener;

    private int duration = -1;
    private Interpolator interpolator;

    public BlurZoomCoordinatorLayout(Context context) {
        this(context, null);
    }

    public BlurZoomCoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurZoomCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        parseAttrs(attrs);

        renderScript = RenderScript.create(getContext());
    }

    private void parseAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BlurZoomCoordinatorLayout);

        collapsedListHeight = a.getDimension(R.styleable.BlurZoomCoordinatorLayout_collapsedListHeight, collapsedListHeight);
        maxBlurRadius = a.getFloat(R.styleable.BlurZoomCoordinatorLayout_maxBlurRadius, maxBlurRadius);
        blurSteps = a.getInt(R.styleable.BlurZoomCoordinatorLayout_blurSteps, blurSteps);
        bitmapSizeDivide = a.getInt(R.styleable.BlurZoomCoordinatorLayout_bitmapSizeDivide, bitmapSizeDivide);
        blurEnable = a.getBoolean(R.styleable.BlurZoomCoordinatorLayout_blurEnable, blurEnable);
        scaleEnable = a.getBoolean(R.styleable.BlurZoomCoordinatorLayout_scaleEnable, scaleEnable);
        maxScale = a.getFloat(R.styleable.BlurZoomCoordinatorLayout_maxScale, maxScale);
        int st = a.getInt(R.styleable.BlurZoomCoordinatorLayout_android_scaleType, scaleType.ordinal());
        scaleType = ImageView.ScaleType.values()[st];

        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        appBarLayout = (AppBarLayout) findViewById(R.id.gallery_coordinator_appbarlayout);
        if(appBarLayout == null) {
            throw new IllegalStateException("No AppBarLayout found in BlurZoomCoordinatorLayout");
        }

        scrollView = findViewById(R.id.gallery_coordinator_scroll);
        if(scrollView == null) {
            throw new IllegalStateException("No scrolling view found in BlurZoomCoordinatorLayout");
        }

        toolbar = findViewById(R.id.gallery_coordinator_toolbar);
        if(toolbar == null) {
            throw new IllegalStateException("No toolbar found in BlurZoomCoordinatorLayout");
        }

        placeholder = findViewById(R.id.gallery_coordinator_placeholder);
        if(placeholder == null) {
            throw new IllegalStateException("No placeholder view found in scrolling view in BlurZoomCoordinatorLayout");
        }

        galleryContainerView = findViewById(R.id.gallery_coordinator_gallery_container);
        if(galleryContainerView == null) {
            throw new IllegalStateException("No gallery container view found in BlurZoomCoordinatorLayout");
        }

        galleryView = galleryContainerView.findViewById(R.id.gallery_coordinator_gallery);
        if(galleryView == null) {
            throw new IllegalStateException("No gallery view found in container in BlurZoomCoordinatorLayout");
        }

        prepareViews();

        prepareHeights();

        appBarLayout.addOnOffsetChangedListener(offsetChangedListener);

        appBarLayout.setOnClickListener(expandListener);
    }

    private void prepareHeights() {
        //TODO get image from cache and remove gallery container
        toolbarCollapseHeight = appBarLayout.getHeight() - toolbar.getHeight();
        listCollapseHeight = (getHeight() - appBarLayout.getHeight()) - collapsedListHeight;
        galleryContainerWidth = galleryContainerView.getWidth();
        galleryContainerHeight = galleryContainerView.getHeight();
    }

    private void prepareViews() {
        if(collapseView == null) {
            collapseView = new View(getContext());

            CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) collapsedListHeight);
            layoutParams.setAnchorId(scrollView.getId());
            layoutParams.anchorGravity = Gravity.BOTTOM;

            addView(collapseView, layoutParams);

            collapseView.setVisibility(GONE);
            collapseView.setOnClickListener(collapseListener);
        }

        if(blurViews.size() == 0 && blurEnable) {
            for(int i = 0; i < blurSteps; i++) {
                blurViews.add(new ImageView(getContext()));

                CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                addView(blurViews.get(i), indexOfChild(galleryContainerView) + 1, layoutParams);

                blurViews.get(i).setVisibility(VISIBLE);
                blurViews.get(i).setScaleType(scaleType);
            }
        }
    }

    public void prepareBlur(OnBlurCompletedListener completedListener) {
        prepareBlur(null, completedListener);
    }

    public void prepareBlur(Bitmap bmpToSet, OnBlurCompletedListener completedListener) {
        if(!blurEnable) {
            if(completedListener != null) {
                completedListener.blurCompleted();
            }

            return;
        }

        task.cancel(true);

        if(bmpToSet != null) {
            task = new BitmapTask(true, completedListener);
            task.execute(bmpToSet);
        } else {
            task = new BitmapTask(false, completedListener);
            task.execute();
        }
    }

    private BitmapTask task = new BitmapTask(false, null);

    private class BitmapTask extends AsyncTask<Bitmap, Void, Void> {
        private boolean isFromBitmap = false;
        private Bitmap drawnView;
        private OnBlurCompletedListener completedListener;

        public BitmapTask(boolean isFromBitmap, OnBlurCompletedListener completedListener) {
            this.isFromBitmap = isFromBitmap;
            this.completedListener = completedListener;
        }

        @Override
        protected void onPreExecute() {
            for(ImageView iv : blurViews) {
                iv.setImageBitmap(null);
            }
            for(Bitmap b : blurBitmaps) {
                if(b != null) {
                    b.recycle();
                }
            }
            blurBitmaps.clear();

            if(!isFromBitmap) {
                drawnView = Bitmap.createBitmap((int) galleryContainerWidth, (int) galleryContainerHeight, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(drawnView);

                galleryContainerView.draw(c);
            }
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            for(int i = 0; i < blurSteps; i++) {
                Bitmap bmp;

                if(isFromBitmap) {
                    bmp = params[0].copy(params[0].getConfig(), true);
                } else {
                    bmp = drawnView.copy(drawnView.getConfig(), true);
                }

                Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, bmp.getWidth()/bitmapSizeDivide, bmp.getHeight()/bitmapSizeDivide, true);
                if(bmpScaled != bmp) {
                    bmp.recycle();
                    bmp = null;
                }

                Utils.blurImage(renderScript, bmpScaled, maxBlurRadius - i * (maxBlurRadius / blurSteps));

                blurBitmaps.add(bmpScaled);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (int i = 0; i < blurViews.size(); i++) {
                blurViews.get(i).setImageBitmap(blurBitmaps.get(i));

                if(scaleEnable) {
                    blurViews.get(i).setPivotX(blurViews.get(i).getWidth() / 2);
                    blurViews.get(i).setPivotY(blurViews.get(i).getHeight() / 2);
                    blurViews.get(i).setScaleX(maxScale - ((maxScale - 1.0f) / blurSteps) * i);
                    blurViews.get(i).setScaleY(maxScale - ((maxScale - 1.0f) / blurSteps) * i);
                }
            }

            if(drawnView != null) {
                drawnView.recycle();
                drawnView = null;
            }

            if(completedListener != null) {
                completedListener.blurCompleted();
            }

            super.onPostExecute(aVoid);
        }
    }

    private View.OnClickListener expandListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentOffset == 0) {
                setUpDurationAndInterpolator();
                isExpandedViaClick = true;
                listener.stateChanged(true);
                appBarLayout.setExpanded(false, true);
                collapseView.setVisibility(VISIBLE);
                scrollView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        galleryView.onTouchEvent(motionEvent);

                        return true;
                    }
                });
            }
        }
    };

    private View.OnClickListener collapseListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setUpDurationAndInterpolator();
            isCollapsedViaClick = true;
            listener.stateChanged(false);
            appBarLayout.setExpanded(true, true);
            scrollView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    galleryView.onTouchEvent(motionEvent);

                    return true;
                }
            });
            collapseView.setVisibility(GONE);
            BlurZoomCoordinatorLayout.this.setOnTouchListener(null);
        }
    };

    private AppBarLayout.OnOffsetChangedListener offsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
            currentOffset = offset;

            if (isExpandedViaClick) {
                float fraction = (-offset / toolbarCollapseHeight);
                ViewGroup.LayoutParams layoutParams = placeholder.getLayoutParams();
                layoutParams.height = (int) ((toolbarCollapseHeight + listCollapseHeight) * fraction);
                placeholder.setLayoutParams(layoutParams);

                if(blurEnable) {
                    float value = ((float) blurSteps) - (fraction * blurSteps);
                    for (int i = 0; i < blurSteps; i++) {
                        float alpha = value - blurSteps + 1f + i;
                        alpha = alpha < 0f ? 0f : alpha;
                        alpha = alpha > 1f ? 1f : alpha;
                        blurViews.get(i).setAlpha(alpha);
                    }
                }

                if (-offset == toolbarCollapseHeight) {
                    isExpandedViaClick = false;
                    collapseView.setVisibility(VISIBLE);
                }
            } else if (isCollapsedViaClick) {
                float fraction = (-offset / toolbarCollapseHeight);
                ViewGroup.LayoutParams layoutParams = placeholder.getLayoutParams();
                layoutParams.height = (int) ((toolbarCollapseHeight + listCollapseHeight) * fraction);
                placeholder.setLayoutParams(layoutParams);

                if(blurEnable) {
                    float value = ((float) blurSteps) - (fraction * blurSteps);
                    for (int i = 0; i < blurSteps; i++) {
                        float alpha = value - blurSteps + 1f + i;
                        alpha = alpha < 0f ? 0f : alpha;
                        alpha = alpha > 1f ? 1f : alpha;
                        blurViews.get(i).setAlpha(alpha);
                    }
                }

                if (offset == 0) {
                    scrollView.setOnTouchListener(null);
                    isCollapsedViaClick = false;
                }
            }
        }

    };

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private void setUpDurationAndInterpolator() {
        try {
            if(duration == -1 && interpolator == null) {
                return;
            }

            CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();

            Class appBarLayoutBehaviorClass = Class.forName("android.support.design.widget.AppBarLayout$Behavior");
            Field animatorField = appBarLayoutBehaviorClass.getDeclaredField("mAnimator");
            animatorField.setAccessible(true);

            Object oldAnimator = animatorField.get(behavior);

            Class valueAnimatorCompatClass = Class.forName("android.support.design.widget.ValueAnimatorCompat");
            if(duration != -1) {
                Method setDurationMethod = valueAnimatorCompatClass.getMethod("setDuration", int.class);
                setDurationMethod.invoke(oldAnimator, duration);
            }
            if(interpolator != null) {
                Method setInterpolatorMethod = valueAnimatorCompatClass.getMethod("setInterpolator", Interpolator.class);
                setInterpolatorMethod.invoke(oldAnimator, interpolator);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        this.listener = listener;
    }

    public void expand() {
        expandListener.onClick(null);
    }

    public void collapse() {
        collapseListener.onClick(null);
    }

    public interface OnStateChangedListener {
        void stateChanged(boolean isExpanded);
    }

    public interface OnBlurCompletedListener {
        void blurCompleted();
    }
}
