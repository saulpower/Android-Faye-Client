package com.moneydesktop.finance.shared;

import android.annotation.TargetApi;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.util.Fonts;

import org.apache.commons.lang.WordUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TransactionDetailBaseFragment extends BaseFragment {

    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    protected SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM.dd.yyyy");
    
    protected TransactionsDao mDao;
    protected Transactions mTransaction;

    protected TextView mAccountName, mBankName, mCategory, mTags;
    protected EditText mPayee, mAmount, mDate, mMemo, mStatement;
    protected ImageView mBankIcon;
    protected ToggleButton mBusiness, mPersonal, mCleared, mFlagged;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.mActivity.onFragmentAttached(this);
    }
    
    protected void setupViews() {
        
        mAccountName = (TextView) mRoot.findViewById(R.id.account_name);
        mBankName = (TextView) mRoot.findViewById(R.id.bank_name);
        mCategory = (TextView) mRoot.findViewById(R.id.category_name);
        mTags = (TextView) mRoot.findViewById(R.id.tags);
        
        mPayee = (EditText) mRoot.findViewById(R.id.payee_name);
        mAmount = (EditText) mRoot.findViewById(R.id.amount);
        mDate = (EditText) mRoot.findViewById(R.id.date);
        mMemo = (EditText) mRoot.findViewById(R.id.memo);
        mStatement = (EditText) mRoot.findViewById(R.id.stmt);
        
        mBankIcon = (ImageView) mRoot.findViewById(R.id.bank_image);
        
        mBusiness = (ToggleButton) mRoot.findViewById(R.id.flag_b);
        mPersonal = (ToggleButton) mRoot.findViewById(R.id.flag_p);
        mCleared = (ToggleButton) mRoot.findViewById(R.id.flag_c);
        mFlagged = (ToggleButton) mRoot.findViewById(R.id.flag);
        
        // Currently we are read-only, disable all input fields
        mPayee.setEnabled(false);
        mAmount.setEnabled(false);
        mDate.setEnabled(false);
        mMemo.setEnabled(false);
        mStatement.setEnabled(false);
        
        mBusiness.setEnabled(false);
        mPersonal.setEnabled(false);
        mCleared.setEnabled(false);
        mFlagged.setEnabled(false);
        
        fixDottedLine();
        applyFonts();
        configureListeners();
    }
    
    @TargetApi(11)
    private void fixDottedLine() {

        LinearLayout container = (LinearLayout) mRoot.findViewById(R.id.root);
        
        if (container == null) {
            return;
        }
        
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            
            for (int i = 0; i < container.getChildCount(); i++) {
                
                View v = container.getChildAt(i);
                
                if (v.getTag() != null && v.getTag().equals("dotted"))
                    v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }   
        }
    }
    
    protected void applyFonts() {
        
        Fonts.applyPrimaryFont(mAccountName, 12);
        Fonts.applySecondaryItalicFont(mBankName, 10);
        Fonts.applyPrimarySemiBoldFont(mPayee, 24);
        Fonts.applyPrimaryBoldFont(mAmount, 48);
        Fonts.applyPrimarySemiBoldFont(mDate, 24);
        Fonts.applyPrimarySemiBoldFont(mCategory, 24);
        Fonts.applyPrimarySemiBoldFont(mTags, 24);
        Fonts.applyPrimarySemiBoldFont(mMemo, 24);
        Fonts.applyPrimaryBoldFont(mStatement, 12);
        
        // labels
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.payee), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.amount_label), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.date_label), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.category), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.tags_label), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.markers_label), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.memo_label), 12);
        Fonts.applySecondaryItalicFont((TextView) mRoot.findViewById(R.id.stmt_label), 12);
    }
    
    private void configureListeners() {
        
        mBusiness.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                
                mPersonal.setChecked(!mBusiness.isChecked());
            }
        });
        
        mPersonal.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                
                mBusiness.setChecked(!mPersonal.isChecked());
            }
        });
    }
    
    protected void getTransaction() {
        
        long guid = getArguments().getLong(Constant.KEY_GUID);
        
        if (guid == -1) {
            getFragmentManager().popBackStack();
            return;
        }

        mDao = (TransactionsDao) DataController.getDao(Transactions.class);
        mTransaction = mDao.load(guid);
    }

    protected void loadTransaction() {
        
        if (mTransaction == null) {
            return;
        }
        
        if (mTransaction.getBankAccount() != null) {
            BankLogoManager.getBankImage(mBankIcon, mTransaction.getBankAccount().getInstitutionId());
        }
            
        mAccountName.setText(mTransaction.getBankAccount().getAccountName());
        mBankName.setText(mTransaction.getBankAccount().getBank().getBankName());
        mCategory.setText(mTransaction.getCategory().getCategoryName());
        mTags.setText(mTransaction.getTagString());
        
        double value = mTransaction.getRawAmount();
        
        if (mTransaction.isIncome()) {
            value = Math.abs(value);
        }
        
        mPayee.setText(WordUtils.capitalize(mTransaction.getTitle().toLowerCase()));
        mAmount.setText(mFormatter.format(value));
        mDate.setText(mDateFormatter.format(mTransaction.getDate()));
        mMemo.setText(mTransaction.getMemo());
        mStatement.setText(mTransaction.getOriginalTitle());
        
        mBusiness.setChecked(mTransaction.getIsBusiness());
        mPersonal.setChecked(!mTransaction.getIsBusiness());
        mCleared.setChecked(mTransaction.getIsCleared());
        mFlagged.setChecked(mTransaction.getIsFlagged());
    }
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_activity_transaction);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
