
package main.java.com.moneydesktop.finance.tablet.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.shared.activity.IntroBaseActivity;
import main.java.com.moneydesktop.finance.tablet.fragment.IntroTabletFragment;
import main.java.com.moneydesktop.finance.util.Fonts;

public class IntroTabletActivity extends IntroBaseActivity {

    @Override
    protected void applyFonts() {
        Fonts.applyPrimaryFont(mLoadingMessage, 24);
        Fonts.applyPrimarySemiBoldFont(mStartButton, 18);
    }

    @Override
    protected FragmentPagerAdapter getAdapter() {
        return new MyAdapter(getSupportFragmentManager());
    }

    @Override
    protected Intent getDashboardIntent() {
        return new Intent(this, DashboardTabletActivity.class);
    }

    @Override
    protected int getContentResource() {
        return R.layout.tablet_intro_view;
    }

    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroTabletFragment(R.drawable.tablet_tips1);
                case 1:
                    return new IntroTabletFragment(R.drawable.tablet_tips2);
                case 2:
                    return new IntroTabletFragment(R.drawable.tablet_tips3);
                case 3:
                    return new IntroTabletFragment(R.drawable.tablet_tips4);
                default:
                    return null;
            }
        }
    }
}
