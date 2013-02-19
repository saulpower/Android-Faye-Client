package com.moneydesktop.finance.handset.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.SlideFrom;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.handset.adapter.AccountTypesHandsetAdapter;
import com.moneydesktop.finance.handset.adapter.BankOptionsAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingView;
import com.moneydesktop.finance.views.UltimateListView;

public class AccountTypesHandsetFragment extends BaseFragment {
    
    private LinearLayout mBanksContainer;
    private UltimateListView mAccountsListView;
    private View mBankOptionsView;
    private SlidingView mSliderView;
    
    public static AccountTypesHandsetFragment getInstance() {

        AccountTypesHandsetFragment fragment = new AccountTypesHandsetFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.account_types_title).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        if (mSliderView != null) {
            mSliderView.dismiss();
            return true;
        }
        return false;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.handset_account_types, null);
        setupViews();
        
        return mRoot;
    }
    
    private void setupViews() {
        
        mBanksContainer = (LinearLayout) mRoot.findViewById(R.id.account_types_bank_list_handset);
        mAccountsListView = (UltimateListView) mRoot.findViewById(R.id.handset_account_types_list);
       
        AccountTypeDao accountDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
        
        List<AccountType> accountTypes = accountDAO.loadAll();
        List<AccountType> accountTypesFiltered = new ArrayList<AccountType>();
             
        for (AccountType type : accountTypes) { 
            if (!type.getBankAccounts().isEmpty()) {
                accountTypesFiltered.add(type);
            }
        }

        loadBank();
        
        mAccountsListView.setDividerHeight(0);
        mAccountsListView.setDivider(null);
        mAccountsListView.setChildDivider(null);
        mAccountsListView.setAdapter(new AccountTypesHandsetAdapter(mActivity, accountTypesFiltered, mAccountsListView));
        
        for (int i = 0; i < accountTypesFiltered.size(); i++) {
            mAccountsListView.expandGroup(i);
        }
        
        mAccountsListView.setSelectedChild(0, 0, true);
        mAccountsListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
    }    
    
    private void loadBank() {
        List<Bank> banksList = ApplicationContext.getDaoSession().getBankDao().loadAll();
        
        TextView addBank = new TextView(getActivity());
        
        addBank.setText(getString(R.string.icon_add));
        addBank.setTextColor(Color.WHITE);
        Fonts.applyGlyphFont(addBank, 35);
        addBank.setPadding(20, 10, 10, 10);
        
        mBanksContainer.addView(addBank);
        
        for (final Bank bank : banksList) {
            final ImageView bankImage = new ImageView(getActivity());
            
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)UiUtils.getScaledPixels(getActivity(), 80), (int)UiUtils.getScaledPixels(getActivity(), 80));
            bankImage.setLayoutParams(layoutParams);
            bankImage.setPadding(10, 10, 10, 10);
            
            if (bank.getBankAccounts().size() > 0) {
            	BankLogoManager.getBankImage(bankImage, bank.getBankAccounts().get(0).getInstitutionId());
            }
            
            bankImage.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(final View v) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    mBankOptionsView = inflater.inflate(R.layout.account_types_handset_bank_options, null);
                    
                    ListView bankOptionsList = (ListView)mBankOptionsView.findViewById(R.id.account_type_handset_options);
                    bankOptionsList.setAdapter(new BankOptionsAdapter(mActivity, bank));
                    
                    if (mSliderView != null) {
                        
                        //this will make it so we don't end up in a loop of sliding views
                        if (mSliderView.getSelectedView() != v) {
                            AnimationListener listener = new AnimationListener() {
                                
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    
                                }
                                
                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    
                                }
                                
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    mSliderView = new SlidingView(getActivity(), 0, v.getBottom() + (int)UiUtils.convertDpToPixel(20, getActivity()), (ViewGroup)mRoot, mBankOptionsView, SlideFrom.BOTTOM, v);                                    
                                }
                            };
                            mSliderView.dismiss(listener);
                            
                        } else {
                            mSliderView.dismiss();
                            mSliderView = null;
                        }
                    } else {
                        mSliderView = new SlidingView(getActivity(), 0, mBanksContainer.getBottom() + (int)UiUtils.convertDpToPixel(20, getActivity()), (ViewGroup)mRoot, mBankOptionsView, SlideFrom.BOTTOM, v);
                    }
                }
            });
            
            mBanksContainer.addView(bankImage);
        }
    }

	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_TYPES;
	}

}