
package main.java.com.moneydesktop.finance.handset.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.handset.fragment.IntroHandsetFragment;
import main.java.com.moneydesktop.finance.shared.activity.IntroBaseActivity;
import main.java.com.moneydesktop.finance.util.Fonts;

public class IntroHandsetActivity extends IntroBaseActivity {

    @Override
    protected void applyFonts() {
        Fonts.applyPrimaryFont(mLoadingMessage, 20);
        Fonts.applyPrimarySemiBoldFont(mStartButton, 14);
    }

    @Override
    protected FragmentPagerAdapter getAdapter() {
        return new MyAdapter(getSupportFragmentManager());
    }

    @Override
    protected Intent getDashboardIntent() {
        return new Intent(this, DashboardHandsetActivity.class);
    }

    @Override
    protected int getContentResource() {
        return R.layout.handset_intro_view;
    }

    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new IntroHandsetFragment(R.drawable.handset_tips1);
                case 1:
                    return new IntroHandsetFragment(R.drawable.handset_tips2);
                case 2:
                    return new IntroHandsetFragment(R.drawable.handset_tips3);
                default:
                    return null;
            }
        }
    }
}
