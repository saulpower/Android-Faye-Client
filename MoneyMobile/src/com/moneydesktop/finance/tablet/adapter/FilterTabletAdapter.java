package com.moneydesktop.finance.tablet.adapter;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.UltimateAdapter;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.CaretView;

import java.util.List;

public class FilterTabletAdapter extends UltimateAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<String, List<FilterViewHolder>>> mData;
    private Activity mActivity;

    public FilterTabletAdapter(Activity activity, List<Pair<String, List<FilterViewHolder>>> data) {
        
        mActivity = activity;
        mData = data;
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mData.get(groupPosition).second.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getItemView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        
        FilterViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.tablet_filter_subitem, parent, false);
            
            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (FilterViewHolder) cell.getTag();
        }
        
        FilterViewHolder filter = (FilterViewHolder) getChild(groupPosition, childPosition);
        
        if (filter != null) {
            
            viewHolder.title.setText(filter.text);
            viewHolder.subTitle.setText(filter.subText);
        }
        
        return cell;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).second.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition).first;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getSectionView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        
        final FilterViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.tablet_filter_item_header, parent, false);

            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (FilterViewHolder) cell.getTag();
        }
        
        String title = (String) getGroup(groupPosition);
        viewHolder.headerTitle.setText(title);
        
        CaretView caret = viewHolder.caret;
        
        if (isExpanded) {
            caret.setCaretRotation(0);
        } else {
            caret.setCaretRotation(90);
        }
        
        return cell;
    }
    
    private FilterViewHolder createViewHolder(View cell) {

        FilterViewHolder viewHolder = new FilterViewHolder();

        viewHolder.title = (TextView) cell.findViewById(R.id.title);
        viewHolder.subTitle = (TextView) cell.findViewById(R.id.subtitle);
        viewHolder.headerTitle = (TextView) cell.findViewById(R.id.header_title);
        viewHolder.caret = (CaretView) cell.findViewById(R.id.caret);
        
        applyFonts(viewHolder);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
    }
    
    private void applyFonts(FilterViewHolder viewHolder) {

        Fonts.applyPrimarySemiBoldFont(viewHolder.title, 12);
        Fonts.applyPrimaryFont(viewHolder.subTitle, 9);
        Fonts.applyPrimaryBoldFont(viewHolder.headerTitle, 12);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void configureHeader(View header, int section) {

        FilterViewHolder holder = (FilterViewHolder) header.getTag();
        
        TextView sectionHeader = holder.headerTitle;
        sectionHeader.setText((String) getGroup(section));
        
        CaretView caret = holder.caret;
        
        if (isSectionVisible(section)) {
            caret.setCaretRotation(0);
        } else {
            caret.setCaretRotation(90);
        }
    }

    @Override
    protected void onNextPageRequested(int page) {
    }
}
