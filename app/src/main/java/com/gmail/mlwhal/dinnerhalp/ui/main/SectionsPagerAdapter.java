package com.gmail.mlwhal.dinnerhalp.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.gmail.mlwhal.dinnerhalp.MainActivity;
import com.gmail.mlwhal.dinnerhalp.R;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return either SearchFragment or ManageFragment (defined as a static inner class below).
        if (position == 0) {
            return MainActivity.SearchFragment.newInstance(position + 1);
        } else {
            return MainActivity.ManageFragment.newInstance(position + 1);
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return mContext.getResources().getString(R.string.title_section2).toUpperCase(l);
        }
        return null;
    }
}