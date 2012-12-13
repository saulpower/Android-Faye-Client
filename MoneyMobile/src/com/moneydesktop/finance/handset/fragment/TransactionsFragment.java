package com.moneydesktop.finance.handset.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.handset.adapter.TransactionsHandsetAdapter;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.views.AmazingListView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

import java.util.List;

public class TransactionsFragment extends BaseFragment implements OnItemClickListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static TransactionsFragment sFragment;
	private static String sAccountNumber;

	private AmazingListView mTransactionsList;
	private TransactionsHandsetAdapter mAdapter;
	private RelativeLayout mLoading;
	
	private boolean mLoaded = false;
	
	public static TransactionsFragment newInstance(String accountNumber) {
			
		sFragment = new TransactionsFragment();
		sAccountNumber = accountNumber;
	
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.mActivity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.mActivity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.handset_transactions_view, null);

		mLoading = (RelativeLayout) mRoot.findViewById(R.id.loading);
		
		mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
		mTransactionsList.setOnItemClickListener(this);
		
		configureView();
		
		if (!mLoaded)
			getInitialTransactions();
		else
			setupList();
		
		return mRoot;
	}
	
	private void getInitialTransactions() {

		new AsyncTask<Integer, Void, Void>() {
			
			@Override
			protected Void doInBackground(Integer... params) {
				
				int page = params[0];

				Transactions.summarizedTransactions(Long.valueOf(sAccountNumber));
				List<Transactions> row1 = Transactions.getRows(page).second;
				List<Pair<String, List<Transactions>>> initial = Transactions.groupTransactions(row1);

				mAdapter = new TransactionsHandsetAdapter(mActivity, mTransactionsList, initial);
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {

				setupList();
			};
			
		}.execute(1);
	}
	
	private void setupList() {

		mTransactionsList.setAdapter(mAdapter);
		mTransactionsList.setPinnedHeaderView(LayoutInflater.from(mActivity).inflate(R.layout.handset_item_transaction_header, mTransactionsList, false));
		mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
		
		mAdapter.notifyMayHaveMorePages();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		Transactions transaction = (Transactions) parent.getItemAtPosition(position);
		
		if (transaction != null) {
			
			TransactionDetailFragment detail = TransactionDetailFragment.newInstance(transaction.getId());
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.out_right, R.anim.in_left);
			ft.replace(R.id.fragment, detail);
			ft.addToBackStack(null);
			ft.commit();
		}
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions);
	}

	private void configureView() {
		
		if (mLoaded) {
			
			mTransactionsList.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.GONE);
		}
	}
	
	public void onEvent(ParentAnimationEvent event) {
		
		if (event.isOutAnimation() && event.isFinished() && !mLoaded) {
			
			animate(mLoading).alpha(0.0f).setDuration(400).setListener(new AnimatorListener() {
				
				public void onAnimationStart(Animator animation) {}
				
				public void onAnimationRepeat(Animator animation) {}
				
				public void onAnimationEnd(Animator animation) {
					mLoading.setVisibility(View.GONE);
				}
				
				public void onAnimationCancel(Animator animation) {}
			});
			
			mTransactionsList.setVisibility(View.VISIBLE);
			animate(mTransactionsList).alpha(1.0f).setDuration(400);
			
			mLoaded = true;
		}
	}

    @Override
    public boolean onBackPressed() {
        // TODO Auto-generated method stub
        return false;
    }
}
