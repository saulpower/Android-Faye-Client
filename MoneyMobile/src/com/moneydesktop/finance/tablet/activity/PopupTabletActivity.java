package com.moneydesktop.finance.tablet.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.handset.fragment.LockFragment;
import com.moneydesktop.finance.handset.fragment.TransactionsFragment;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.util.Fonts;

public class PopupTabletActivity extends DashboardBaseActivity {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private TextView mLabel, mArrow;
    private LinearLayout mContainer;
    
    @Override
    public void onFragmentAttached(BaseFragment fragment) {
        super.onFragmentAttached(fragment);
        
        mLabel.setText(fragment.getFragmentTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tablet_popup_view);

        int index = getIntent().getIntExtra("fragment", 0);
        showFragment(index);
        
        setupView();
    }
    
    private void setupView() {

        mArrow = (TextView) findViewById(R.id.arrow);
        mLabel = (TextView) findViewById(R.id.label);
        mContainer = (LinearLayout) findViewById(R.id.container);
        
        Fonts.applyPrimaryBoldFont(mLabel, 18);
        Fonts.applyGlyphFont(mArrow, 18);
        
        mContainer.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mContainer.getWindowToken(), 0);
                
                finish();
            }
        });
    }
    
    @Override
    public String getActivityTitle() {
        return null;
    }

    @Override
    public void showFragment(int index) {

        BaseFragment fragment = getFragment(index);
        
        FragmentTransaction ft = mFm.beginTransaction();
        ft.replace(R.id.fragment, fragment);
        ft.commit();
    }

    private BaseFragment getFragment(int index) {

        switch (index) {
        case 0:
            return LockFragment.newInstance(true);
        case 1:
          // return TransactionsFragment.newInstance(getIntent().getExtras().get);
        }
        
        return null;
    }

}
