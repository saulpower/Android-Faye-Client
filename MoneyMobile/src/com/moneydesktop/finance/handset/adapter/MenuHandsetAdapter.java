package com.moneydesktop.finance.handset.adapter;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.MenuViewHolder;
import com.moneydesktop.finance.util.Fonts;

public class MenuHandsetAdapter extends ArrayAdapter<int[]> {

    public final String TAG = this.getClass().getSimpleName();
	
	private Activity mActivity;
	private int mSelectedIndex = -1;

	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	public void setSelectedIndex(int mSelectedIndex) {
		this.mSelectedIndex = mSelectedIndex;
    	notifyDataSetChanged();
	}

	public MenuHandsetAdapter(Activity activity, int textViewResourceId, List<int[]> objects) {
		super(activity, textViewResourceId, objects);
		
		mActivity = activity;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        View cell = convertView;
        
        if (cell == null) {
            cell = mActivity.getLayoutInflater().inflate(R.layout.handset_menu_item, parent, false);
            createViewHolder(cell);
        }
        
        MenuViewHolder holder = (MenuViewHolder) cell.getTag();
        
        int[] item = getItem(position);
        
        if (item != null) {
        	
        	boolean selected = position == mSelectedIndex;
        	
        	int color = mActivity.getResources().getColor(selected ? R.color.white : R.color.gray2);
        	
        	holder.icon.setText(item[0]);
        	holder.icon.setTextColor(color);
        	holder.title.setText(getContext().getString(item[1]).toUpperCase());
        	holder.title.setTextColor(color);
        	holder.item.setBackgroundResource(selected ? R.color.primaryColor : R.drawable.transparent_to_primary);
        }
        
        return cell;
	}
	
	private MenuViewHolder createViewHolder(View cell) {
		
		MenuViewHolder holder = new MenuViewHolder();
		holder.item = (LinearLayout) cell.findViewById(R.id.item);
		holder.icon = (TextView) cell.findViewById(R.id.icon);
		holder.title = (TextView) cell.findViewById(R.id.title);
		
		applyFonts(holder);
		
		cell.setTag(holder);
		
		return holder;
	}
	
	private void applyFonts(MenuViewHolder holder) {
		Fonts.applyGlyphFont(holder.icon, 17);
		Fonts.applyPrimaryFont(holder.title, 9);
	}
}
