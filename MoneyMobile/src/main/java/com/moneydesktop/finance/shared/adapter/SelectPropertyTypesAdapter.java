package main.java.com.moneydesktop.finance.shared.adapter;

import java.util.List;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.AccountTypesEnum;
import main.java.com.moneydesktop.finance.data.Enums.PropertyTypesEnum;
import main.java.com.moneydesktop.finance.database.AccountType;
import main.java.com.moneydesktop.finance.util.Fonts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectPropertyTypesAdapter extends ArrayAdapter<AccountType> {

    private Context mContext;
    private int mLayoutId;
    private List<AccountType> mAccountTypesList;

    public SelectPropertyTypesAdapter(Context context, int layoutResourceId, List<AccountType> accountTypes) {
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

        holder.accountTypeImg = (TextView)convertView.findViewById(R.id.txt_image);
        holder.txtTitle = (TextView)convertView.findViewById(R.id.select_property_type_name_list_item);

        PropertyTypesEnum type = PropertyTypesEnum.fromString(mAccountTypesList.get(position).getAccountTypeName().toUpperCase());

        switch (type) {
        case REAL_ESTATE:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_real_estate));
            break;

        case VEHICLE:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_vehicle));
            break;

        case ART:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_art));
            break;

        case JEWELRY:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_jewelry));
            break;

        case FURNITURE:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_furniture));
            break;

        case APPLIANCES:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_appliances));
            break;

        case COMPUTER:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_computer));
            break;

        case ELECTRONICS:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_electronics));
            break;

        case SPORTS_EQUIPMENT:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_sports_equipment));
            break;

        case MISCELLANEOUS:
            holder.accountTypeImg.setText(mContext.getString(R.string.icon_miscellaneous));
            break;

        default:
            break;
        }


        holder.txtTitle.setText(mAccountTypesList.get(position).getAccountTypeName());
        Fonts.applyGlyphFont(holder.accountTypeImg, 22);
        Fonts.applyPrimaryBoldFont(holder.txtTitle, 14);


        return convertView;
    }


    static class AccountTypesHolder
    {
        TextView accountTypeImg;
        TextView txtTitle;
    }



}