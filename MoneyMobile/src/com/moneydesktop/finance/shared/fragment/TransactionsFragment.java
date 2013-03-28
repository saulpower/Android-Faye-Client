package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.handset.adapter.TransactionsHandsetAdapter;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.FilterEvent;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.tablet.fragment.TransactionsPageTabletFragment;
import com.moneydesktop.finance.util.*;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class TransactionsFragment extends BaseFragment implements FilterChangeListener, OnDataLoadedListener {
    
    public final String TAG = this.getClass().getSimpleName();

    protected AmazingListView mTransactionsList;
    protected TransactionsAdapter mAdapter;

    protected String mAccountId;
    protected ArrayList<Long> mCategories;
    protected int mCategoryType;
    protected TxFilter mTxFilter;
    protected QueryProperty mAccountIdProp = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.BankAccountId);
    protected QueryProperty mAccountIdForBankAccountProp = new QueryProperty(BankAccountDao.TABLENAME, BankAccountDao.Properties.Id);
    protected QueryProperty mIsProcessed = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.IsProcessed);
    protected QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
    protected boolean mDirection = true;
    protected String mSearchTitle = "%";
    protected ArrayList<PowerQuery> mQueries = new ArrayList<PowerQuery>();
    protected boolean mIsSearchEnabled = true;
    protected String mBankAccountID;
    
    protected EditText mSearch;
    
    protected boolean mLoaded = false;
    protected boolean mWaiting = true;

    protected Handler mHandler;
    
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
    
    public void setCategories(ArrayList<Long> mCategories) {
        this.mCategories = mCategories;
    }
    
    public void setCategoryType(int mCategoryType) {
        this.mCategoryType = mCategoryType;
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

        mHandler = new Handler();

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

        if (mIsSearchEnabled) {
            mSearch = (EditText) mRoot.findViewById(R.id.search);

            if (mSearch == null) {
                mSearch = getSearchBar();
            }

            mSearch.addTextChangedListener(mWatcher);
        
            setupListeners();
            applyFonts();
        }

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

        addAdditionalFilters();
        
        mAdapter.setDateRange(getStartDate(), getEndDate());
        mAdapter.setOrder(mOrderBy, mDirection);
        if (mIsSearchEnabled) {
            mAdapter.setSearch(mSearchTitle);
        }
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

    	mQueries.clear();
        mQueries.add(event.getQuery());
        filterChanged(-1);
    }
    
    public void onEvent(DatabaseSaveEvent event) {

    	if (mAdapter != null && event.getChangedClassesList().contains(Transactions.class)) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mAdapter.refreshCurrentSelection();
                }
            });
    	}
    }
    
    public void onEvent(SyncEvent event) {
        
    	if (mAdapter != null && event.isFinished()) {
            mAdapter.refreshCurrentSelection();
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
    private void addAdditionalFilters() {
        
        if (mAccountId == null && mCategories == null && mTxFilter != null) {
            return;
        }

        if (mBankAccountID != null && mQueries.size() == 0) {
            mQueries.add(new PowerQuery(false));
        } else {
            if (mQueries.size() == 0 && (mAccountId != null || (mTxFilter != null && mTxFilter == TxFilter.UNCLEARED))) {
                mQueries.add(new PowerQuery(false));
            }
        }


        if (mBankAccountID != null) {
            mQueries.get(0).and().where(mAccountIdForBankAccountProp, mBankAccountID);
        } else {
            if (mAccountId != null) {
                mQueries.get(0).and().where(mAccountIdProp, mAccountId);
            }
        }
        
        if (mTxFilter != null && mTxFilter == TxFilter.UNCLEARED) {
            mQueries.get(0).and().where(mIsProcessed, "0");
        }
        
        if (mCategories != null) {
        	
        	PowerQuery query = new PowerQuery(true);
        	
        	for (Long cat : mCategories) {
        		
        		String categoryId = Long.toString(cat);
        		
        		switch (mCategoryType) {
        		
	        		case Constant.CATEGORY_TYPE_GROUP:
	            	    QueryProperty parentCategoryIdProp = new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.ParentCategoryId);
	    	            query.or().where(parentCategoryIdProp, categoryId);
	    	            
	        		case Constant.CATEGORY_TYPE_CHILD:
	            	    QueryProperty categoryIdProp = new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.Id);
	    	            query.or().where(categoryIdProp, categoryId);
	    	            break;
        		}
        	}
        	
        	mQueries.add(query);
        }
    }

    protected void emailTransactions(View headers) {

        DialogUtils.showProgress(getActivity(), getString(R.string.generate_email));

        new AsyncTask<View, Void, String>() {

            @Override
            protected String doInBackground(View... params) {

                View headers = params[0];

                String path = "";
                Bitmap image = getWholeListViewItemsToBitmap(headers);

                if (image != null) {
                    path = FileIO.saveBitmap(getActivity(), image, getString(R.string.transactions_list));
                }

                return path;
            }

            @Override
            protected void onPostExecute(String path) {

                DialogUtils.hideProgress();

                if (path != null && !path.equals("")) {
                    EmailUtils.sendEmail(getActivity(), getString(R.string.email_transactions_subject), "", path);
                }
            }

        }.execute(headers);
    }

    private  Bitmap getWholeListViewItemsToBitmap(View headers) {

        synchronized (mTransactionsList) {

            Bitmap listBitmap;

            try {

                final float divider = UiUtils.getDynamicPixels(getActivity(), 1);
                int allItemsHeight = headers == null ? 0 : headers.getHeight();

                List<Bitmap> bitmaps = new ArrayList<Bitmap>();

                if (headers != null) {
                    Bitmap header = UiUtils.convertViewToBitmap(headers);
                    bitmaps.add(header);
                }

                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                for (int i = 0; i < mAdapter.getCount(); i++) {

                    View childView = mAdapter.getView(i, null, mTransactionsList);

                    if (childView.getLayoutParams() == null) {
                        childView.setLayoutParams(params);
                    }

                    int width = View.MeasureSpec.makeMeasureSpec(mTransactionsList.getWidth(),
                            View.MeasureSpec.EXACTLY);
                    int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

                    childView.measure(width, height);

                    childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
                    childView.setDrawingCacheEnabled(true);
                    childView.buildDrawingCache();

                    bitmaps.add(childView.getDrawingCache());
                    allItemsHeight += childView.getMeasuredHeight();
                }

                listBitmap = Bitmap.createBitmap(mTransactionsList.getMeasuredWidth(), (int) (allItemsHeight + mAdapter.getCount() * divider), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(listBitmap);
                canvas.drawColor(getResources().getColor(R.color.gray1));

                final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                int currentTop = 0;

                for (int i = 0; i < bitmaps.size(); i++) {

                    Bitmap bmp = bitmaps.get(i);
                    canvas.drawBitmap(bmp, 0, currentTop, paint);
                    currentTop += (bmp.getHeight() + divider);

                    bmp.recycle();
                    bmp = null;
                }

            } catch (Exception ex) {
                Log.e(TAG, "Error creating transactions email", ex);
                return null;
            }

            return listBitmap;
        }
    }
}
