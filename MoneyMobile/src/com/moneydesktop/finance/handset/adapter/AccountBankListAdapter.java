package com.moneydesktop.finance.handset.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.BankRefreshIcon;

public class AccountBankListAdapter extends BaseAdapter {

	private List<Bank> mBanks;
	private Activity mActivity;
	private boolean mShouldForceToUpdate = false;
	private List<Bank> mBanksToUpdate;
   	
	public AccountBankListAdapter(Activity activity, List<Bank> banks) {
		super();
		mActivity = activity;
		mBanks = banks;
		mBanksToUpdate = new ArrayList<Bank>();
	}

	@Override
	public int getCount() {
		return mBanks.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == 0) {
			return addBankSymbolToContainer();
		} else {
			return populateBankContainer(mBanks.get(position - 1)); // -1 is to account for the manually added view of "addBankSymbolToContainer"
		}
	}

	private View populateBankContainer(final Bank bank) {
		
		LayoutInflater layoutInflater = mActivity.getLayoutInflater();
		final View bankView = layoutInflater.inflate(R.layout.handset_account_types_bank_item, null);
		ImageView bankImage = (ImageView)bankView.findViewById(R.id.handset_account_types_bank_image);
		ImageView bankStatus = (ImageView)bankView.findViewById(R.id.handset_account_types_bank_status);
		BankRefreshIcon bankRefreshIcon = (BankRefreshIcon) bankView.findViewById(R.id.handset_account_types_bank_status_update);
		
		Fonts.applyGlyphFont(bankRefreshIcon, 20);
		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)UiUtils.getScaledPixels(mActivity, 80), (int)UiUtils.getScaledPixels(mActivity, 80));
		bankImage.setLayoutParams(layoutParams);
		bankImage.setPadding(10, 10, 10, 10);
		bankStatus.setLayoutParams(layoutParams);
		bankStatus.setPadding(10, 10, 10, 10);
		bankRefreshIcon.setLayoutParams(layoutParams);
		bankRefreshIcon.setPadding(10, 10, 10, 10);
		
		
		setBanner(bank, bankStatus, bankRefreshIcon);
		
        String logoId = bank.getBankId();
        
        if (bank.getInstitution() != null) {
            logoId = bank.getInstitution().getInstitutionId();
        }
		
        BankLogoManager.getBankImage(bankImage, logoId);

		return bankView;
	}
	
	private TextView addBankSymbolToContainer() {
		
		LayoutInflater layoutInflater = mActivity.getLayoutInflater();
		final View addBankView = layoutInflater.inflate(R.layout.handset_account_types_add_bank_item, null);
		
		TextView addBank = (TextView)addBankView.findViewById(R.id.handset_account_types_add_bank_icon);	
	//	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)UiUtils.getScaledPixels(mActivity, 80), (int)UiUtils.getScaledPixels(mActivity, 80));
	
        Fonts.applyGlyphFont(addBank, 35);
       // addBank.setLayoutParams(layoutParams);
        int tenDIP = (int) UiUtils.convertDpToPixel(10, mActivity);
       addBank.setPadding(tenDIP, tenDIP*(int)(1.5), 0, tenDIP);
        
   
        return addBank;
	}
	
    private void setBanner(final Bank bank, final ImageView status, final TextView refreshStatus) { 
    	status.setVisibility(View.VISIBLE);

        if (mActivity != null) {
        	if (mBanksToUpdate.size() > 0) {

    	        List<Bank> tempList = new ArrayList<Bank>();
    	        tempList = Arrays.asList(new Bank[mBanksToUpdate.size()]);  
    	        Collections.copy(tempList, mBanksToUpdate);
        		
        		for (Bank bankIterator : tempList) {        			
        			if (bankIterator.getBankName().equals(bank.getBankName())) {
	        			mShouldForceToUpdate = true;
	        			mBanksToUpdate.remove(bankIterator);
        			}
        		
        		} 
    		} 
        	
        	if (mShouldForceToUpdate) {
        		applyUpdatingImage(status, refreshStatus);
        	} else {
	        	if (bank.getProcessStatus() == null) {
	        		applyUpdatingImage(status, refreshStatus);
	        		return;
	        	}
	            if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_SUCCEEDED.index()) {
	            	refreshStatus.setVisibility(View.GONE);
	                status.setVisibility(View.GONE);
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_PENDING.index()) {
	            	applyUpdatingImage(status, refreshStatus);
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_MFA.index()) {
	            	refreshStatus.setVisibility(View.GONE);
	                status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.handset_accounts_mfa));
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_LOGIN_FAILED.index()) {
	            	refreshStatus.setVisibility(View.GONE);
	                status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.handset_accounts_broken));
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_UPDATE_REQUIRED.index()) {
	            	refreshStatus.setVisibility(View.GONE);
	                status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.handset_accounts_mfa));
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_EXCEPTION.index()) {
	            	refreshStatus.setVisibility(View.GONE);
	                status.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.handset_accounts_broken));
	                
	            } else if (bank.getProcessStatus().intValue() == BankRefreshStatus.STATUS_PROCESSING.index()) {
	            	applyUpdatingImage(status, refreshStatus);
	            }
        	}
        }
    }
    
	private void applyUpdatingImage(final ImageView status, TextView bankRefreshIcon) {
		status.setVisibility(View.GONE);
		bankRefreshIcon.setVisibility(View.VISIBLE);
	}

	public void setAllToUpdate(boolean shouldForceToUpdate) {
		mShouldForceToUpdate = shouldForceToUpdate;
	}

	public void setBankToUpdate(Bank bank) {
		if (!mBanksToUpdate.contains(bank)) {
			mBanksToUpdate.add(bank);			
		}
		
	}
	
}
