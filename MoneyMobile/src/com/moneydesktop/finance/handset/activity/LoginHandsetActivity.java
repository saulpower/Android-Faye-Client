package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.LoginBaseActivity;

public class LoginHandsetActivity extends LoginBaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.handset_login_view);

        setupAnimations();
        setupView();
    }
}
