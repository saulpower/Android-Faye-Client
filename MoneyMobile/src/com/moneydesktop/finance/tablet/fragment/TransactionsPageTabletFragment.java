package com.moneydesktop.finance.tablet.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;
import com.moneydesktop.finance.views.HeaderView;
import com.moneydesktop.finance.views.HorizontalScroller;

import de.greenrobot.event.EventBus;

import java.util.List;

@TargetApi(11)
public class TransactionsPageTabletFragment extends BaseFragment implements OnItemClickListener, FilterChangeListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private AmazingListView mTransactionsList;
    private DateRangeView mDateRange;
    private HorizontalScroller mScroller;
    private TransactionsTabletFragment mParent;
    private int[] mLocation = new int[2];
    private String mOrderBy = Constant.FIELD_DATE, mDirection = Constant.ORDER_DESC;
    private String mSearchTitle = "%";
    
    private HeaderView mDate, mPayee, mCategory, mAmount;
    private EditText mSearch;
    
    private boolean mLoaded = false;
    private boolean mWaiting = true;
    
    private TextWatcher mWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            mSearchTitle = "%" + mSearch.getText().toString().toLowerCase() + "%";
            getInitialTransactions();
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
        
        getInitialTransactions();
        
        return mRoot;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
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
    
    private void setupListeners() {
        
        mDate.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mDate, Constant.FIELD_DATE);
            }
        });
        
        mPayee.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mPayee, Constant.FIELD_TITLE);
            }
        });
        
        mCategory.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mCategory, Constant.FIELD_CATEGORY);
            }
        });
        
        mAmount.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mAmount, Constant.FIELD_AMOUNT);
            }
        });
        
        mDate.performClick();
    }
    
    private void applyFonts() {
        
        Fonts.applyPrimaryBoldFont(mDate, 14);
        Fonts.applyPrimaryBoldFont(mPayee, 14);
        Fonts.applyPrimaryBoldFont(mCategory, 14);
        Fonts.applyPrimaryBoldFont(mAmount, 14);
        Fonts.applyPrimaryBoldFont(mSearch, 18);
    }
    
    private void applySort(HeaderView clicked, String field) {
        
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
        
        mOrderBy = field;
        mDirection = clicked.isAscending() ? Constant.ORDER_ASC : Constant.ORDER_DESC;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);
        
        if (transaction != null) {

            mRoot.getLocationOnScreen(mLocation);
            mParent.showTransactionDetails(view, mLocation[1], transaction);
        }
    }

    private void configureView() {
        
        if (mLoaded && !mWaiting && mTransactionsList.getVisibility() != View.VISIBLE) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                
                @Override
                public void run() {

                    mTransactionsList.setVisibility(View.VISIBLE);
                    mTransactionsList.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_fast));
                }
            }, 100);
        
        }
    }
    
    private void getInitialTransactions() {

        new AsyncTask<Integer, Void, TransactionsTabletAdapter>() {
            
            @Override
            protected TransactionsTabletAdapter doInBackground(Integer... params) {
                
                int page = params[0];
                
                List<Transactions> row1 = Transactions.getRows(page, mSearchTitle, mDateRange.getStartDate(), mDateRange.getEndDate(), mOrderBy, mDirection).second;

                TransactionsTabletAdapter adapter = new TransactionsTabletAdapter(mActivity, mTransactionsList, row1);
                adapter.setDateRange(mDateRange.getStartDate(), mDateRange.getEndDate());
                adapter.setOrder(mOrderBy, mDirection);
                adapter.setSearch(mSearchTitle);
                
                return adapter;
            }
            
            @Override
            protected void onPostExecute(TransactionsTabletAdapter adapter) {

                mLoaded = true;
                setupList(adapter);
            };
            
        }.execute(1);
    }
    
    private void setupList(TransactionsTabletAdapter adapter) {

        mTransactionsList.setAdapter(adapter);
        
        adapter.notifyDataSetChanged();
        
        if (adapter.getCount() < Constant.QUERY_LIMIT) {
            adapter.notifyNoMorePages();
        } else {
            adapter.notifyMayHaveMorePages();
        }
        
        if (!mWaiting) {
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
    
    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void filterChanged() {
        getInitialTransactions();
    }

}
