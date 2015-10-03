package com.github.fafaldo.blurzoomgallery.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by fafik on 2015-08-23.
 */
public class GalleryPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<ImageFragment> fragments = new ArrayList<>();

    public GalleryPagerAdapter(FragmentManager fm) {
        super(fm);

        fragments.add(ImageFragment.newInstance("drawable://" + R.drawable.foto1, 0));
        fragments.add(ImageFragment.newInstance("drawable://" + R.drawable.foto2, 1));
        fragments.add(ImageFragment.newInstance("drawable://" + R.drawable.foto3, 2));
        fragments.add(ImageFragment.newInstance("drawable://" + R.drawable.foto4, 3));
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return fragments.get(0);
            case 1:
                return fragments.get(1);
            case 2:
                return fragments.get(2);
            case 3:
                return fragments.get(3);
        }

        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
