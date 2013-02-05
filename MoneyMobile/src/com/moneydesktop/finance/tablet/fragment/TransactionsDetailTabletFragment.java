package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.DashboardBaseActivity;
import com.moneydesktop.finance.shared.TransactionDetailBaseFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import com.moneydesktop.finance.util.Fonts;

import java.util.List;

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
        
        if (mActivity != null && mActivity instanceof DashboardBaseActivity) {
            ((DashboardBaseActivity) mActivity).setDetailFragment(this);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_transaction_detail_view, null);
        initialize();
        
        if (mActivity instanceof DropDownTabletActivity) {
            ((DropDownTabletActivity) mActivity).setEditText(mPayee);
        }
        
        mRoot.setSoundEffectsEnabled(false);
        mRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // intercepting clicks
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
                        
                        List<Tag> tags = ApplicationContext.getDaoSession().getTagDao().loadAll();
                        if (tags.size() > 0) {
                            Tag tag = tags.get(0);
                            deleteTag(tag);
                        }
                    }
                });

                alert.show();
            }
        });
        
        return mRoot;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        
        long categoryId = data.getLongExtra(Constant.EXTRA_CATEGORY_ID, -1);
        
        if (categoryId != -1) {
            updateTransactionCategory(categoryId);
        }
    }
    
    @Override
    protected void configureListeners() {
        super.configureListeners();
        
        mCategoryContainer.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showCategoryPopup(v);
            }
        });
    }
    
    private void showCategoryPopup(View view) {

        int[] catLocation = new int[2];
        view.getLocationOnScreen(catLocation);
        
        int adjustedX = catLocation[0] + view.getWidth();
        int adjustedY = catLocation[1] + (view.getHeight() / 2);
        
        Intent intent = new Intent(getActivity(), PopupTabletActivity.class);
        intent.putExtra(Constant.EXTRA_POSITION_X, adjustedX);
        intent.putExtra(Constant.EXTRA_POSITION_Y, adjustedY);
        intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.POPUP_CATEGORIES);
        intent.putExtra(Constant.EXTRA_SOURCE_CODE, Constant.CODE_CATEGORY_DETAIL);
        startActivityForResult(intent, Constant.CODE_CATEGORY_DETAIL);
    }
    
    public void updateTransaction(Transactions transaction) {
        
        mTransaction = transaction;
        configureTransactionView();
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
        Fonts.applyPrimaryBoldFont(mMemo, 12);
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
