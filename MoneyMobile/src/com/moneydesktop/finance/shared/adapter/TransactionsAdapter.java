package com.moneydesktop.finance.shared.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BusinessObjectBaseDao;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.TagInstanceDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.views.AmazingListView;

public abstract class TransactionsAdapter extends AmazingAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

    private OnDataLoadedListener mOnDataLoadedListener;

	protected List<Pair<String, List<Transactions>>> mSections = new ArrayList<Pair<String, List<Transactions>>>();
	private List<Pair<String, List<Transactions>>> mNewSections = new ArrayList<Pair<String, List<Transactions>>>();
	
    protected List<Transactions> mAllTransactions = new ArrayList<Transactions>();
    private List<Transactions> mNewTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> mBackgroundTask;
	private AmazingListView mListView;
	private boolean mHasMore = false;
	private boolean mUseSections = false;
	private boolean mInvalidate = true;
	
	private Date mStart, mEnd;
	
	private QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
	private boolean mDirection = true;
	private String mSearch = "%";
	private List<PowerQuery> mQueries;

	protected Activity mActivity;
	
    private QueryProperty mTransactionDate = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date, TransactionsDao.Properties.Id);
    private QueryProperty mTransactionTitle = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title, TransactionsDao.Properties.Id);
    private QueryProperty mTagInstance = new QueryProperty(TagInstanceDao.TABLENAME, TransactionsDao.Properties.BusinessObjectId, TagInstanceDao.Properties.BaseObjectId);
    private QueryProperty mDataState = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BusinessObjectBaseDao.Properties.DataState, QueryProperty.NOT_EQUALS);

    private QueryProperty mCategoryId = new QueryProperty(CategoryDao.TABLENAME, TransactionsDao.Properties.CategoryId, CategoryDao.Properties.Id);
    private QueryProperty mCategoryName = new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.CategoryName, CategoryDao.Properties.Id);
    private QueryProperty mBankAccountId = new QueryProperty(BankAccountDao.TABLENAME, TransactionsDao.Properties.BankAccountId, BankAccountDao.Properties.Id);
    private QueryProperty mBusinessObjectBase = new QueryProperty(BusinessObjectBaseDao.TABLENAME, TransactionsDao.Properties.BusinessObjectId, BusinessObjectBaseDao.Properties.Id);
    private TransactionsDao mDao = ApplicationContext.getDaoSession().getTransactionsDao();

    public TransactionsAdapter(Activity activity, AmazingListView listView) {
    	this(activity, listView, false);
    }
    
	public TransactionsAdapter(Activity activity, AmazingListView listView, boolean useSections) {
		this.mActivity = activity;
		this.mListView = listView;
		mUseSections = useSections;
	}
    
    public void setOnDataLoadedListener(OnDataLoadedListener mOnDataLoadedListener) {
        this.mOnDataLoadedListener = mOnDataLoadedListener;
    }
    
    public List<Transactions> getTransactions() {
        return mAllTransactions;
    }

	public int getCount() {
		
		int res = 0;

		if (mUseSections) {
			for (int i = 0; i < mSections.size(); i++)
				res += mSections.get(i).second.size();
		} else {
			res = mAllTransactions.size();
		}
		
		return res;
	}

	public Transactions getItem(int position) {
		
		return mAllTransactions.get(position);
	}
	
	public void setDateRange(Date start, Date end) {
	    mStart = start;
	    mEnd = end;
	}
	
	public void setOrder(QueryProperty orderBy, boolean direction) {
	    mOrderBy = orderBy;
	    mDirection = direction;
	}

	public void setSearch(String search) {
        this.mSearch = search;
    }

    public void setQueries(List<PowerQuery> queries) {
        this.mQueries = queries;
    }

    public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {

		loadPage(page);
	}
	
	public void initializeData() {
		initializeData(true);
	}
	
	public void initializeData(boolean invalidate) {
	    
		mInvalidate = invalidate;
		
	    resetPage();
	    loadPage(1);
	}
	
	public void applyNewData() {
        
	    mListView.post(new Runnable() {
            
            @Override
            public void run() {
                
                if (mUseSections) {
	                mSections.clear();
	                mSections.addAll(mNewSections);
	                mNewSections.clear();
                }

                mAllTransactions.clear();
                mAllTransactions.addAll(mNewTransactions);
                
                mNewTransactions.clear();

                if (mInvalidate) {

            	    mListView.setSelection(0);
                	notifyDataSetInvalidated();
                	
                } else {
	                
                	notifyDataSetChanged();
                }

                if (mHasMore) {
                    notifyMayHaveMorePages();
                } else {
                    notifyNoMorePages();
                }
            }
        });
	}
	
	private void loadPage(final int page) {
	    
	    if (mBackgroundTask != null) {
            mBackgroundTask.cancel(false);
        }

        mBackgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>>() {
            
            @Override
            protected Pair<Boolean, List<Transactions>> doInBackground(Integer... params) {
                
                int page = params[0];

                Pair<Boolean, List<Transactions>> rows = Transactions.getRows(generateQuery(page));
                
                return rows;
            }

            @Override
            protected void onPostExecute(Pair<Boolean, List<Transactions>> rows) {

                if (isCancelled()) {
                    return;
                }

                if (page == 1) {
                    mHasMore = rows.first;
                    mNewTransactions.addAll(rows.second);
                    
                    if (mUseSections) {
	    				List<Pair<String, List<Transactions>>> grouped = Transactions.groupTransactions(mNewTransactions);
	    				
	    				mNewSections.clear();
	    				mNewSections.addAll(grouped);
                    }
    				
                    notifyDataLoaded();
                    return;
                }

                if (rows.first) {
                    notifyMayHaveMorePages();
                } else {
                    notifyNoMorePages();
                }
                
                nextPage();
                
                mAllTransactions.addAll(rows.second);
                
                if (mUseSections) {
					List<Pair<String, List<Transactions>>> grouped = Transactions.groupTransactions(mAllTransactions);
					
					mSections.clear();
					mSections.addAll(grouped);
                }
				
                notifyDataSetChanged();
            }

        }.execute(page);
	}
	
	private void notifyDataLoaded() {
	    if (mOnDataLoadedListener != null) {
            mOnDataLoadedListener.dataLoaded(mInvalidate);
        }
	}
	
	private PowerQuery generateQuery(int page) {
	    
	    int offset = (page - 1) * Constant.QUERY_LIMIT;
	    
	    boolean category = false;
	    boolean transactionTitle = false;
	    
	    PowerQuery subQuery = new PowerQuery(true);
	    
        PowerQuery query = new PowerQuery(mDao);
        query.join(mCategoryId)
            .join(mBankAccountId)
            .join(mTagInstance)
            .join(mBusinessObjectBase)
            .where(subQuery).and();
            
        for (PowerQuery powerQuery : mQueries) {
        	
        	query.where(powerQuery).and();
        	
	        category = powerQuery.hasQueryProperty(mCategoryName) || category;
	        transactionTitle = powerQuery.hasQueryProperty(mTransactionTitle) || transactionTitle;
        }
        
        query.where(mDataState, Integer.toString(DataState.DATA_STATE_DELETED.index())).and()
            .orderBy(mOrderBy, mDirection)
            .limit(Constant.QUERY_LIMIT)
            .offset(offset);
    	
    	if (!category) {
	        subQuery.or().whereLike(mCategoryName, mSearch);
	    }
    	
        if (!transactionTitle) {
            subQuery.or().whereLike(mTransactionTitle, mSearch);
        }
        
        if (mStart != null && mEnd != null) {
            query.between(mTransactionDate, mStart, mEnd);
        }
        
        return query;
	}
    
    public interface OnDataLoadedListener {
        public void dataLoaded(boolean invalidate);
    }
}
