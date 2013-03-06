package com.moneydesktop.finance.handset.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.MenuEvent;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.shared.adapter.FilterAdapter;
import com.moneydesktop.finance.shared.fragment.TransactionsFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.UltimateListView;

import de.greenrobot.event.EventBus;

public class TransactionsHandsetFragment extends TransactionsFragment implements OnItemClickListener, OnChildClickListener {
	
	public final String TAG = this.getClass().getSimpleName();

	private TransactionDetailHandsetFragment mDetail;
	
    private View mFilterListView;
    private TextView mBack, mTitle;
    private UltimateListView mFiltersList;
    private FilterAdapter mFilterAdapter;
    
    private int mFragmentResource = R.id.transactions_fragment;
	
	public static TransactionsHandsetFragment newInstance() {
			
	    TransactionsHandsetFragment frag = new TransactionsHandsetFragment();
	
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}

    @SuppressWarnings("unchecked")
	public static TransactionsHandsetFragment newInstance(Intent intent, int fragmentResource) {
    	
	    TransactionsHandsetFragment fragment = new TransactionsHandsetFragment();
	    fragment.setFragmentResource(fragmentResource);
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setCategories((ArrayList<Long>) intent.getSerializableExtra(Constant.EXTRA_CATEGORY_ID));
        fragment.setCategoryType(intent.getIntExtra(Constant.EXTRA_CATEGORY_TYPE, -1));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
	
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
    
    public void setFragmentResource(int resource) {
    	mFragmentResource = resource;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.TRANSACTIONS;
	}
    
    @Override
    public void isShowing(boolean fromBackstack) {
    	super.isShowing(fromBackstack);

		setupMenuItems();
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
		
		List<Pair<Integer, List<int[]>>> data = new ArrayList<Pair<Integer, List<int[]>>>();

    	List<int[]> items = new ArrayList<int[]>();
    	items.add(new int[] {R.string.nav_icon_filter, R.string.label_show_filters });
    	items.add(new int[] {R.string.nav_icon_mark, R.string.label_mark_read });
    	
    	data.add(new Pair<Integer, List<int[]>>(R.string.menu_transactions, items));
    	
    	mActivity.addMenuItems(data);
	}
	
	private void setupFilterList() {

	    List<Pair<String, List<FilterViewHolder>>> data = new ArrayList<Pair<String, List<FilterViewHolder>>>();
        
        for (int j = 0; j < Constant.FILTERS.length; j++) {

            List<FilterViewHolder> subItems = new ArrayList<FilterViewHolder>();
            
            if (j == 0) {

                for (int i = 0; i < Constant.FOLDER_TITLE.length; i++) {
                    FilterViewHolder holder = new FilterViewHolder();
                    holder.mText = getString(Constant.FOLDER_TITLE[i]);
                    holder.mSubText = getString(Constant.FOLDER_SUBTITLE[i]);
                    holder.mQuery = Constant.FOLDER_QUERIES[i];
                    subItems.add(holder);
                }
            }
            
            Pair<String, List<FilterViewHolder>> temp = new Pair<String, List<FilterViewHolder>>(getString(Constant.FILTERS[j]).toUpperCase(), subItems);
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
		
		Transactions transaction = (Transactions) parent.getItemAtPosition(position - 1);
		
		if (transaction != null) {
			
			if (!transaction.getIsProcessed()) {
                transaction.setIsProcessed(true);
                transaction.updateSingle();
            }
			
			TransactionDetailHandsetFragment frag = getDetailFragment();
			frag.setTransactionId(transaction.getId());
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
			ft.replace(mFragmentResource, frag);
			ft.addToBackStack(null);
			ft.commit();
		}
	}
	
	private TransactionDetailHandsetFragment getDetailFragment() {
		
		if (mDetail == null) {
			mDetail = TransactionDetailHandsetFragment.newInstance(getActivity(), mFragmentResource);
		}
		
		return mDetail;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions).toUpperCase();
	}
	
	public void onEvent(MenuEvent event) {
	    
	    switch (event.getChildPosition()) {
		    case 0:
		    	mActivity.pushMenuView(mFilterListView);
		    	break;
		    case 1:
		    	Transactions.setAllRead();
		    	for (Transactions t : mAdapter.getTransactions()) {
		    		t.setIsProcessed(true);
		    	}
		    	refreshTransactionsList();
		    	break;
	    }
	}
	
	public void onEvent(DatabaseSaveEvent event) {
	    
	    if (mFilterAdapter != null && event.didDatabaseChange()) {
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
		return null;
	}

	@Override
	protected Date getEndDate() {
		return null;
	}

	@Override
	protected Object getChildInstance() {
		return this;
	}
}
