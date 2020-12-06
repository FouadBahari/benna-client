package com.fouadbahari.lellafood.SplashScreen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    public ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                BoardingFragment1 tab1 = new BoardingFragment1();
                return tab1;
            case 1:
                BoardingFragment2 tab2 = new BoardingFragment2();
                return tab2;
            case 2:
                BoardingFragment3 tab3 = new BoardingFragment3();
                return tab3;
        }
        return null;

    }

    @Override
    public int getCount() {
        return 3;
    }
}
