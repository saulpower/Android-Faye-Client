package com.moneydesktop.finance.tablet.adapter;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.UltimateAdapter;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.shared.CategoryViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.UltimateListView;

import java.util.ArrayList;
import java.util.List;

public class CategoryTabletAdapter extends UltimateAdapter implements Filterable {
    
    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<Category, List<Category>>> mData;
    private List<Pair<Category, List<Category>>> mFilteredData;
    private Activity mActivity;
    private Filter mFilter;
    private Object mLock = new Object();
    private UltimateListView mListView;
    
    public CategoryTabletAdapter(Activity activity, UltimateListView listView, List<Pair<Category, List<Category>>> data) {
        
        mActivity = activity;
        mListView = listView;
        mData = data;
        mFilteredData = new ArrayList<Pair<Category, List<Category>>>(data);
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        
        if (!isSectionLoaded(groupPosition)) {
            return null;
        }
        
        return mFilteredData.get(groupPosition).second.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (new String(groupPosition + "-" + childPosition)).hashCode();
    }

    @Override
    public View getItemView(int section, int position, boolean isLastChild, View convertView, ViewGroup parent) {
        
        CategoryViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.tablet_category_item, parent, false);
            
            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (CategoryViewHolder) cell.getTag();
        }
        
        Category category = (Category) getChild(section, position);
        
        viewHolder.itemTitle.setText(category.getCategoryName());
        
        return cell;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        
        int size = mFilteredData.get(groupPosition).second.size();
        
        size = getLoadedAdjustment(size, groupPosition);
        
        return size;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mFilteredData.get(groupPosition).first;
    }

    @Override
    public int getGroupCount() {
        return mFilteredData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (new String("section" + groupPosition)).hashCode();
    }

    @Override
    public View getSectionView(int section, boolean isExpanded, View convertView, ViewGroup parent) {
        
        final CategoryViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.tablet_category_item_header, parent, false);

            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (CategoryViewHolder) cell.getTag();
        }

        Category category = mFilteredData.get(section).first;
        viewHolder.icon.setText(category.getImageName());
        viewHolder.title.setText(category.getCategoryName().replace("+", "&"));
        
        return cell;
    }
    
    private CategoryViewHolder createViewHolder(View cell) {

        CategoryViewHolder viewHolder = new CategoryViewHolder();

        viewHolder.icon = (TextView) cell.findViewById(R.id.icon);
        viewHolder.title = (TextView) cell.findViewById(R.id.title);
        viewHolder.itemTitle = (TextView) cell.findViewById(R.id.item_title);
        viewHolder.subCategory = (TextView) cell.findViewById(R.id.sub_category);
        
        applyFonts(viewHolder);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
    }
    
    private void applyFonts(CategoryViewHolder viewHolder) {

        Fonts.applyGlyphFont(viewHolder.icon, 26);
        Fonts.applyPrimaryBoldFont(viewHolder.title, 14);
        Fonts.applyPrimaryFont(viewHolder.itemTitle, 12);
        Fonts.applyPrimarySemiBoldFont(viewHolder.subCategory, 12);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void configureHeader(View header, int section) {

        Category category = mFilteredData.get(section).first;
        
        CategoryViewHolder holder = (CategoryViewHolder) header.getTag();
        holder.icon.setText(category.getImageName());
        holder.title.setText(category.getCategoryName().replace("+", "&"));
    }

    @Override
    protected void loadSection(final int section) {
    }

    @Override
    protected boolean isSectionLoadable(int section) {
        return false;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CategoryFilter();
        }
        return mFilter;
    }
    
    private class CategoryFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            
            FilterResults results = new FilterResults();
            
            if (constraint == null || constraint.length() == 0) {
                
                synchronized (mLock) {
                    results.values = mData;
                    results.count = mData.size();
                }
                
            } else {
                
                String filterText = constraint.toString().toLowerCase();
                final List<Pair<Category, List<Category>>> list = mFilteredData;
                final int count = mFilteredData.size();
                
                final List<Pair<Category, List<Category>>> newSection = new ArrayList<Pair<Category, List<Category>>>(count);
                
                for (Pair<Category, List<Category>> section : list) {
                    
                    boolean useSection = section.first.getCategoryName().toLowerCase().startsWith(filterText);
                    final List<Category> sectionItems = section.second;
                    final int sectionCount = section.second.size();
                    
                    final List<Category> newSectionItems = new ArrayList<Category>(sectionCount);
                    
                    for (Category cat : sectionItems) {
                        
                        if (cat.getCategoryName().toLowerCase().startsWith(filterText)) {
                            newSectionItems.add(cat);
                            useSection = true;
                        }
                    }
                    
                    if (useSection) {
                        newSection.add(new Pair<Category, List<Category>>(section.first, newSectionItems));
                    }
                }
                results.values = newSection;
                results.count = newSection.size();
            }
            
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            
            mFilteredData = (List<Pair<Category, List<Category>>>) results.values;
            
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
            
            mListView.expandAll();
        }
    }
}
