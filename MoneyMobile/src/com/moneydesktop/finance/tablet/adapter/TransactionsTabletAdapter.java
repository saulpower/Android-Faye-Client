package com.moneydesktop.finance.tablet.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AmazingAdapter;
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
import com.moneydesktop.finance.model.StopListFling;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.CaretView;
import com.moneydesktop.finance.views.VerticalTextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsTabletAdapter extends AmazingAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

    private OnDataLoadedListener mOnDataLoadedListener;

    private List<Transactions> mAllTransactions = new ArrayList<Transactions>();
    private List<Transactions> mNewTransactions = new ArrayList<Transactions>();
	private AsyncTask<Integer, Void, Pair<Boolean, List<Transactions>>> mBackgroundTask;
    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("M/d/yy");
	private AmazingListView mListView;
	private boolean mHasMore = false;
	
	private Date mStart, mEnd;
	
	private QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
	private boolean mDirection = true;
	private String mSearch = "%";
	private PowerQuery mQueries;

	private Activity mActivity;
	
    private QueryProperty mTransactionDate = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date, TransactionsDao.Properties.Id);
    private QueryProperty mTransactionTitle = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title, TransactionsDao.Properties.Id);
    private QueryProperty mTagInstance = new QueryProperty(TagInstanceDao.TABLENAME, TransactionsDao.Properties.BusinessObjectId, TagInstanceDao.Properties.BaseObjectId);
    private QueryProperty mDataState = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BusinessObjectBaseDao.Properties.DataState, QueryProperty.NOT_EQUALS);

    private QueryProperty mCategoryId = new QueryProperty(CategoryDao.TABLENAME, TransactionsDao.Properties.CategoryId, CategoryDao.Properties.Id);
    private QueryProperty mCategoryName = new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.CategoryName, CategoryDao.Properties.Id);
    private QueryProperty mBankAccountId = new QueryProperty(BankAccountDao.TABLENAME, TransactionsDao.Properties.BankAccountId, BankAccountDao.Properties.Id);
    private QueryProperty mBusinessObjectBase = new QueryProperty(BusinessObjectBaseDao.TABLENAME, TransactionsDao.Properties.BusinessObjectId, BusinessObjectBaseDao.Properties.Id);
    TransactionsDao mDao = ApplicationContext.getDaoSession().getTransactionsDao();

	public TransactionsTabletAdapter(Activity activity, AmazingListView listView) {
		this.mActivity = activity;
		this.mListView = listView;
	}
    
    public void setOnDataLoadedListener(OnDataLoadedListener mOnDataLoadedListener) {
        this.mOnDataLoadedListener = mOnDataLoadedListener;
    }
    
    public List<Transactions> getTransactions() {
        return mAllTransactions;
    }

	public int getCount() {

		return mAllTransactions.size();
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

    public void setQueries(PowerQuery queries) {
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
	    
	    resetPage();
	    loadPage(1);
	}
	
	public void applyNewData() {
        
	    mListView.setSelection(0);
	    mListView.post( new Runnable() {
            
            @Override
            public void run() {

                mAllTransactions.clear();
                mAllTransactions.addAll(mNewTransactions);
                mNewTransactions.clear();

                notifyDataSetInvalidated();

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
            protected void onPreExecute() {

                /* Stop the listview from flinging as it is causing crashes
                 * due to accessing data while the data is being changed
                 */
                StopListFling.stop(mListView);
            }
            
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
                notifyDataSetChanged();
            }

        }.execute(page);
	}
	
	private void notifyDataLoaded() {
	    if (mOnDataLoadedListener != null) {
            mOnDataLoadedListener.dataLoaded();
        }
	}
	
	private PowerQuery generateQuery(int page) {
	    
	    int offset = (page - 1) * Constant.QUERY_LIMIT;
	    
	    PowerQuery subQuery = new PowerQuery(true);
	    
	    if (mQueries == null || !mQueries.hasQueryProperty(mCategoryName)) {
	        subQuery.or().whereLike(mCategoryName, mSearch);
	    }
        if (mQueries == null || !mQueries.hasQueryProperty(mTransactionTitle)) {
            subQuery.or().whereLike(mTransactionTitle, mSearch);
        }
	    
        PowerQuery query = new PowerQuery(mDao);
        query.join(mCategoryId)
            .join(mBankAccountId)
            .join(mTagInstance)
            .join(mBusinessObjectBase)
            .where(subQuery).and()
            .where(mQueries).and()
            .where(mDataState, Integer.toString(DataState.DATA_STATE_DELETED.index())).and()
            .between(mTransactionDate, mStart, mEnd)
            .orderBy(mOrderBy, mDirection)
            .limit(Constant.QUERY_LIMIT)
            .offset(offset);
        
        return query;
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
	    TransactionViewHolder viewHolder;
		View res = convertView;
		
		if (res == null) {
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.tablet_transaction_item, parent, false);

			viewHolder = new TransactionViewHolder();
			
	        viewHolder.newText = (VerticalTextView) res.findViewById(R.id.text_new);
	        viewHolder.date = (TextView) res.findViewById(R.id.date);
	        viewHolder.payee = (TextView) res.findViewById(R.id.payee);
	        viewHolder.category = (TextView) res.findViewById(R.id.category);
	        viewHolder.amount = (TextView) res.findViewById(R.id.amount);
	        viewHolder.type = (ImageView) res.findViewById(R.id.type);
            viewHolder.flag = (ImageView) res.findViewById(R.id.flag);
	        viewHolder.caret = (CaretView) res.findViewById(R.id.caret); 
	        
	        res.setTag(viewHolder);
	        
	        applyFonts(viewHolder);
	        
		} else {
		    
		    viewHolder = (TransactionViewHolder) res.getTag();
		}
		
		final Transactions transactions = getItem(position);
		
		if (transactions != null) {
		
			viewHolder.date.setText(mDateFormatter.format(transactions.getDate()));
			viewHolder.payee.setText(transactions.getCapitalizedTitle());
			viewHolder.caret.setVisibility(transactions.isIncome() ? View.VISIBLE : View.INVISIBLE);
			
			viewHolder.newText.setText(!transactions.getIsProcessed() ? "NEW" : "");
			viewHolder.newText.setBackgroundResource(!transactions.getIsProcessed() ? R.drawable.primary_to_white : R.color.gray1);
			
			viewHolder.amount.setText(mFormatter.format(transactions.normalizedAmount()));
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.amount.getLayoutParams();
			int[] rules = params.getRules();
			rules[RelativeLayout.ALIGN_PARENT_LEFT] = 0;
            rules[RelativeLayout.ALIGN_PARENT_RIGHT] = -1;
            
            if (transactions.getTransactionType() == 1) {
                rules[RelativeLayout.ALIGN_PARENT_LEFT] = -1;
                rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 0;
            }

			viewHolder.type.setImageResource(transactions.getIsBusiness() ? R.drawable.ipad_txndetail_icon_business_color : R.drawable.ipad_txndetail_icon_personal_grey);
			viewHolder.flag.setVisibility(transactions.getIsFlagged() ? View.VISIBLE : View.INVISIBLE);
			viewHolder.category.setText(transactions.getCategory() != null ? transactions.getCategory().getCategoryName() : "");
		}

		return res;
	}
	
	private void applyFonts(TransactionViewHolder viewHolder) {

        Fonts.applyPrimaryBoldFont(viewHolder.newText, 8);
        Fonts.applyPrimarySemiBoldFont(viewHolder.date, 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.payee, 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.category, 10);
        Fonts.applyPrimaryBoldFont(viewHolder.amount, 10);
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
	}

	@Override
	public int getPositionForSection(int section) {

		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {

		return 0;
	}

	@Override
	public String[] getSections() {
		return null;
	}

    @Override
    protected boolean isSectionVisible(int position) {
        return true;
    }

    @Override
    protected boolean isPositionVisible(int position) {
        return true;
    }
    
    public interface OnDataLoadedListener {
        public void dataLoaded();
    }
}
