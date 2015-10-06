package com.github.fafaldo.blurzoomgallery.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.github.fafaldo.blurzoomgallery.widget.BlurZoomCoordinatorLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BlurZoomCoordinatorLayout coordinatorLayout;

    private ViewPager gallery;
    private GalleryPagerAdapter adapter;

    public boolean isScrolling = false;
    public int currentPage = 0;
    public boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        toolbar = (Toolbar) findViewById(R.id.gallery_coordinator_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.gallery_coordinator_appbarlayout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        gallery = (ViewPager) findViewById(R.id.gallery_coordinator_gallery);
        coordinatorLayout = (BlurZoomCoordinatorLayout) findViewById(R.id.coordinator);

        adapter = new GalleryPagerAdapter(getSupportFragmentManager());

        gallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    isScrolling = false;
                    currentPage = gallery.getCurrentItem();

                    coordinatorLayout.prepareBlur(null);
                } else {
                    isScrolling = true;
                }
            }
        });

        gallery.setOffscreenPageLimit(10);
        gallery.setAdapter(adapter);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle("Paris, France");

        coordinatorLayout.setOnStateChangedListener(new BlurZoomCoordinatorLayout.OnStateChangedListener() {
            @Override
            public void stateChanged(boolean state) {
                isExpanded = state;
            }
        });

        coordinatorLayout.setDuration(400);
        coordinatorLayout.setInterpolator(new AccelerateDecelerateInterpolator());

        appBarLayout.setExpanded(true, true);
    }

    public void blur(Bitmap bmp, BlurZoomCoordinatorLayout.OnBlurCompletedListener listener) {
        coordinatorLayout.prepareBlur(bmp, listener);
    }
}
