package com.moneydesktop.finance.shared.fragment;


import java.util.Date;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.handset.adapter.TransactionsHandsetAdapter;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.FilterEvent;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.tablet.fragment.TransactionsPageTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;

import de.greenrobot.event.EventBus;

public abstract class TransactionsFragment extends BaseFragment implements FilterChangeListener, OnDataLoadedListener {
    
    public final String TAG = this.getClass().getSimpleName();

    protected AmazingListView mTransactionsList;
    protected TransactionsAdapter mAdapter;

    protected String mAccountId;
    protected TxFilter mTxFilter;
    protected QueryProperty mAccountIdProp = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.BankAccountId);
    protected QueryProperty mIsProcessed = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.IsProcessed);
    protected QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
    protected boolean mDirection = true;
    protected String mSearchTitle = "%";
    protected PowerQuery mQueries;
    
    protected EditText mSearch;
    
    protected boolean mLoaded = false;
    protected boolean mWaiting = true;
    
    protected AsyncTask<Integer, Void, TransactionsTabletAdapter> mBackgroundTask;
    
    protected TextWatcher mWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            mSearchTitle = "%" + mSearch.getText().toString().toLowerCase() + "%";
            refreshTransactionsList();
        }
    };
    
    public void setAccountId(String mAccountId) {
        this.mAccountId = mAccountId;
    }

    public void setTxFilter(TxFilter mTxFilter) {
        this.mTxFilter = mTxFilter;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.TRANSACTIONS_PAGE;
	}
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (mTransactionsList == null) {
        	setupView();
        	refreshTransactionsList();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
        
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(false);
        }
    }
    
    protected void setupView() {

        mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
        mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
        mTransactionsList.setEmptyView(mActivity.getLayoutInflater().inflate(R.layout.empty_view, null));
        
        mSearch = (EditText) mRoot.findViewById(R.id.search);
        
        if (mSearch == null) {
        	mSearch = getSearchBar();
        }
        
        mSearch.addTextChangedListener(mWatcher);
        
        setupListeners();
        applyFonts();
    }
    
    private void setupListeners() {
        
        mSearch.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    UiUtils.hideKeyboard(mActivity, v);
                }
                
                return false;
            }
        });
    }
    
    private void applyFonts() {
    	
    	if (getChildInstance() instanceof TransactionsPageTabletFragment) {
    		Fonts.applyPrimaryBoldFont(mSearch, 14);
    	} else {
    		Fonts.applyPrimaryFont(mSearch, 12);
    	}
    }
    
    protected void updateTransactionCategory(int position, long categoryId) {
        
        Transactions transaction = (Transactions) mTransactionsList.getItemAtPosition(position);
        transaction.setCategoryId(categoryId);
        transaction.updateSingle();
        
        mAdapter.notifyDataSetChanged();
    }
    
    protected void refreshTransactionsList() {
    	refreshTransactionsList(true);
    }
    
    protected void refreshTransactionsList(boolean invalidate) {
    	
        mLoaded = false;
        
        if (mAdapter == null) {
        	
        	if (getChildInstance() instanceof TransactionsPageTabletFragment) {
        		mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList);
        	} else {
        		mAdapter = new TransactionsHandsetAdapter(mActivity, mTransactionsList);
        	}
        	
            mAdapter.setOnDataLoadedListener(this);
            mTransactionsList.setAdapter(mAdapter);
        }

        addAccountFilter();
        
        mAdapter.setDateRange(getStartDate(), getEndDate());
        mAdapter.setOrder(mOrderBy, mDirection);
        mAdapter.setSearch(mSearchTitle);
        mAdapter.setQueries(mQueries);
        mAdapter.initializeData(invalidate);
    }
    
    protected abstract Date getStartDate();
    protected abstract Date getEndDate();
    protected abstract Object getChildInstance();
    protected EditText getSearchBar() {
    	return null;
    }
    
    public void onEvent(FilterEvent event) {

        mQueries = event.getQueries();
        filterChanged(-1);
    }
    
    public void onEvent(DatabaseSaveEvent event) {
        
    	if (mAdapter != null && event.didDatabaseChange() && event.getChangedClassesList().contains(Transactions.class)) {
    		refreshTransactionsList(false);
    	}
    }
    
    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public void filterChanged(int direction) {

        if (direction != -1) {
            mDirection = direction == 0;
        }
        
        refreshTransactionsList();
    }
    
    /**
     * Add any passed in filters when the fragment
     * was constructed.
     */
    private void addAccountFilter() {
        
        if (mAccountId == null && mTxFilter != null) {
            return;
        }
        
        if (mQueries == null) {
            mQueries = new PowerQuery(false);
        }
        
        if (mAccountId != null) {
            mQueries.and().where(mAccountIdProp, mAccountId);
        }
        
        if (mTxFilter != null && mTxFilter == TxFilter.UNCLEARED) {
            mQueries.and().where(mIsProcessed, "0");
        }
    }
}
