package com.moneydesktop.finance.shared;

import android.annotation.TargetApi;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LabelEditText;

import org.apache.commons.lang.WordUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TransactionDetailBaseFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    protected SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM.dd.yyyy");

    protected TransactionsDao mDao;
    protected Transactions mTransaction;

    protected TextView mAccountName, mBankName;
    protected LabelEditText mPayee, mMemo, mDate, mTags, mAmount, mStatement, mCategory;
    protected ImageView mBankIcon;
    protected ToggleButton mBusiness, mPersonal, mCleared, mFlagged;
    
    protected Animation mShake;
    
    public Transactions getTransaction() {
        return mTransaction;
    }

    public void setTransaction(Transactions mTransaction) {
        this.mTransaction = mTransaction;
    }
    
    protected void initialize() {
        
        setupAnimations();
        setupViews();
        
        fixDottedLine();
        applyFonts();
        configureListeners();
    }
    
    protected void setupViews() {
        
        mAccountName = (TextView) mRoot.findViewById(R.id.account_name);
        mBankName = (TextView) mRoot.findViewById(R.id.bank_name);
        
        mCategory = (LabelEditText) mRoot.findViewById(R.id.category);
        mTags = (LabelEditText) mRoot.findViewById(R.id.tags);
        mPayee = (LabelEditText) mRoot.findViewById(R.id.payee);
        mAmount = (LabelEditText) mRoot.findViewById(R.id.amount);
        mDate = (LabelEditText) mRoot.findViewById(R.id.date);
        mMemo = (LabelEditText) mRoot.findViewById(R.id.memo);
        mStatement = (LabelEditText) mRoot.findViewById(R.id.stmt);
        
        mBankIcon = (ImageView) mRoot.findViewById(R.id.bank_image);
        
        mBusiness = (ToggleButton) mRoot.findViewById(R.id.flag_b);
        mPersonal = (ToggleButton) mRoot.findViewById(R.id.flag_p);
        mCleared = (ToggleButton) mRoot.findViewById(R.id.flag_c);
        mFlagged = (ToggleButton) mRoot.findViewById(R.id.flag);
        
        // Currently we are read-only, disable all input fields
        mAmount.setFocusable(false);
        mDate.setFocusable(false);
        mStatement.setFocusable(false);
        mTags.setFocusable(false);
        mCategory.setFocusable(false);
        
        mCleared.setEnabled(false);
    }
    
    protected void setupAnimations() {
        mShake = AnimationFactory.createShakeAnimation(mActivity);
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
        Fonts.applyPrimarySemiBoldFont(mTags, 12);
        Fonts.applyPrimarySemiBoldFont(mMemo, 24);
        Fonts.applyPrimaryBoldFont(mStatement, 12);
        
        // labels
        mPayee.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mMemo.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mAmount.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mDate.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mCategory.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mTags.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mStatement.setLabelFont(Fonts.getFont(Fonts.PRIMARY_BOLD));
    }
    
    protected void configureListeners() {
        
        mAmount.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mAmount.startAnimation(mShake);
            }
        });
        
        mDate.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mDate.startAnimation(mShake);
            }
        });
        
        mStatement.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mStatement.startAnimation(mShake);
            }
        });
        
        mBusiness.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPersonal.setChecked(!isChecked);
                mTransaction.setIsBusiness(isChecked);
                mTransaction.updateSingle();
            }
        });
        
        mPersonal.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBusiness.setChecked(!isChecked);
                mTransaction.setIsBusiness(!isChecked);
                mTransaction.updateSingle();
            }
        });
        
        mFlagged.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTransaction.setIsFlagged(isChecked);
                mTransaction.updateSingle();
            }
        });
        
        mPayee.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE && mTransaction != null && (mTransaction.getTitle() == null || !mTransaction.getTitle().equals(mPayee.getText().toString()))) {
                    
                    mTransaction.setTitle(mPayee.getText().toString());
                    mTransaction.updateSingle();
                    finishEditing(v);
                    
                    return true;
                }
                
                finishEditing(v);
                
                return false;
            }
        });
        
        mMemo.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE && mTransaction != null && (mTransaction.getMemo() == null || !mTransaction.getMemo().equals(mMemo.getText().toString()))) {
                    
                    mTransaction.setMemo(mMemo.getText().toString());
                    mTransaction.updateSingle();
                    finishEditing(v);
                    
                    return true;
                }
                
                finishEditing(v);
                
                return false;
            }
        });
    }
    
    private void finishEditing(View v) {
        v.clearFocus();
        UiUtils.hideKeyboard(getActivity(), v);
    }
    
    protected void loadTransaction() {
        
        long guid = getArguments().getLong(Constant.KEY_GUID);
        
        if (guid == -1) {
            getFragmentManager().popBackStack();
            return;
        }

        mDao = (TransactionsDao) DataController.getDao(Transactions.class);
        mTransaction = mDao.load(guid);
    }

    public void configureTransactionView() {

        if (mTransaction == null) return;
        
        if (mTransaction.getBankAccount() != null) {
            BankLogoManager.getBankImage(mBankIcon, mTransaction.getBankAccount().getInstitutionId());
        }
            
        mAccountName.setText(mTransaction.getBankAccount().getAccountName());
        mBankName.setText(mTransaction.getBankAccount().getBank().getBankName());
        mCategory.setText(mTransaction.getCategory().getCategoryName());
        mTags.setText(mTransaction.buildTagString());
        
        mPayee.setText(WordUtils.capitalize(mTransaction.getTitle().toLowerCase()));
        mAmount.setText(mFormatter.format(mTransaction.normalizedAmount()));
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
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
    
    protected void refreshTag() {
        
        mTags.setText(mTransaction.buildTagString());
    }
    
    public void updateTransactionCategory(long categoryId) {
        
        if (mTransaction == null) {
            return;
        }

        mTransaction.setCategoryId(categoryId);
        mTransaction.updateSingle();
        
        configureTransactionView();
    }
    
    protected void deleteTag(Tag tag) {
        
        Tag.deleteTag(tag);
    }

}
