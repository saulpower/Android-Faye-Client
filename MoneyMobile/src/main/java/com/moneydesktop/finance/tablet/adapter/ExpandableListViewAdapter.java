package main.java.com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.database.AccountType;
import main.java.com.moneydesktop.finance.database.BankAccount;
import main.java.com.moneydesktop.finance.views.AccountTypeChildView;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kentandersen
 * Date: 4/1/13
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<AccountType> mAccountTypes;

    public ExpandableListViewAdapter(Context context, List<AccountType> accountTypes) {
        mContext = context;
        mAccountTypes = accountTypes;
    }

    @Override
    public int getGroupCount() {
        return mAccountTypes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder groupViewHolder;

        if (convertView == null) {

            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.account_type_group, parent, false);

            groupViewHolder = new GroupViewHolder();
            groupViewHolder.accountTypeName = (TextView)convertView.findViewById(R.id.account_type_group_name);
            groupViewHolder.accountTypeSum = (TextView)convertView.findViewById(R.id.account_type_group_sum);
            groupViewHolder.accountTypeIndicator = (TextView)convertView.findViewById(R.id.account_type_group_indicator);

            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        populateGroupView(groupViewHolder, mAccountTypes.get(groupPosition), isExpanded);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        AccountType accountType = mAccountTypes.get(groupPosition);
        accountType.resetBankAccounts(); //pulls fresh from the DB
        AccountTypeChildView accountTypeChildView = new AccountTypeChildView(mContext, accountType.getBankAccounts(), parent);

        return accountTypeChildView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private void populateGroupView(GroupViewHolder groupViewHolder, AccountType accountType, boolean expanded) {

        groupViewHolder.accountTypeName.setText(accountType.getAccountTypeName()); //get the account name (Checking, savings, etc)
        double accountTypeSum = 0;

        for (BankAccount bankAccount : accountType.getBankAccounts()) {
            accountTypeSum = accountTypeSum + bankAccount.getBalance();
        }

        String formatedSum = NumberFormat.getCurrencyInstance().format(accountTypeSum);

        groupViewHolder.accountTypeSum.setText(formatedSum);

        if (expanded) {
            groupViewHolder.accountTypeIndicator.setText(mContext.getString(R.string.account_types_indicator_hide));
        } else {
            groupViewHolder.accountTypeIndicator.setText(mContext.getString(R.string.account_types_indicator_show));
        }
    }



    static class GroupViewHolder
    {
        TextView accountTypeName, accountTypeSum, accountTypeIndicator;
    }

}
