package com.moneydesktop.finance.tablet.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage.FilterEvent;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;
import com.moneydesktop.finance.views.HeaderView;
import com.moneydesktop.finance.views.HorizontalScroller;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class TransactionsPageTabletFragment extends BaseFragment implements OnItemClickListener, FilterChangeListener, OnDataLoadedListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private AmazingListView mTransactionsList;
    private TransactionsTabletAdapter mAdapter;
    private DateRangeView mDateRange;
    private HorizontalScroller mScroller;
    private TransactionsTabletFragment mParent;
    private int[] mLocation = new int[2];
    
    private QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
    private boolean mDirection = true;
    private String mSearchTitle = "%";
    private PowerQuery mQueries;
    
    private HeaderView mDate, mPayee, mCategory, mAmount;
    private EditText mSearch;
    
    private boolean mLoaded = false;
    private boolean mWaiting = true;
    
    private Animation mFadeIn, mFadeOut;
    
    private AsyncTask<Integer, Void, TransactionsTabletAdapter> mBackgroundTask;
    
    private TextWatcher mWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            mSearchTitle = "%" + mSearch.getText().toString().toLowerCase() + "%";
            setupTransactionsList();
        }
    };
    
    public static TransactionsPageTabletFragment newInstance() {
            
        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_transaction_page_view, null);

        mParent = ((TransactionsTabletFragment) getParentFragment());
        
        setupView();
        setupAnimations();
        
        setupTransactionsList();
        
        return mRoot;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
        
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(false);
        }
    }
    
    private void setupView() {

        mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
        mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
        mTransactionsList.setEmptyView(mActivity.getLayoutInflater().inflate(R.layout.empty_view, null));
        mTransactionsList.setOnItemClickListener(this);
        
        mScroller = (HorizontalScroller) mRoot.findViewById(R.id.date_scroller);
        
        mDateRange = (DateRangeView) mRoot.findViewById(R.id.date_range);
        mDateRange.setScroller(mScroller);
        mDateRange.setDateRangeChangeListener(this);
        
        mDate = (HeaderView) mRoot.findViewById(R.id.date_header);
        mDate.setOnFilterChangeListener(this);
        mPayee = (HeaderView) mRoot.findViewById(R.id.payee_header);
        mPayee.setOnFilterChangeListener(this);
        mCategory = (HeaderView) mRoot.findViewById(R.id.category_header);
        mCategory.setOnFilterChangeListener(this);
        mAmount = (HeaderView) mRoot.findViewById(R.id.amount_header);
        mAmount.setOnFilterChangeListener(this);
        
        mSearch = (EditText) mRoot.findViewById(R.id.search);
        mSearch.addTextChangedListener(mWatcher);
        
        setupListeners();
        applyFonts();
    }
    
    private void setupAnimations() {
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_now);
        mFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_now);
        mFadeOut.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
                mTransactionsList.setVisibility(View.INVISIBLE);
                mAdapter.notifyDataSetInvalidated();
                mTransactionsList.setVisibility(View.VISIBLE);
                mTransactionsList.startAnimation(mFadeIn);
            }
        });
    }
    
    private void setupListeners() {
        
        mDate.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mDate, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date));
            }
        });
        
        mPayee.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mPayee, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title));
            }
        });
        
        mCategory.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mCategory, new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.CategoryName));
            }
        });
        
        mAmount.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mAmount, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Amount));
            }
        });
        

        mSearch.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                     hideKeyboard();
                }
                
                return false;
            }
        });
        
        mDate.performClick();
    }

    private void hideKeyboard() {

        InputMethodManager in = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mSearch.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    private void applyFonts() {
        
        Fonts.applyPrimaryBoldFont(mDate, 12);
        Fonts.applyPrimaryBoldFont(mPayee, 12);
        Fonts.applyPrimaryBoldFont(mCategory, 12);
        Fonts.applyPrimaryBoldFont(mAmount, 12);
        Fonts.applyPrimaryBoldFont(mSearch, 14);
    }
    
    private void applySort(HeaderView clicked, QueryProperty field) {
        
        mOrderBy = field;
        
        final boolean wasShowing = clicked.isShowing();
        
        mDate.setIsShowing(false);
        mPayee.setIsShowing(false);
        mCategory.setIsShowing(false);
        mAmount.setIsShowing(false);
        
        clicked.setIsShowing(true);
        
        if (wasShowing) {
            clicked.setIsAscending(!clicked.isAscending(), true);
        } else {
            clicked.setIsAscending(false, false);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);
        
        if (transaction != null) {

            mRoot.getLocationOnScreen(mLocation);
            mParent.showTransactionDetails(view, mLocation[1], transaction);
        }
    }
    
    private void setupTransactionsList() {
        
        mLoaded = false;
        
        if (mAdapter == null) {
            mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList);
            mAdapter.setOnDataLoadedListener(this);
            mTransactionsList.setAdapter(mAdapter);
        }
        
        mAdapter.setDateRange(mDateRange.getStartDate(), mDateRange.getEndDate());
        mAdapter.setOrder(mOrderBy, mDirection);
        mAdapter.setSearch(mSearchTitle);
        mAdapter.setQueries(mQueries);
        mAdapter.initializeData();
    }
    
    @Override
    public void dataLoaded(boolean isRequest) {
        
        mLoaded = true;

        if (mTransactionsList.getVisibility() == View.VISIBLE && !isRequest) {
            mTransactionsList.startAnimation(mFadeOut);
        } else {
            configureView();
        }
    }
    
    public void onEvent(ParentAnimationEvent event) {
        
        if (!event.isOutAnimation() && !event.isFinished()) {
            mWaiting = true;
        }
        
        if (event.isOutAnimation() && event.isFinished()) {

            mWaiting = false;
            configureView();
        }
    }
    
    public void onEvent(FilterEvent event) {

        mQueries = event.getQueries();
        filterChanged(-1);
    }

    private void configureView() {
        
        if (mLoaded && !mWaiting && mTransactionsList.getVisibility() != View.VISIBLE) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                
                @Override
                public void run() {

                    mTransactionsList.setVisibility(View.VISIBLE);
                    mTransactionsList.startAnimation(mFadeIn);
                }
            }, 100);
        }
    }
    
    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void filterChanged(int direction) {

        if (direction != -1) {
            mDirection = direction == 0;
        }
        
        setupTransactionsList();
    }
}
