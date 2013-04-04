package com.moneydesktop.finance.handset.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.TagInstance;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.MenuEvent;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.shared.adapter.FilterAdapter;
import com.moneydesktop.finance.shared.fragment.TransactionsFragment;
import com.moneydesktop.finance.tablet.fragment.BankListTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.UltimateListView;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionsHandsetFragment extends TransactionsFragment implements OnItemClickListener, OnChildClickListener {
	
	public final String TAG = this.getClass().getSimpleName();

	private TransactionDetailHandsetFragment mDetail;
	
    private View mFilterListView;
    private TextView mBack, mTitle;
    private UltimateListView mFiltersList;
    private FilterAdapter mFilterAdapter;
    private Date mStart, mEnd;

    private boolean mSubList = false;
    
    private int mFragmentResource = R.id.transactions_fragment;
	
	public static TransactionsHandsetFragment newInstance() {
			
	    TransactionsHandsetFragment fragment = new TransactionsHandsetFragment();
	    fragment.setRetainInstance(true);
	
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
    public static TransactionsHandsetFragment newInstance(Intent intent) {
        
    	TransactionsHandsetFragment fragment = new TransactionsHandsetFragment();
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
        
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }

    @SuppressWarnings("unchecked")
	public static TransactionsHandsetFragment newInstance(Intent intent, int fragmentResource) {
    	
	    TransactionsHandsetFragment fragment = new TransactionsHandsetFragment();
	    fragment.setRetainInstance(true);
        fragment.setSubList(true);
	    fragment.setFragmentResource(fragmentResource);
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setCategories((ArrayList<Long>) intent.getSerializableExtra(Constant.EXTRA_CATEGORY_ID));
        fragment.setCategoryType(intent.getIntExtra(Constant.EXTRA_CATEGORY_TYPE, -1));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
        
        if (intent.hasExtra(Constant.EXTRA_START_DATE) && intent.hasExtra(Constant.EXTRA_END_DATE)) {
        	fragment.setStart(new Date(intent.getLongExtra(Constant.EXTRA_START_DATE, 0)));
        	fragment.setEnd(new Date(intent.getLongExtra(Constant.EXTRA_END_DATE, 0)));
        }
	
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}

    public void setSubList(boolean mSubList) {
        this.mSubList = mSubList;
    }
    
    public void setFragmentResource(int resource) {
    	mFragmentResource = resource;
    }

	@Override
	public FragmentType getType() {
		return mSubList ? FragmentType.TRANSACTIONS_SUB : FragmentType.TRANSACTIONS;
	}
    
    public void setStart(Date mStart) {
		this.mStart = mStart;
	}

	public void setEnd(Date mEnd) {
		this.mEnd = mEnd;
	}

	@Override
    public void isShowing() {
        super.isShowing();

		setupMenuItems();

        if (mLoaded) {
            mAdapter.refreshCurrentSelection();
        }
    }

    @Override
    public void isHiding() {

        mTransactionsList.setSelection(0);
        mSearch.setText("");
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (mRoot != null) {
			
			View oldParent = (View) mRoot.getParent();
			
			if (oldParent != container) {
				((ViewGroup) oldParent).removeView(mRoot);
			}
			
			return mRoot;
		}
		
		mRoot = inflater.inflate(R.layout.handset_transactions_view, container, false);
		
		getDetailFragment();
		
		return mRoot;
	}
	
	private void setupMenuItems() {

		List<Pair<Integer, List<int[]>>> menu = new ArrayList<Pair<Integer, List<int[]>>>();

    	List<int[]> communication = new ArrayList<int[]>();
        List<int[]> edit = new ArrayList<int[]>();
        List<int[]> filter = new ArrayList<int[]>();

        communication.add(new int[]{R.string.nav_icon_email, R.string.label_email_transactions});
        edit.add(new int[]{R.string.nav_icon_mark, R.string.label_mark_read});

        if (!mSubList) {
            edit.add(new int[]{R.string.nav_icon_add, R.string.label_add_transaction});
            filter.add(new int[]{R.string.nav_icon_filter, R.string.label_show_filters});
        }
    	
    	menu.add(new Pair<Integer, List<int[]>>(R.string.label_communication, communication));
        menu.add(new Pair<Integer, List<int[]>>(R.string.label_edit, edit));
        if (!mSubList) {
            menu.add(new Pair<Integer, List<int[]>>(R.string.label_filter, filter));
        }

    	mActivity.configureRightMenu(menu, getType());
	}

    public void onEvent(MenuEvent event) {

        if (event.getFragmentType().equals(getType())) {

            switch (event.getAction()) {
                case ((0 << 8) + 0):
                    emailTransactions(null);
                    break;
                case ((1 << 8) + 0):

                    if (!mSubList) Transactions.setAllRead();

                    for (Transactions t : mAdapter.getTransactions()) {
                        t.setIsProcessed(true);
                        if (mSubList) t.updateBatch();
                    }

                    if (mSubList) DataController.save();

                    refreshTransactionsList();
                    break;
                case ((1 << 8) + 1):
                    mActivity.pushFragment(getId(), BankListTabletFragment.newInstance());
                    break;
                case ((2 << 8) + 0):
                    mActivity.pushMenuView(mFilterListView);
                    break;
            }
        }
    }
	
	private void setupFilterList() {

	    List<Pair<String, List<FilterViewHolder>>> data = new ArrayList<Pair<String, List<FilterViewHolder>>>();
        
        for (int j = 0; j < Constant.FILTERS.length; j++) {

            List<FilterViewHolder> subItems = new ArrayList<FilterViewHolder>();
            
            if (j == 0) {

                for (int i = 0; i < Constant.FOLDER_TITLE.length; i++) {
                    FilterViewHolder holder = new FilterViewHolder();
                    holder.mText = mActivity.getString(Constant.FOLDER_TITLE[i]);
                    holder.mSubText = mActivity.getString(Constant.FOLDER_SUBTITLE[i]);
                    holder.mQuery = Constant.FOLDER_QUERIES[i];
                    subItems.add(holder);
                }
            }
            
            Pair<String, List<FilterViewHolder>> temp = new Pair<String, List<FilterViewHolder>>(mActivity.getString(Constant.FILTERS[j]).toUpperCase(), subItems);
            data.add(temp);
        }
        
        mFilterAdapter = new FilterAdapter(mActivity, mFiltersList, data, true);
        mFilterAdapter.setAutomaticSectionLoading(true);
        mFiltersList.setAdapter(mFilterAdapter);
        mFiltersList.setOnChildClickListener(this);
        mFiltersList.setSelectedChild(0, 0, true);
	}
	
	@Override
	protected void setupView() {
		super.setupView();
		
		mFilterListView = mActivity.getLayoutInflater().inflate(R.layout.handset_transaction_filters, null, false);
		mBack = (TextView) mFilterListView.findViewById(R.id.menu_left_button);
		mTitle = (TextView) mFilterListView.findViewById(R.id.menu_title);
		mFiltersList = (UltimateListView) mFilterListView.findViewById(R.id.filters);
        mFiltersList.setDividerHeight(0);
        mFiltersList.setDivider(null);
        mFiltersList.setChildDivider(null);
		
        mSearch.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        
        mTransactionsList.addHeaderView(mSearch);
		mTransactionsList.setPinnedHeaderView(mActivity.getLayoutInflater().inflate(R.layout.handset_item_transaction_header, mTransactionsList, false));
		mTransactionsList.setOnItemClickListener(this);
		
		applyFonts();

		setupListeners();
		
		setupFilterList();
	}
    
    @Override
    public void dataLoaded(boolean invalidate) {
    	
        mLoaded = true;
        mAdapter.applyNewData();
    }
	
	@Override
	protected EditText getSearchBar() {
		return (EditText) mActivity.getLayoutInflater().inflate(R.layout.search_bar, null, false);
	}
	
	private void applyFonts() {
		
		Fonts.applyPrimarySemiBoldFont(mTitle, 10);
		Fonts.applyNavIconFont(mBack, 24);
	}
	
	private void setupListeners() {
		
		mBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mActivity.popMenuView();
			}
		});
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        UiUtils.hideKeyboard(mActivity, mSearch);

		Transactions transaction = (Transactions) parent.getItemAtPosition(position);
		
		if (transaction != null) {
			
			if (!transaction.getIsProcessed()) {
                transaction.setIsProcessed(true);
                transaction.updateSingle();
            }
			
			TransactionDetailHandsetFragment frag = getDetailFragment();
			frag.setTransactionId(transaction.getId());

            mActivity.pushFragment(mFragmentResource, frag);
		}
	}
	
	private TransactionDetailHandsetFragment getDetailFragment() {
		
		if (mDetail == null) {
			mDetail = TransactionDetailHandsetFragment.newInstance(mActivity, mFragmentResource);
		}
		
		return mDetail;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions).toUpperCase();
	}
	
	public void onEvent(DatabaseSaveEvent event) {
	    
	    if (mFilterAdapter != null && event.didDatabaseChange()
                && (event.getChangedClassesList().contains(Transactions.class)
                || event.getChangedClassesList().contains(TagInstance.class))) {

	        mFilterAdapter.reloadSections();
    		refreshTransactionsList(false);
	    }
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		
		mFiltersList.setSelectedChild(groupPosition, childPosition, true);
        FilterViewHolder holder = (FilterViewHolder) v.getTag();
        
        // Notify transaction list view of new filter
        EventBus.getDefault().post(new EventMessage().new FilterEvent(holder.mQuery));
        
        // Expand any existing sub sections (PAYEES)
        if (holder != null && holder.mSubSection != null) {
            mFilterAdapter.expandSubSection(groupPosition, childPosition, holder.mSubSection);
        }
        
        return true;
	}

	@Override
	protected Date getStartDate() {
		return mStart;
	}

	@Override
	protected Date getEndDate() {
		return mEnd;
	}

	@Override
	protected Object getChildInstance() {
		return this;
	}
}
