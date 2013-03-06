package com.moneydesktop.finance.handset.adapter;

import java.sql.Array;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.util.Fonts;

public class BankOptionsAdapter extends ArrayAdapter<String> {


	private Context mContext;
    private Bank mBank;
    private int mLayoutId;
    private String[] mOptions;
    
	public BankOptionsAdapter(Context context, int layoutResourceId, Bank bank, String[] options) {
		super(context, layoutResourceId);
	
		mContext = context;
		mLayoutId = layoutResourceId;
		mBank = bank;
		mOptions = options;
	}
    
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BankOptionsHolder holder = new BankOptionsHolder();
		
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		convertView = inflater.inflate(mLayoutId, parent, false);
		
		holder.container = (LinearLayout)convertView.findViewById(R.id.bank_options_container);
		holder.optionImg = (TextView)convertView.findViewById(R.id.txt_image);
		holder.txtTitle = (TextView)convertView.findViewById(R.id.handset_bank_options_name_list_item);			
		
		
		//if the bank is broken and we're looking at the first row after the header then highlight the row
		if (mOptions[position].equals(mContext.getString(R.string.fix_bank_more_info_needed))) {
			holder.container.setBackgroundColor(mContext.getResources().getColor(R.color.budgetColorYellowBackground));
			holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.white));
			holder.optionImg.setText(mContext.getString(R.string.icon_alert));
			holder.optionImg.setTextColor(mContext.getResources().getColor(R.color.white));
			holder.txtTitle.setText(mOptions[position]);
		} else if (mOptions[position].equals(mContext.getString(R.string.fix_bank_somethings_wrong))) {
			holder.container.setBackgroundColor(mContext.getResources().getColor(R.color.budgetColorRedBackground));
			holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.white));
			holder.optionImg.setText(mContext.getString(R.string.icon_alert));
			holder.optionImg.setTextColor(mContext.getResources().getColor(R.color.white));
			holder.txtTitle.setText(mOptions[position]);
		} else {
			holder.txtTitle.setText(mOptions[position]);
			holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.gray7));
			
			if (mOptions[position].equals(mContext.getString(R.string.credentials))) {
				holder.optionImg.setText(mContext.getString(R.string.icon_lock));
			} else if (mOptions[position].equals(mContext.getString(R.string.delete_institution))) {
				holder.optionImg.setText(mContext.getString(R.string.icon_trash));
			}
		}
		
		
		Fonts.applyGlyphFont(holder.optionImg, 18);
		Fonts.applyPrimaryBoldFont(holder.txtTitle, 12);
			
		
		return convertView;
	}
	
	
    @Override
	public int getCount() {
		return mOptions.length;
	}


	static class BankOptionsHolder
    {
		LinearLayout container;
    	TextView optionImg;
        TextView txtTitle;
    }
    
}
