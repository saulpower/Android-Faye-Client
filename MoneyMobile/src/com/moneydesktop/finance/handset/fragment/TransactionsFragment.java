package com.moneydesktop.finance.handset.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.List;

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

import com.moneydesktop.finance.BaseActivity.AppearanceListener;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.TransactionsAdapter;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.views.AmazingListView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

public class TransactionsFragment extends BaseFragment implements OnItemClickListener, AppearanceListener {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static TransactionsFragment fragment;

	private AmazingListView transactionsList;
	private TransactionsAdapter adapter;
	private RelativeLayout loading;
	
	private boolean loaded = false;
	
	public static TransactionsFragment newInstance() {
			
		fragment = new TransactionsFragment();
	
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.activity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_transactions_view, null);

		loading = (RelativeLayout) root.findViewById(R.id.loading);
		
		transactionsList = (AmazingListView) root.findViewById(R.id.transactions);
		transactionsList.setOnItemClickListener(this);
		
		configureView();
		
		if (!loaded)
			getInitialTransactions();
		else
			setupList();
		
		return root;
	}
	
	private void getInitialTransactions() {

		new AsyncTask<Integer, Void, Void>() {
			
			@Override
			protected Void doInBackground(Integer... params) {
				
				int page = params[0];

				List<Transactions> row1 = Transactions.getRows(page).second;
				List<Pair<String, List<Transactions>>> initial = Transactions.groupTransactions(row1);

				adapter = new TransactionsAdapter(activity, transactionsList, initial);
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {

				setupList();
			};
			
		}.execute(1);
	}
	
	private void setupList() {

		transactionsList.setAdapter(adapter);
		transactionsList.setPinnedHeaderView(LayoutInflater.from(activity).inflate(R.layout.item_transaction_header, transactionsList, false));
		transactionsList.setLoadingView(activity.getLayoutInflater().inflate(R.layout.loading_view, null));
		
		adapter.notifyMayHaveMorePages();
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
		
		if (loaded) {
			
			transactionsList.setVisibility(View.VISIBLE);
			loading.setVisibility(View.GONE);
		}
	}
	
	public void onViewDidAppear() {
		
		if (!loaded) {
			
			animate(loading).alpha(0.0f).setDuration(400).setListener(new AnimatorListener() {
				
				public void onAnimationStart(Animator animation) {}
				
				public void onAnimationRepeat(Animator animation) {}
				
				public void onAnimationEnd(Animator animation) {
					loading.setVisibility(View.GONE);
				}
				
				public void onAnimationCancel(Animator animation) {}
			});
			
			transactionsList.setVisibility(View.VISIBLE);
			animate(transactionsList).alpha(1.0f).setDuration(400);
			
			loaded = true;
		}
	}
}
