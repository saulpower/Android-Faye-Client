package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionDetailBaseFragment;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.util.Fonts;

public class TransactionsDetailTabletFragment extends TransactionDetailBaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private onBackPressedListener mListener;
    
    public void setListener(onBackPressedListener mListener) {
        this.mListener = mListener;
    }

    public static TransactionsDetailTabletFragment newInstance() {
        
        TransactionsDetailTabletFragment frag = new TransactionsDetailTabletFragment();
        
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (mActivity != null && mActivity instanceof DashboardTabletActivity) {
            ((DashboardTabletActivity) mActivity).setDetailFragment(this);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_transaction_detail_view, null);
        setupViews();
        
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Here");
            }
        });
        
        return mRoot;
    }
    
    public void updateTransaction(Transactions transaction) {
        
        mTransaction = transaction;
        loadTransaction();
    }
    
    @Override
    protected void applyFonts() {
        
        Fonts.applyPrimaryBoldFont(mAccountName, 18);
        Fonts.applyPrimaryBoldFont(mBankName, 12);
        Fonts.applyPrimaryBoldFont(mPayee, 24);
        Fonts.applyPrimaryBoldFont(mAmount, 40);
        Fonts.applyPrimaryBoldFont(mDate, 24);
        Fonts.applyPrimaryBoldFont(mCategory, 24);
        Fonts.applyPrimaryBoldFont(mTags, 24);
        Fonts.applyPrimaryBoldFont(mMemo, 24);
        Fonts.applyPrimaryBoldFont(mStatement, 12);
        
        // labels
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.payee), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.amount_label), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.date_label), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.category), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.tags_label), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.markers_label), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.memo_label), 12);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.stmt_label), 12);
    }
    
    public interface onBackPressedListener {
        public void onFragmentBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        
        if (mTransaction != null && mListener != null) {
            mListener.onFragmentBackPressed();
            
            return true;
        }
        
        return false;
    }
}
