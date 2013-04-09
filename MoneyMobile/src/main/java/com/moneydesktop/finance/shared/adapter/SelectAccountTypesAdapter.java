package main.java.com.moneydesktop.finance.shared.adapter;

import java.util.List;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.AccountTypesEnum;
import main.java.com.moneydesktop.finance.database.AccountType;
import main.java.com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SelectAccountTypesAdapter extends ArrayAdapter<AccountType> {

    private Context mContext;
    private int mLayoutId;
    private List<AccountType> mAccountTypesList;

    public SelectAccountTypesAdapter(Context context, int layoutResourceId, List<AccountType> accountTypes) {
        super(context, layoutResourceId, accountTypes);

        mContext = context;
        mLayoutId = layoutResourceId;
        mAccountTypesList = accountTypes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AccountTypesHolder holder = new AccountTypesHolder();

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        convertView = inflater.inflate(mLayoutId, parent, false);

        holder.accountTypeImg = (TextView)convertView.findViewById(R.id.image);
        holder.txtTitle = (TextView)convertView.findViewById(R.id.tablet_add_bank_manually_account_type_name_list_item);


        AccountTypesEnum type = AccountTypesEnum.fromString(mAccountTypesList.get(position).getAccountTypeName().toUpperCase());

        switch (type) {
        case CASH:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_cash));
            break;

        case CHECKING:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_checking));
            break;

        case CREDIT_CARD:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_cc));
            break;

        case INVESTMENTS:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_inv));
            break;

        case LINE_OF_CREDIT:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_loc));
            break;

        case LOANS:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_loans));
            break;

        case MORTGAGE:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_mort));
            break;

        case PROPERTY:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_prop));
            break;

        case SAVINGS:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_saving));
            break;

        default:
            break;
        }

        holder.txtTitle.setText(mAccountTypesList.get(position).getAccountTypeName());

        Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);
        Fonts.applyGlyphFont(holder.accountTypeImg, 20);

        return convertView;
    }

    static class AccountTypesHolder
    {
        TextView accountTypeImg;
        TextView txtTitle;
    }

}