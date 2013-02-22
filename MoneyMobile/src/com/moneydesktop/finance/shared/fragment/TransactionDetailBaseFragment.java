package com.moneydesktop.finance.shared.fragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.animation.AnimationFactory;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.LabelEditText;

import de.greenrobot.event.EventBus;

public abstract class TransactionDetailBaseFragment extends BaseFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    protected SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM.dd.yyyy", Locale.US);

    protected TransactionsDao mDao;
    protected Transactions mTransaction;

    protected TextView mAccountName, mBankName, mMarkersLabel;
    private EditText mDummy;
    protected LabelEditText mPayee, mMemo, mDate, mTags, mAmount, mStatement, mCategory;
    protected ImageView mBankIcon;
    protected ToggleButton mBusiness, mPersonal, mCleared, mFlagged;
    
    private Handler mHandler;
    
    protected Animation mShake;
    
    public Transactions getTransaction() {
        return mTransaction;
    }

    public void setTransaction(Transactions mTransaction) {
        this.mTransaction = mTransaction;
    }
    
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
        mShake = AnimationFactory.createShakeAnimation(ApplicationContext.getContext());
    }
    
    protected void applyFonts() {
        
        Fonts.applyPrimaryFont(mAccountName, 8);
        Fonts.applySecondaryItalicFont(mBankName, 6);
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
            }
        });
        
        mBusiness.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

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

                mTransaction.setIsBusiness(!mPersonal.isChecked());
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
    
    protected void showFragment(BaseFragment fragment) {
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
		ft.replace(R.id.fragment, fragment);
		ft.addToBackStack(null);
		ft.commit();
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
        
        mBusiness.setChecked(mTransaction.getIsBusiness());
        mPersonal.setChecked(!mTransaction.getIsBusiness());
        mCleared.setChecked(mTransaction.getIsCleared());
        mFlagged.setChecked(mTransaction.getIsFlagged());
        
        if (isUpdate) {

        	mCategory.setAnimatedText(mTransaction.getCategory().getCategoryName());
	        mTransaction.buildTagString(mTags, true);
	        
	        mPayee.setAnimatedText(mTransaction.getCapitalizedTitle());
	        mMemo.setAnimatedText(mTransaction.getMemo());
	        
	        return;
	        
        } else {
        	
        	boolean income = mTransaction.getTransactionType() == 1;
        	
	        mCategory.setText(mTransaction.getCategory().getCategoryName());
	        mTransaction.buildTagString(mTags);
	        
	        mPayee.setText(mTransaction.getCapitalizedTitle());
	        mAmount.setText((income ? "(" : "") + mFormatter.format(mTransaction.normalizedAmount()) + (income ? ")" : ""));
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
    
    public void updateTransactionCategory(long categoryId) {
        
        if (mTransaction == null) {
            return;
        }

        mTransaction.setCategoryId(categoryId);
        mTransaction.updateSingle();
        
        configureTransactionView(false);
    }
    
    protected void deleteTag(Tag tag) {
        
        Tag.deleteTag(tag);
    }

}
