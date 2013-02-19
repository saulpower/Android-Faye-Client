package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.TransactionDetailController;
import com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.fragment.TransactionsDetailTabletFragment.onBackPressedListener;

@TargetApi(11)
public abstract class ParentTransactionFragment extends BaseFragment implements onBackPressedListener, ParentTransactionInterface {
    
    private TransactionDetailController mBase;
    
    public void setupView() {

        ImageView fakeCell = (ImageView) mRoot.findViewById(R.id.cell);
        FrameLayout detail = (FrameLayout) mRoot.findViewById(R.id.detail_fragment);
        
        mBase = new TransactionDetailController(fakeCell, detail, 0);
        mBase.setDetailFragment(TransactionsDetailTabletFragment.newInstance());
        mBase.getDetailFragment().setListener(this);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.detail_fragment, mBase.getDetailFragment());
        ft.commit();
    }
    
    protected boolean mAnimating = false;
    
    @Override
    public void showTransactionDetails(View view, int offset, Transactions transaction) {
        mBase.showTransactionDetails(view, offset, transaction);
    }

    @Override
    public void setDetailFragment(TransactionsDetailTabletFragment fragment) {
        mBase.setDetailFragment(fragment);
    }

    @Override
    public TransactionsDetailTabletFragment getDetailFragment() {
        return mBase.getDetailFragment();
    }

    @Override
    public void onFragmentBackPressed() {
        mBase.configureDetailView();
    }
    
    @Override
    public void parentOnActivityResult(int requestCode, int resultCode, Intent data) {    
        mBase.parentOnActivityResult(requestCode, resultCode, data);
    }
}
