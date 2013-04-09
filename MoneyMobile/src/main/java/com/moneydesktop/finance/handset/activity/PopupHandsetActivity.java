package main.java.com.moneydesktop.finance.handset.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.data.Enums.LockType;
import main.java.com.moneydesktop.finance.shared.activity.DashboardBaseActivity;
import main.java.com.moneydesktop.finance.shared.fragment.BaseFragment;
import main.java.com.moneydesktop.finance.shared.fragment.FeedbackFragment;
import main.java.com.moneydesktop.finance.shared.fragment.LockCodeFragment;
import main.java.com.moneydesktop.finance.util.UiUtils;

public class PopupHandsetActivity extends DashboardBaseActivity {

    private BaseFragment mFragment;
    private FrameLayout mLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.handset_popup_activity);

        mLayout = (FrameLayout) findViewById(R.id.fragment);

        FragmentType fragType = (FragmentType) getIntent().getSerializableExtra(Constant.EXTRA_FRAGMENT);

        switch(fragType) {
            case LOCK_SCREEN:
                LockType lockType = (LockType) getIntent().getSerializableExtra(LockCodeFragment.EXTRA_LOCK);
                mFragment = LockCodeFragment.newInstance(lockType);
                break;
            case FEEDBACK:
                mFragment = FeedbackFragment.newInstance();
                break;
            default:
                dismissModal();
                break;
        }

        FragmentTransaction ft = mFragmentManager.beginTransaction();
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
            dismissModal();
        }
    }

    public void dismissModal() {

        UiUtils.hideKeyboard(this, mLayout);
        finish();
        overridePendingTransition(R.anim.none, R.anim.out_down);
    }

    @Override
    public String getActivityTitle() {
        return null;
    }

    @Override
    public void showFragment(FragmentType type, boolean moveUp) {
    }

}
