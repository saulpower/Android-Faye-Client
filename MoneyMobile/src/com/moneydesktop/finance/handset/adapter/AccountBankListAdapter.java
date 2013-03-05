package com.moneydesktop.finance.handset.adapter;

import java.util.List;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.handset.fragment.AddAccountHandsetFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.BankRefreshIcon;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AccountBankListAdapter extends BaseAdapter {

	private List<Bank> mBanks;
	private Activity mActivity;
	
	public AccountBankListAdapter(Activity activity, List<Bank> banks) {
		super();
		mActivity = activity;
		mBanks = banks;
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
	
}
