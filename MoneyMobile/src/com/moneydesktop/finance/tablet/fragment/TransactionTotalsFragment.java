package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;

public class TransactionTotalsFragment extends BaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static TransactionTotalsFragment newInstance(String[] values) {
			
	    TransactionTotalsFragment fragment = new TransactionTotalsFragment();
	    fragment.setValues(values);
	    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
	private String[] mValues;

    private TextView mAverage, mAverageLabel, mCount, mCountLabel, mSum, mSumLabel;
    
    public void setValues(String[] mValues) {
        this.mValues = mValues;
    }

	@Override
	public FragmentType getType() {
		return null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_transactions_total_view, null);
		
		if (mValues == null || mValues.length == 0) {
		    getActivity().finish();
		}
		
		setupViews();
		applyFonts();
		
        mCount.setText(mValues[0]);
        mAverage.setText(mValues[1]);
        mSum.setText(mValues[2]);
		
		return mRoot;
	}
	
	private void setupViews() {
	    
	    mAverage = (TextView) mRoot.findViewById(R.id.average);
	    mAverageLabel = (TextView) mRoot.findViewById(R.id.average_label);
	    mCount = (TextView) mRoot.findViewById(R.id.count);
	    mCountLabel = (TextView) mRoot.findViewById(R.id.count_label);
	    mSum = (TextView) mRoot.findViewById(R.id.sum);
	    mSumLabel = (TextView) mRoot.findViewById(R.id.sum_label);
	}
	
	private void applyFonts() {
	    
	    Fonts.applySecondaryItalicFont(mAverageLabel, 12);
        Fonts.applySecondaryItalicFont(mCountLabel, 12);
        Fonts.applySecondaryItalicFont(mSumLabel, 12);
        
        Fonts.applyPrimarySemiBoldFont(mAverage, 14);
        Fonts.applyPrimarySemiBoldFont(mCount, 14);
        Fonts.applyPrimarySemiBoldFont(mSum, 14);
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_fragment_transaction_totals);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
