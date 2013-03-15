package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.util.Fonts;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/11/13
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class BankAccountTabletAdapter extends BaseAdapter {

    private Context mContext;
    List<BankAccount> mBanks;

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param objects            The BankAccounts to represent in the ListView.
     */
    public BankAccountTabletAdapter(Context context, List<BankAccount> objects) {

        mContext = context;
        mBanks = objects;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {

        int count = mBanks.size();

        if (count == 0) {
            return 1;
        }

        return count;
    }

    @Override
    public Object getItem(int position) {

        if (mBanks.size() == 0) {
            return null;
        }

        return mBanks.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View cell = convertView;

        if (cell == null) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            cell = inflater.inflate(R.layout.manual_bank_item, parent, false);
        }

        BankAccount account = (BankAccount) getItem(position);

        TextView name = (TextView) cell.findViewById(R.id.name);
        TextView subtext = (TextView) cell.findViewById(R.id.subtext);
        Fonts.applyPrimaryFont(name, 12);
        Fonts.applyPrimaryFont(subtext, 10);

        if (account == null) {
            name.setText(R.string.label_no_manual_accounts);
            subtext.setVisibility(View.VISIBLE);
            subtext.setText(R.string.label_create);
        } else {
            name.setText(account.getAccountName());
            subtext.setVisibility(View.GONE);
        }

        cell.setTag(account);

        return cell;
    }
}
