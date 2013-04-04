package com.moneydesktop.finance.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.Institution;
import com.moneydesktop.finance.database.InstitutionDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.shared.adapter.AmazingAdapter;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AddNewInstitutionAdapter extends AmazingAdapter {

	private Context mContext;
	private int mLayoutId;
	private ListView mListView;
	private boolean mHasMore = false;
	private InstitutionDao mDAO;
	private boolean mInvalidate = true;
	private EditText mFilter;
	
    private List<Institution> mInstitutions = new ArrayList<Institution>();
    private List<Institution> mNewInstitutions = new ArrayList<Institution>();
    
    private AsyncTask<Integer, Void, Pair<Boolean, List<Institution>>> mBackgroundTask;
    private OnDataLoadedListener mOnDataLoadedListener;
    private Handler mHandler = new Handler();
	
    
	protected List<Pair<String, List<Institution>>> mSections = new ArrayList<Pair<String, List<Institution>>>();
	private List<Pair<String, List<Institution>>> mNewSections = new ArrayList<Pair<String, List<Institution>>>();
    
	private QueryProperty mWherePopularity = new QueryProperty(InstitutionDao.TABLENAME, InstitutionDao.Properties.Popularity, "!= ?");
	private QueryProperty mWhereLike = new QueryProperty(InstitutionDao.TABLENAME, InstitutionDao.Properties.Name);
	private QueryProperty mOrderBy = new QueryProperty(InstitutionDao.TABLENAME, InstitutionDao.Properties.Name);

	private String filterString;
	
	Runnable mFilterTask = new Runnable() {
	     @Override
	     public void run() {
	          fillList();
	     }
	};
		
	private void fillList() {
		if (filterString.length() > 0) {

			PowerQuery powerQuery = new PowerQuery(mDAO);
			powerQuery.whereLike(mWhereLike, filterString + "%")
			.orderBy(mOrderBy, false);
			
			mInstitutions = mDAO.queryRaw(powerQuery.toString(), powerQuery.getSelectionArgs());
			
			if (mInstitutions.size() > 0 && mInstitutions.size() <= Constant.QUERY_LIMIT) {
				mListView.setVisibility(View.VISIBLE);
				notifyDataSetInvalidated();
				initializeData();
				
			} else if (mInstitutions.size() == 0) {
				mListView.setVisibility(View.GONE);
			}
			
		} else {		
			PowerQuery powerQuery = new PowerQuery(mDAO);
			powerQuery.where(mWherePopularity, "0")
			.orderBy(mOrderBy, false);
			
			mInstitutions = mDAO.queryRaw(powerQuery.toString(), powerQuery.getSelectionArgs());
			mListView.setVisibility(View.VISIBLE);
			
			notifyDataSetInvalidated();
			initializeData();
		}
		
	}     
	
	public AddNewInstitutionAdapter(Context context, int layoutResourceId, List<Institution> institutions, EditText filter, final ListView addInstitutionList) {
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mInstitutions = institutions;
		mListView = addInstitutionList;
		
		
		
		mFilter = filter;
		final InstitutionDao dao = ApplicationContext.getDaoSession().getInstitutionDao();
		mDAO = dao;
		
		mFilter.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {				
			}
			
			@Override
			public void afterTextChanged(final Editable s) {
				
				filterString = s.toString();
		        mHandler.removeCallbacks(mFilterTask); 
		        mHandler.postDelayed(mFilterTask, 2000);
			}
		});
	}

	
    static class AddInstitutionListHolder
    {
    	ImageView imageLogo;
        TextView txtTitle;
    }

	@Override
	public int getCount() {
		return mInstitutions.size();	
	}

	@Override
	public Object getItem(int position) {
		return mInstitutions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {
		loadPage(page);
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {
		
	}

	@Override
	public View getAmazingView(final int position, View convertView, ViewGroup parent) {
		AddInstitutionListHolder holder;
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(mLayoutId, parent, false);
			holder = new AddInstitutionListHolder();
			
			holder.imageLogo = (ImageView)convertView.findViewById(R.id.add_bank_institution_logo);
			holder.txtTitle = (TextView)convertView.findViewById(R.id.add_bank_institution_name_item);
			
			convertView.setTag(holder);
		} else {
			holder = (AddInstitutionListHolder) convertView.getTag();
		}
            Bitmap bitmap = BankLogoManager.getBitmapFromMemCache(mInstitutions.get(position).getInstitutionId());
            if (bitmap == null) {
                BankLogoManager.getBankImage(holder.imageLogo, mInstitutions.get(position).getInstitutionId());
            } else {
                holder.imageLogo.setImageBitmap(bitmap);
            }

			holder.txtTitle.setText(mInstitutions.get(position).getName());			
			Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);
			
			return convertView;
	}
	
	
	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		
	}

	@Override
	protected boolean isPositionVisible(int position) {
		return false;
	}

	@Override
	protected boolean isSectionVisible(int section) {
		return false;
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
	public Object[] getSections() {
		return null;
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
        
	    mListView.setSelection(0);
	    mListView.post( new Runnable() {
            
            @Override
            public void run() {
           	
            	mInstitutions.clear();
            	mInstitutions.addAll(mNewInstitutions);
            	mNewInstitutions.clear();
            	
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
	
    public void setOnDataLoadedListener(OnDataLoadedListener mOnDataLoadedListener) {
        this.mOnDataLoadedListener = mOnDataLoadedListener;
    }
	
	private void loadPage(final int page) {
	    
	    if (mBackgroundTask != null) {
            mBackgroundTask.cancel(false);
        }

        mBackgroundTask = new AsyncTask<Integer, Void, Pair<Boolean, List<Institution>>>() {
           
            @Override
            protected Pair<Boolean, List<Institution>> doInBackground(Integer... params) {
                
                int page = params[0];

                Pair<Boolean, List<Institution>> rows = Institution.getRows(generateQuery(page));
                
                return rows;
            }

            @Override
            protected void onPostExecute(Pair<Boolean, List<Institution>> rows) {

                if (isCancelled()) {
                    return;
                }

                if (page == 1) {
                    mHasMore = rows.first;
                    mNewInstitutions.addAll(rows.second);
                   
                    notifyDataLoaded();
                    return;
                }

                if (rows.first) {
                    notifyMayHaveMorePages();
                } else {
                    notifyNoMorePages();
                }
                
                nextPage();
                
                mInstitutions.addAll(rows.second);
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
	    
        PowerQuery query = new PowerQuery(mDAO);  
        query.whereLike(mWhereLike, mFilter.getText().toString() + "%")        
        .orderBy(mOrderBy, false)
        .limit(Constant.QUERY_LIMIT)
        .offset(offset);
        
        return query;
	}

}
