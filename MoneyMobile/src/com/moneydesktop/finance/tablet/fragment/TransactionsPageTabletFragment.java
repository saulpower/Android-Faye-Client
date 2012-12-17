package com.moneydesktop.finance.tablet.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.views.AmazingListView;

import de.greenrobot.event.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@TargetApi(11)
public class TransactionsPageTabletFragment extends BaseFragment implements OnItemClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private AmazingListView mTransactionsList;
    private TransactionsTabletFragment mParent;
    private int[] mLocation = new int[2];
    private TransactionsTabletAdapter mAdapter;
    
    private boolean mLoaded = false;
    private boolean mWaiting = true;
    
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
        
        mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
        mTransactionsList.setOnItemClickListener(this);
        
        if (!mLoaded)
            getInitialTransactions();
        else
            setupList();
        
        return mRoot;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
    }
    
    private void setupList() {

        mTransactionsList.setAdapter(mAdapter);
        mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
        
        mAdapter.notifyMayHaveMorePages();
        
        if (!mWaiting) {
            configureView();
        }
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

        new AsyncTask<Integer, Void, Void>() {
            
            @Override
            protected Void doInBackground(Integer... params) {
                
                int page = params[0];
                
                Date today = new Date();
                
                Calendar c = Calendar.getInstance();    
                c.setTime(today);
                c.add(Calendar.MONTH, -1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                
                Date start = c.getTime();
                
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.add(Calendar.MONTH, 2);
                
                Date end = c.getTime();
                
                List<Transactions> row1 = Transactions.getRows(page, start, end).second;

                mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList, row1);
                mAdapter.setDateRange(start, end);
                
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {

                mLoaded = true;
                setupList();
            };
            
        }.execute(1);
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

}
