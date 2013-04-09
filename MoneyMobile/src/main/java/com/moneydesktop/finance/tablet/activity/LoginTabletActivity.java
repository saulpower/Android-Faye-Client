package main.java.com.moneydesktop.finance.tablet.activity;

import android.content.Intent;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.shared.activity.LoginBaseActivity;

public class LoginTabletActivity extends LoginBaseActivity {

    @Override
    protected int getContentResource() {
        return R.layout.tablet_login_view;
    }

    @Override
    protected Intent getDashboardIntent() {
        return new Intent(this, DashboardTabletActivity.class);
    }

    @Override
    protected Intent getLoginIntent() {
        return  new Intent(this, IntroTabletActivity.class);
    }
}