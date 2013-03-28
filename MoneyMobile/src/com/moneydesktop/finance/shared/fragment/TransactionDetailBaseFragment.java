package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.*;
import com.moneydesktop.finance.views.LabelEditCurrency;
import com.moneydesktop.finance.views.LabelEditText;
import de.greenrobot.event.EventBus;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class TransactionDetailBaseFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private DecimalFormat mFormatter = new DecimalFormat("#,##0.00;-#,##0.00");
    protected SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM.dd.yyyy", Locale.US);

    protected TransactionsDao mDao;
    protected Transactions mTransaction;

    protected TextView mAccountName, mBankName, mMarkersLabel;
    private EditText mDummy;
    protected LabelEditText mPayee, mMemo, mDate, mTags, mStatement, mCategory;
    protected LabelEditCurrency mAmount;
    protected ImageView mBankIcon;
    protected ToggleButton mBusiness, mPersonal, mCleared, mFlagged;
    
    private Handler mHandler;
    
    protected Animation mShake;

    private OnClickListener mShakeClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            v.startAnimation(mShake);
        }
    };

    private OnClickListener mDateClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            selectDate(v);
        }
    };
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        mHandler = new Handler();
        
        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

        configureTransactionView(false);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
    public void onEvent(SyncEvent event) {

    	if (event.isFinished()) {
    		
    		mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
		            configureTransactionView(true);
				}
			}, 300);
    	}
    }
    
    protected void initialize() {
    	
        setupAnimations();
        setupViews();
        
        applyFonts();
        configureListeners();
    }
    
    protected void setupViews() {
        
        mAccountName = (TextView) mRoot.findViewById(R.id.account_name);
        mBankName = (TextView) mRoot.findViewById(R.id.bank_name);
        mMarkersLabel = (TextView) mRoot.findViewById(R.id.markers_label);
        
        mDummy = (EditText) mRoot.findViewById(R.id.dummy);
        mCategory = (LabelEditText) mRoot.findViewById(R.id.category);
        mTags = (LabelEditText) mRoot.findViewById(R.id.tags);
        mPayee = (LabelEditText) mRoot.findViewById(R.id.payee);
        mAmount = (LabelEditCurrency) mRoot.findViewById(R.id.amount);
        mDate = (LabelEditText) mRoot.findViewById(R.id.date);
        mMemo = (LabelEditText) mRoot.findViewById(R.id.memo);
        mStatement = (LabelEditText) mRoot.findViewById(R.id.stmt);
        
        mBankIcon = (ImageView) mRoot.findViewById(R.id.bank_image);
        
        mBusiness = (ToggleButton) mRoot.findViewById(R.id.flag_b);
        mPersonal = (ToggleButton) mRoot.findViewById(R.id.flag_p);
        mCleared = (ToggleButton) mRoot.findViewById(R.id.flag_c);
        mFlagged = (ToggleButton) mRoot.findViewById(R.id.flag);
        
        // Currently we are read-only, disable all input fields
        mDate.setFocusable(false);
        mStatement.setFocusable(false);
        mTags.setFocusable(false);
        mCategory.setFocusable(false);
    }
    
    protected void setupAnimations() {
        mShake = AnimationFactory.createShakeAnimation(ApplicationContext.getContext());
    }
    
    protected void applyFonts() {
        
        Fonts.applyPrimaryFont(mAccountName, 12);
        Fonts.applySecondaryItalicFont(mBankName, 10);
        Fonts.applyPrimarySemiBoldFont(mPayee, 16);
        Fonts.applyPrimaryBoldFont(mAmount, 28);
        Fonts.applyPrimarySemiBoldFont(mDate, 16);
        Fonts.applyPrimarySemiBoldFont(mCategory, 16);
        Fonts.applyPrimarySemiBoldFont(mTags, 16);
        Fonts.applyPrimaryBoldFont(mStatement, 8);
        Fonts.applyPrimarySemiBoldFont(mMemo, 16);
        Fonts.applySecondaryItalicFont(mMarkersLabel, 8);
        
        // labels
        mPayee.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mMemo.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mAmount.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mDate.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mCategory.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mTags.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mStatement.setLabelFont(Fonts.getFont(Fonts.SECONDARY_ITALIC));
    }
    
    protected void configureListeners() {
        
    	mCategory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showFragment(CategoriesFragment.newInstance(mTransaction.getId()));
			}
		});
    	
    	mTags.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showFragment(TagsFragment.newInstance(mTransaction.getBusinessObjectId()));
			}
		});
        
        mStatement.setOnClickListener(mShakeClick);
        
        mBusiness.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPersonal.setChecked(!isChecked);
            }
        });
        
        mBusiness.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mTransaction == null) return;

                mTransaction.setIsBusiness(mBusiness.isChecked());
                mTransaction.updateSingle();
            }
        });
        
        mPersonal.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBusiness.setChecked(!isChecked);
            }
        });
        
        mPersonal.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mTransaction == null) return;

                mTransaction.setIsBusiness(!mPersonal.isChecked());
                mTransaction.updateSingle();
            }
        });
        
        mFlagged.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mTransaction == null) return;

                mTransaction.setIsFlagged(isChecked);
                mTransaction.updateSingle();
            }
        });

        mCleared.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mTransaction == null) return;

                mTransaction.setIsCleared(isChecked);
                mTransaction.updateSingle();
            }
        });
        
        mAmount.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String value = mAmount.getText().toString().substring(1);

                if (actionId == EditorInfo.IME_ACTION_DONE && mTransaction != null) {

                    try {
                        mTransaction.setAmount(mFormatter.parse(value).doubleValue());
                        mTransaction.updateSingle();
                    } catch (ParseException ex) {
                        Log.e(TAG, "Could not parse number", ex);
                    }

                    finishEditing(v);

                    return true;
                }

                finishEditing(v);

                return false;
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
    
    protected void showFragment(BaseFragment fragment) {
        mActivity.pushFragment(R.id.fragment, fragment);
    }
    
    private void finishEditing(View v) {
        v.clearFocus();
        mDummy.requestFocus();
        UiUtils.hideKeyboard(getActivity(), v);
    }
    
    protected void loadTransaction() {
    	
        long guid = getArguments().getLong(Constant.KEY_GUID, -1);
        
        if (guid == -1) {
            return;
        }

        mDao = (TransactionsDao) DataController.getDao(Transactions.class);
        mTransaction = mDao.load(guid);
        
        mTransaction.getBankAccount().getBank();
        mTransaction.getCategory();
    }

    public void configureTransactionView(boolean isUpdate) {

        if (mTransaction == null) return;

        boolean isManual = (mTransaction.getIsManual() != null && mTransaction.getIsManual());

        mCleared.setEnabled(isManual);
        mAmount.setFocusable(isManual);
        mAmount.setFocusableInTouchMode(isManual);
        mAmount.setOnClickListener(isManual ? null : mShakeClick);
        mDate.setOnClickListener(isManual ? mDateClick : mShakeClick);

        mBusiness.setChecked(mTransaction.getIsBusiness() != null && mTransaction.getIsBusiness());
        mPersonal.setChecked(!mBusiness.isChecked());
        mCleared.setChecked(mTransaction.getIsCleared() != null && mTransaction.getIsCleared());
        boolean isFlagged = (mTransaction.getIsFlagged() != null && mTransaction.getIsFlagged());
        mFlagged.setChecked(isFlagged);

        boolean income = mTransaction.getTransactionType() == 1;
        String amount = (income ? "(" : "") + mFormatter.format(mTransaction.normalizedAmount()) + (income ? ")" : "");

        if (isUpdate) {

            mAmount.setAnimatedText(amount);
        	mCategory.setAnimatedText(mTransaction.getCategory().getCategoryName());
	        mTransaction.buildTagString(mTags, true);
	        
	        mPayee.setAnimatedText(mTransaction.getCapitalizedTitle());
	        mMemo.setAnimatedText(mTransaction.getMemo());
	        
	        return;
	        
        } else {
        	
	        mCategory.setText(mTransaction.getCategory().getCategoryName());
	        mTransaction.buildTagString(mTags);
	        
	        mPayee.setText(mTransaction.getCapitalizedTitle());
	        mAmount.setText(amount);
	        mDate.setText(mDateFormatter.format(mTransaction.getDate()));
	        mMemo.setText(mTransaction.getMemo());
	        mStatement.setText(mTransaction.getOriginalTitle());
        }
        
        if (mTransaction.getBankAccount() != null) {
            BankLogoManager.getBankImage(mBankIcon, mTransaction.getBankAccount().getInstitutionId());
        }

        mAccountName.setText(mTransaction.getBankAccount().getAccountName());
        mBankName.setText(mTransaction.getBankAccount().getBank().getBankName());
    }

    protected void emailTransaction(View transactionView) {

        Bitmap image = UiUtils.convertViewToBitmap(transactionView);
        String path = FileIO.saveBitmap(getActivity(), image, mTransaction.getTransactionId());

        EmailUtils.sendEmail(getActivity(), getString(R.string.email_transaction_subject), "", path);
    }

    protected void confirmDeleteTransaction() {

        DialogUtils.alertDialog(getString(R.string.title_delete_transaction).toUpperCase(), getString(R.string.message_delete_transaction), getString(R.string.label_yes).toUpperCase(), getString(R.string.label_no).toUpperCase(), mActivity, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case -1:
                        deleteTransaction();
                        break;
                }

                DialogUtils.dismissAlert();
            }
        });
    }

    protected void deleteTransaction() {

        mTransaction.softDeleteSingle();
        onBackPressed();
    }

    protected abstract void selectDate(View view);
}
