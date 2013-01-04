package com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.shared.LockCodeFragment;
import com.moneydesktop.finance.util.Enums.LockType;

public class LockCodeHandsetActivity extends DashboardBaseActivity {
    
    private BaseFragment mFragment;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.handset_activity_lock_code_view);
        
        LockType lockType = (LockType) getIntent().getSerializableExtra(LockCodeFragment.EXTRA_LOCK);
        
        mFragment = LockCodeFragment.newInstance(lockType);
        
        FragmentTransaction ft = mFm.beginTransaction();
        ft.replace(R.id.fragment, mFragment);
        ft.commit();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        ApplicationContext.setLockShowing(false);
    }
    
    @Override
    public void onFragmentAttached(BaseFragment fragment) {
        super.onFragmentAttached(fragment);
        
        mFragment = fragment;
    }
    
    @Override
    public void onBackPressed() {
        
        if (!mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
    
    public void dismissModal() {
          
        finish();
        overridePendingTransition(R.anim.none, R.anim.out_down);
    }
    
    @Override
    public String getActivityTitle() {
        return null;
    }

    @Override
    public void showFragment(int index) {
    }

}
