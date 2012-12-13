package com.moneydesktop.finance.tablet.activity;

import android.os.Bundle;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.LoginBaseActivity;

public class LoginTabletActivity extends LoginBaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_login_view);

        setupAnimations();
        setupView();
    }
}
