package com.moneydesktop.finance.tablet.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.tablet.adapter.FilterTabletAdapter;
import com.moneydesktop.finance.views.NavBarButtons;
import com.moneydesktop.finance.views.UltimateListView;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class TransactionsTabletFragment extends ParentTransactionFragment implements OnChildClickListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
    private UltimateListView mFiltersList;
    private FilterTabletAdapter mAdapter;
	
	public static TransactionsTabletFragment newInstance() {
			
	    TransactionsTabletFragment fragment = new TransactionsTabletFragment();
	    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        EventBus.getDefault().register(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_transactions_view, null);
		
		setupView();
		setupFilterList();
		
		TransactionsPageTabletFragment frag = TransactionsPageTabletFragment.newInstance(this);
      
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, frag);
        ft.commit();
		
		return mRoot;
	}
    
    @Override
    public void onResume() {
        super.onResume();
        
        setupTitleBar(getActivity());
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    Fragment frag = getChildFragmentManager().findFragmentById(R.id.fragment);
	    if (frag != null) {
	        frag.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	@Override
	public void setupView() {
	    super.setupView();
	    
        mFiltersList = (UltimateListView) mRoot.findViewById(R.id.filters);
        mFiltersList.setDividerHeight(0);
        mFiltersList.setDivider(null);
        mFiltersList.setChildDivider(null);
	}
	
	public void onEvent(DatabaseSaveEvent event) {
	    
	    if (mAdapter != null) {
	        mAdapter.reloadSections();
	    }
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
            
            Pair<String, List<FilterViewHolder>> temp = new Pair<String, List<FilterViewHolder>>(getString(Constant.FILTERS[j]), subItems);
            data.add(temp);
        }
        
        mAdapter = new FilterTabletAdapter(mActivity, mFiltersList, data);
        mAdapter.setAutomaticSectionLoading(true);
        mFiltersList.setAdapter(mAdapter);
        mFiltersList.setOnChildClickListener(this);
        mFiltersList.setSelectedChild(0, 0, true);
	}

    private void setupTitleBar(final Activity activity) {
        
        String[] icons = getResources().getStringArray(R.array.transactions_title_bar_icons);
        
        ArrayList<OnClickListener> onClickListeners = new ArrayList<OnClickListener>();
       
        onClickListeners.add(new OnClickListener() {
        	
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "help", Toast.LENGTH_LONG).show();
            }
        });
        
        new NavBarButtons(activity, icons, onClickListeners);
     }
    
	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions).toUpperCase();
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
            mAdapter.expandSubSection(groupPosition, childPosition, holder.mSubSection);
        }
        
        return true;
    }
}
