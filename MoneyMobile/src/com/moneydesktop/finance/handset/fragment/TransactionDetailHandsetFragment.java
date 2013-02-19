package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.shared.fragment.TransactionDetailBaseFragment;

public class TransactionDetailHandsetFragment extends TransactionDetailBaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static TransactionDetailHandsetFragment newInstance(Activity activity) {
		
		TransactionDetailHandsetFragment frag = new TransactionDetailHandsetFragment();
		frag.inflateView(activity);
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}

	@Override
	public FragmentType getType() {
		return null;
	}
	
	public void inflateView(Activity activity) {
		mRoot = activity.getLayoutInflater().inflate(R.layout.handset_transaction_detail_view, null);
        initialize();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (mRoot != null) {
			
			View oldParent = (View) mRoot.getParent();
			
			if (oldParent != null && oldParent != container) {
				((ViewGroup) oldParent).removeView(mRoot);
			}
			
			return mRoot;
			
		} else {
		
			mRoot = inflater.inflate(R.layout.handset_transaction_detail_view, null);
	        initialize();
		}
		
		return mRoot;
	}
	
	public void setTransactionId(long guid) {
		getArguments().putLong(Constant.KEY_GUID, guid);
		loadTransaction();
	}
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_fragment_transaction).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
    
    @Override
    protected void showFragment(BaseFragment fragment) {
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(R.id.transactions_fragment, fragment);
		ft.addToBackStack(null);
		ft.commit();
    }
}
