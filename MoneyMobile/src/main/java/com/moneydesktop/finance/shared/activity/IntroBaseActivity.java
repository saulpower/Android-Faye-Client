package main.java.com.moneydesktop.finance.shared.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import main.java.com.moneydesktop.finance.data.Preferences;
import main.java.com.moneydesktop.finance.data.Util;
import main.java.com.moneydesktop.finance.database.BankAccount;
import main.java.com.moneydesktop.finance.database.BankAccountDao;
import main.java.com.moneydesktop.finance.model.EventMessage.SyncEvent;
import main.java.com.moneydesktop.finance.views.FixedSpeedScroller;
import main.java.com.moneydesktop.finance.views.SmallSpinnerView;
import com.viewpagerindicator.CirclePageIndicator;
import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class IntroBaseActivity extends BaseActivity {

    private static final long WAIT_DURATION = 4000;

    private FragmentPagerAdapter mAdapter;
    private ViewPager mPager;

    protected TextView mLoadingMessage;
    protected TextView mStartButton;
    private SmallSpinnerView mSpinner;
    private CirclePageIndicator mTitleIndicator;

    private int mCurrentFragment = 0;
    private final Handler mHandler = new Handler();

    private Runnable mTask;
    private boolean mToDashboard = false;

    private AnimationListener mOut = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mLoadingMessage.setVisibility(View.GONE);
            mSpinner.setVisibility(View.GONE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentResource());

        setupView();
        mStartButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                toDashboard();
            }
        });

        mAdapter = getAdapter();
        mPager.setAdapter(mAdapter);

        mTitleIndicator.setViewPager(mPager);
        mLoadingMessage.setText(getResources().getText(R.string.loading_app));

        // Hack fix to adjust scroller velocity on view pager
        try {
            Field mScroller;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(mPager.getContext(), null);
            mScroller.set(mPager, scroller);

        } catch (Exception e) {}

        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                stopFlipping();
                return false;
            }
        });
    }

    private void setupView() {

        mPager = (ViewPager) findViewById(R.id.pager);
        mTitleIndicator = (CirclePageIndicator) findViewById(R.id.titles);
        mLoadingMessage = (TextView) findViewById(R.id.loading_text);
        mStartButton = (TextView) findViewById(R.id.get_started);
        mSpinner = (SmallSpinnerView) findViewById(R.id.loading_spinner);

        applyFonts();
    }

    protected abstract void applyFonts();

    protected abstract FragmentPagerAdapter getAdapter();

    protected abstract Intent getDashboardIntent();

    protected abstract int getContentResource();

    @Override
    public void onResume() {
        super.onResume();

        beginAutoSlide();
    }

    private void beginAutoSlide() {

        if (mTask == null) {

            mTask = new Runnable() {

                @Override
                public void run() {

                    if (mCurrentFragment <= mPager.getChildCount()) {
                        mPager.setCurrentItem(mCurrentFragment++);
                    } else {
                        mCurrentFragment = 0;
                        mPager.setCurrentItem(mCurrentFragment++);
                    }

                    mHandler.postDelayed(this, WAIT_DURATION);
                }
            };
        }

        mHandler.postDelayed(mTask, WAIT_DURATION);
    }

    public void stopFlipping() {
        mHandler.removeCallbacks(mTask);
    }

    public void onEvent(SyncEvent event) {

        if (event.isFinished()) {

            configureReadyState();
        }
    }

    private void configureReadyState() {

        mLoadingMessage.setVisibility(View.GONE);
        mSpinner.setVisibility(View.GONE);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
        fadeOut.setAnimationListener(mOut);

        mLoadingMessage.startAnimation(fadeOut);
        mSpinner.startAnimation(fadeOut);

        mStartButton.setVisibility(View.VISIBLE);
        mStartButton.startAnimation(fadeIn);
    }

    private void toDashboard() {

        if (mToDashboard) return;

        mToDashboard = true;

        saveBankExclusions();

        Intent i = getDashboardIntent();
        startActivity(i);
        overridePendingTransition(R.anim.fade_in_fast, R.anim.none);
        finish();
    }

    private void saveBankExclusions() {

        ApplicationContext.getDaoSession().clear();
        BankAccountDao dao = ApplicationContext.getDaoSession().getBankAccountDao();
        List<BankAccount> bankAccountList = dao.loadAll();

//        ArrayList<String> transactionsList = new ArrayList<String>();
//        ArrayList<String> reports = new ArrayList<String> ();
//        ArrayList<String> accountList = new ArrayList<String> ();
//        ArrayList<String> budgets = new ArrayList<String> ();
//        ArrayList<String> transfersFromIncome = new ArrayList<String> ();
//        ArrayList<String> transfersFromExpenses = new ArrayList<String> ();
//        ArrayList<String> all = new ArrayList<String> ();

        JSONArray transactionsList = new JSONArray();
        JSONArray reports = new JSONArray();
        JSONArray accountList = new JSONArray();
        JSONArray budgets = new JSONArray();
        JSONArray transfersFromIncome = new JSONArray();
        JSONArray transfersFromExpenses = new JSONArray();
        JSONArray all = new JSONArray();


        for (BankAccount bankAccount : bankAccountList) {

            List<AccountExclusionFlags> exclusionListForAccount = BankAccount.getExclusionsForAccount(bankAccount);
            for (AccountExclusionFlags exclusions : exclusionListForAccount) {
                if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ALL) {
                    all.put(String.valueOf(bankAccount.getId()));
                    //all.add(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES) {
                    //transfersFromExpenses.add(String.valueOf(bankAccount.getId()));
                    transfersFromExpenses.put(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME) {
                    transfersFromIncome.put(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_BUDGETS) {
                    budgets.put(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_ACCOUNT_LIST) {
                    accountList.put(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_REPORTS) {
                    reports.put(String.valueOf(bankAccount.getId()));
                } else if (exclusions == AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSACTION_LIST) {
                    transactionsList.put(String.valueOf(bankAccount.getId()));
                }
            }
        }

        Preferences.saveString(Constant.PREFS_EXCLUSIONS_ALL, all.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSFERS_FROM_EXPENSES, transfersFromExpenses.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSFERS_FROM_INCOME, transfersFromIncome.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_BUDGETS, budgets.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_ACCOUNTS_LIST, accountList.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_REPORTS, reports.toString());
        Preferences.saveString(Constant.PREFS_EXCLUSIONS_TRANSACTIONS_LIST, transactionsList.toString());
    }


    @Override
    public String getActivityTitle() {
        return null;
    }
}