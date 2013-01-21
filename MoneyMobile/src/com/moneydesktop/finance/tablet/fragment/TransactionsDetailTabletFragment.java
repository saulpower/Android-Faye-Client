package com.moneydesktop.finance.tablet.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.TagDao;
import com.moneydesktop.finance.database.TagInstance;
import com.moneydesktop.finance.database.TagInstanceDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionDetailBaseFragment;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.util.Fonts;

public class TransactionsDetailTabletFragment extends TransactionDetailBaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();
    
    private onBackPressedListener mListener;
    private TagInstanceDao mTagInstanceDao;
    private TagDao mTagDao;
    
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
        
        mTagInstanceDao = ApplicationContext.getDaoSession().getTagInstanceDao();
        mTagDao = ApplicationContext.getDaoSession().getTagDao();
        
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Here");
            }
        });
        
        mTags.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Title");
                alert.setMessage("Message");

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        createTag(value);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });
        
        return mRoot;
    }
    
    private void createTag(String tagName) {
        Tag tag = new Tag();
        tag.setTagName(tagName);
        tag.setTagId(tagName);
        mTagDao.insert(tag);
        
        TagInstance ti = new TagInstance();
        ti.setBaseObjectId(mTransaction.getBusinessObjectId());
        ti.setTag(tag);
        mTagInstanceDao.insert(ti);
        
        mTags.setText(mTransaction.buildTagString());
    }
    
    public void updateTransaction(Transactions transaction) {
        
        mTransaction = transaction;
        loadTransaction();
    }
    
    @Override
    protected void applyFonts() {
        
        Fonts.applyPrimaryBoldFont(mAccountName, 16);
        Fonts.applyPrimaryBoldFont(mBankName, 10);
        Fonts.applyPrimaryBoldFont(mPayee, 20);
        Fonts.applyPrimaryBoldFont(mAmount, 34);
        Fonts.applyPrimaryBoldFont(mDate, 20);
        Fonts.applyPrimaryBoldFont(mCategory, 20);
        Fonts.applyPrimaryBoldFont(mTags, 10);
        Fonts.applyPrimaryBoldFont(mMemo, 20);
        Fonts.applyPrimaryBoldFont(mStatement, 10);
        
        // labels
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.payee), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.amount_label), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.date_label), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.category), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.tags_label), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.markers_label), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.memo_label), 10);
        Fonts.applyPrimaryBoldFont((TextView) mRoot.findViewById(R.id.stmt_label), 10);
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
