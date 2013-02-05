package com.moneydesktop.finance.handset.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Bank;

public class BankOptionsAdapter implements ListAdapter {

    private Context mContext;
    private Bank mBank;
    
    public BankOptionsAdapter (Context context, Bank bank) {
        mContext = context;
        mBank = bank;
    }
    
    @Override
    public int getCount() {
        return mContext.getResources().getStringArray(R.array.account_types_bank_options).length;
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
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

}
