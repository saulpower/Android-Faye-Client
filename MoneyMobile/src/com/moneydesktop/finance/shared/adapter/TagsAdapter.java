package com.moneydesktop.finance.shared.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.BusinessObjectBase;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.shared.TagViewHolder;
import com.moneydesktop.finance.tablet.adapter.DeleteBaseAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.ClearEditText;
import com.moneydesktop.finance.views.DeleteLinearLayout;
import com.moneydesktop.finance.views.DeleteLinearLayout.OnCellDeletedListener;

public class TagsAdapter extends DeleteBaseAdapter<Tag> implements Filterable {
    
    public final String TAG = this.getClass().getSimpleName();

    private Activity mActivity;
    
    private List<Tag> mTags;
    private List<Tag> mFilteredTags;
    private List<Tag> mObjectTags = new ArrayList<Tag>();
    private Filter mFilter;
    private Object mLock = new Object();
    private ClearEditText mSearch;

    private BusinessObjectBase mBusinessObject;
    
    public TagsAdapter(Activity activity, int resourceId, List<Tag> tags, ListView listView, ClearEditText search, long id) {
        super(activity, resourceId, tags, listView);

        mActivity = activity;
        
        mBusinessObject = BusinessObjectBase.getBusinessObjectBase(id);
        
        updateObjectTagList();
        
        mTags = tags;
        mFilteredTags = new ArrayList<Tag>(tags);
        mSearch = search;
    }
    
    private void updateObjectTagList() {
        
        mObjectTags.clear();
        mObjectTags.addAll(mBusinessObject.getTags());
    }
    
    public void updateData(List<Tag> tags) {
    	mTags = tags;
        getFilter().filter(mSearch.getText().toString());
    }
    
    @Override
    public int getCount() {
        return (mFilteredTags.size() == 0) ? 1 : mFilteredTags.size();
    }
    
    @Override
    public Tag getItem(int position) {
        
        Tag tag = null;
        
        if (position < mFilteredTags.size()) {
            tag = mFilteredTags.get(position);
        } else if (!mSearch.getText().toString().equals("")) {
            tag = new Tag();
            tag.setTagName(mSearch.getText().toString() + " " + getContext().getString(R.string.new_tag));
            tag.setTagId(mSearch.getText().toString());
            tag.setId(-1l);
        } else {
        	 tag = new Tag();
             tag.setTagName(getContext().getString(R.string.label_no_tags).toUpperCase());
             tag.setId(0l);
        }
        
        return tag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        final TagViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.tag_item, parent, false);
            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (TagViewHolder) cell.getTag();
        }
        
        Tag tag = getItem(position);
        viewHolder.tag = tag;
        
        if (tag != null) {
            
            viewHolder.itemTitle.setText(tag.getTagName());

            viewHolder.checkBox.setVisibility(tag.getId() == 0 ? View.GONE : View.VISIBLE);
            viewHolder.checkBox.setOnCheckedChangeListener(null);
            viewHolder.checkBox.setChecked(mObjectTags.contains(tag));
            viewHolder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    
                	if (viewHolder.tag.getId() == 0) return;
                	
                    if (viewHolder.tag.getId() == -1) {
                        viewHolder.tag = createTag(viewHolder.tag.getTagId());
                    }
                    
                    if (isChecked) {
                        viewHolder.tag.tagObject(mBusinessObject);
                    } else {
                        viewHolder.tag.untagObject(mBusinessObject);
                    }
                    
                    notifyDataSetChanged();
                }
            });
        }
        
        return cell;
    }
    
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        
        updateObjectTagList();
    }
    
    private TagViewHolder createViewHolder(View cell) {

        final TagViewHolder viewHolder = new TagViewHolder();

        viewHolder.checkBox = (CheckBox) cell.findViewById(R.id.checkbox);
        viewHolder.itemTitle = (TextView) cell.findViewById(R.id.item_title);
        viewHolder.deleteCell = (DeleteLinearLayout) cell.findViewById(R.id.info);
        
        viewHolder.deleteCell.setDeleteBaseAdapter(this);
        viewHolder.deleteCell.setOnCellDeletedListener(new OnCellDeletedListener() {
            
            @Override
            public void onCellDeleted() {
              Tag.deleteTag(viewHolder.tag);
              updateTags();
            }
        });
        
        viewHolder.deleteCell.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                viewHolder.checkBox.setChecked(!viewHolder.checkBox.isChecked());
            }
        });
        
        applyFonts(viewHolder);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
    }
    
    private void applyFonts(TagViewHolder viewHolder) {
        Fonts.applyPrimaryBoldFont(viewHolder.itemTitle, 14);
    }
    
    private Tag createTag(String name) {
        
        if (name == null || name.equals("")) return null;
        
        Tag tag = Tag.createTag(name);
        updateTags();
        
        mSearch.setText("");
        mSearch.clearFocus();
        UiUtils.hideKeyboard(mActivity, mSearch);
        
        return tag;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new TagFilter();
        }
        return mFilter;
    }

    private void updateTags() {
        
        new AsyncTask<Void, Void, List<Tag>>() {

            @Override
            protected List<Tag> doInBackground(Void... params) {

                if (isCancelled()) {
                    return null;
                }
                
                List<Tag> data = Tag.loadAll();

                return data;
            }

            @Override
            protected void onPostExecute(List<Tag> data) {
                
                if (isCancelled()) {
                    return;
                }
                
                mTags = data;
                notifyDataSetChanged();
                getFilter().filter(mSearch.getText().toString());
            }

        }.execute();
    }
    
    private class TagFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            
            if (constraint == null || constraint.length() == 0) {
                
                synchronized (mLock) {
                    results.values = mTags;
                    results.count = mTags.size();
                }
                
            } else {
                
                String filterText = constraint.toString().toLowerCase();
                final List<Tag> list = mTags;
                final int count = mTags.size();
                
                final List<Tag> newItems = new ArrayList<Tag>(count);
                
                for (Tag tag : list) {

                    if (tag.getTagName().toLowerCase().contains(filterText)) {
                        newItems.add(tag);
                    }
                }
                
                results.values = newItems;
                results.count = newItems.size();
            }
            
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            
            mFilteredTags = (List<Tag>) results.values;
            
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
