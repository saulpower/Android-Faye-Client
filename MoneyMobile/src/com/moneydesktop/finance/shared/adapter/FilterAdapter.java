package com.moneydesktop.finance.shared.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FilterType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.database.TagInstanceDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.CaretView;
import com.moneydesktop.finance.views.UltimateListView;

import org.apache.commons.lang.WordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilterAdapter extends UltimateAdapter implements OnGroupExpandListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<String, List<FilterViewHolder>>> mData;
    private Activity mActivity;
    private UltimateListView mListView;
    private ArrayList<AsyncTask<Integer, Void, Integer>> mLoaders = new ArrayList<AsyncTask<Integer, Void, Integer>>();
    private boolean[] mSubLoaded;
    
    private boolean mIsHandset = false;
    
    public FilterAdapter(Activity activity, UltimateListView listView, List<Pair<String, List<FilterViewHolder>>> data) {
        this(activity, listView, data, false);
    }

    public FilterAdapter(Activity activity, UltimateListView listView, List<Pair<String, List<FilterViewHolder>>> data, boolean isHandset) {
        
        mActivity = activity;
        mListView = listView;
        mData = data;
        mIsHandset = isHandset;
        
        mListView.setOnGroupExpandListener(this);
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        
        if (!isSectionLoaded(groupPosition)) {
            return null;
        }
        
        return mData.get(groupPosition).second.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (new String(groupPosition + "-" + childPosition)).hashCode();
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
        
        if (filter == null) {
            filter = new FilterViewHolder();
            filter.mText = mActivity.getString(R.string.loading_menu);
        }
        
        boolean isSelected = (groupPosition == mSelectedGroupPosition && childPosition == mSelectedChildPosition);
        
        configureCell(viewHolder, filter, isSelected);
        
        return cell;
    }
    
    private void configureCell(FilterViewHolder viewHolder, FilterViewHolder filter, boolean isSelected) {
        
        viewHolder.mSubSection = filter.mSubSection;
        viewHolder.mQuery = filter.mQuery;
        viewHolder.mCaret.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        
        if (!filter.mIsSubSection) {
            viewHolder.mRoot.setBackgroundResource(R.drawable.gray4_to_primary);
        } else {
            viewHolder.mRoot.setBackgroundResource(R.drawable.gray3_to_primary);
        }
        
        if (filter.mSubText == null || filter.mSubText.equals("")) {
            
            viewHolder.mInfo.setVisibility(View.INVISIBLE);
            viewHolder.mSingleTitle.setVisibility(View.VISIBLE);
            viewHolder.mSingleTitle.setText(filter.mText);
            viewHolder.mRoot.setBackgroundResource(isSelected ? R.color.primaryColor : R.drawable.gray4_to_primary);
            
            return;
        }
        
        viewHolder.mInfo.setVisibility(View.VISIBLE);
        viewHolder.mRoot.setBackgroundResource(isSelected ? R.color.primaryColor : R.drawable.gray4_to_primary);
        viewHolder.mSingleTitle.setVisibility(View.INVISIBLE);
        viewHolder.mTitle.setText(filter.mText);
        viewHolder.mSubTitle.setText(filter.mSubText);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        
        int size = mData.get(groupPosition).second.size();
        
        size = getLoadedAdjustment(size, groupPosition);
        
        return size;
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
        return (new String("section" + groupPosition)).hashCode();
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
        viewHolder.mHeaderTitle.setText(title);
        
        CaretView caret = viewHolder.mCaret;
        
        if (isExpanded) {
            caret.setCaretRotation(0);
        } else {
            caret.setCaretRotation(90);
        }
        
        return cell;
    }
    
    private FilterViewHolder createViewHolder(View cell) {

        FilterViewHolder viewHolder = new FilterViewHolder();

        viewHolder.mRoot = (RelativeLayout) cell.findViewById(R.id.root);
        viewHolder.mTitle = (TextView) cell.findViewById(R.id.title);
        viewHolder.mSubTitle = (TextView) cell.findViewById(R.id.subtitle);
        viewHolder.mHeaderTitle = (TextView) cell.findViewById(R.id.header_title);
        viewHolder.mCaret = (CaretView) cell.findViewById(R.id.caret);
        viewHolder.mSingleTitle = (TextView) cell.findViewById(R.id.single_title);
        viewHolder.mInfo = (LinearLayout) cell.findViewById(R.id.info);
        
        applyFonts(viewHolder);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
    }
    
    private void applyFonts(FilterViewHolder viewHolder) {

        Fonts.applyPrimarySemiBoldFont(viewHolder.mTitle, mIsHandset ? 9 : 12);
        Fonts.applyPrimarySemiBoldFont(viewHolder.mSubTitle, mIsHandset ? 7 : 10);
        Fonts.applyPrimarySemiBoldFont(viewHolder.mSingleTitle, mIsHandset ? 9 : 12);
        Fonts.applyPrimaryBoldFont(viewHolder.mHeaderTitle, mIsHandset ? 8 : 12);
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

        FilterViewHolder holder = (FilterViewHolder) header.getTag();
        
        TextView sectionHeader = holder.mHeaderTitle;
        sectionHeader.setText((String) getGroup(section));
        
        CaretView caret = holder.mCaret;
        
        if (isSectionExpanded(section)) {
            caret.setCaretRotation(0);
        } else {
            caret.setCaretRotation(90);
        }
    }

    @Override
    protected void loadSection(final int section) {
        
        AsyncTask<Integer, Void, Integer> temp = new AsyncTask<Integer, Void, Integer>() {

            @Override
            protected Integer doInBackground(Integer... params) {
                
                int section = params[0];

                if (isCancelled()) {
                    return -1;
                }
                
                loadFilter(section);
                
                return section;
            }

            @Override
            protected void onPostExecute(Integer section) {

                sectionLoaded(section, (section != -1));
                
                if (isCancelled()) {
                    return;
                }
                
                mLoaders.remove(this);
            }

        }.execute(section);
        
        mLoaders.add(temp);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }
    
    public void cancelLoaders() {
        
        for (AsyncTask<Integer, Void, Integer> task : mLoaders) {
            task.cancel(false);
        }
        
        mLoaders.clear();
    }
    
    private void loadFilter(int section) {
        
        String sectionTitle = mData.get(section).first;
        List<FilterViewHolder> sectionData = new ArrayList<FilterViewHolder>();
        
        switch (FilterType.fromInteger(section)) {
            
            case FILTER_BANKS: {
                
                BankDao bankDao = ApplicationContext.getDaoSession().getBankDao();
                List<Bank> banks = bankDao.loadAll();
                
                for (Bank bank : banks) {
                    
                    if (bank.getBankAccounts().size() > 1) {
                        
                        PowerQuery query = PowerQuery.where(false, new QueryProperty(BankAccountDao.TABLENAME, BankAccountDao.Properties.InstitutionId), bank.getBankId());
                        
                        FilterViewHolder temp = new FilterViewHolder();
                        temp.mText = bank.getBankName().toUpperCase();
                        temp.mSubText = mActivity.getString(R.string.all_accounts);
                        temp.mQuery = query;
                        
                        sectionData.add(temp);
                    }
                    
                    for (BankAccount account : bank.getBankAccounts()) {
                        
                        PowerQuery query = PowerQuery.where(false, new QueryProperty(BankAccountDao.TABLENAME, BankAccountDao.Properties.AccountId), account.getAccountId());
                        
                        FilterViewHolder temp = new FilterViewHolder();
                        temp.mText = bank.getBankName().toUpperCase();
                        temp.mSubText = WordUtils.capitalize(account.getAccountName().toLowerCase());
                        temp.mQuery = query;
                        
                        sectionData.add(temp);
                    }
                }
                
                Collections.sort(sectionData, new FilterComparator());
                
                mData.set(section, new Pair<String, List<FilterViewHolder>>(sectionTitle, sectionData));
                
                break;
            }
            
            case FILTER_CATEGORIES: {
                
                CategoryDao catDao = ApplicationContext.getDaoSession().getCategoryDao();
                List<Category> categories = catDao.loadAll();
                
                for (Category cat : categories) {

                    PowerQuery query = PowerQuery.where(false, new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.CategoryId), cat.getCategoryId());
                    
                    FilterViewHolder temp = new FilterViewHolder();
                    temp.mText = cat.getCategoryName().toUpperCase();
                    temp.mQuery = query;
                    
                    sectionData.add(temp);
                }
                
                Collections.sort(sectionData, new FilterComparator());
                
                mData.set(section, new Pair<String, List<FilterViewHolder>>(sectionTitle, sectionData));
                
                break;
            }
            
            case FILTER_TAGS: {
                
                List<Tag> tags = Tag.loadAll();
                
                // Create the filter query for each tag
                for (Tag tag : tags) {

                    PowerQuery query = PowerQuery.where(false, new QueryProperty(TagInstanceDao.TABLENAME, TagInstanceDao.Properties.TagId), Long.toString(tag.getId()));
                    
                    FilterViewHolder temp = new FilterViewHolder();
                    temp.mText = tag.getTagName().toUpperCase();
                    temp.mQuery = query;
                    
                    // Add the item to our data list
                    sectionData.add(temp);
                }
                
                // Sort the data alphabetically
                Collections.sort(sectionData, new FilterComparator());
                
                mData.set(section, new Pair<String, List<FilterViewHolder>>(sectionTitle, sectionData));
                
                break;
            }
            
            case FILTER_PAYEES: {
                
                TransactionsDao transactionsDao = ApplicationContext.getDaoSession().getTransactionsDao();
                List<Transactions> transactions = transactionsDao.queryRaw("GROUP BY TITLE ORDER BY TITLE", new String[] {});
                
                String startUpper = transactions.get(0).getTitle().substring(0, 1).toUpperCase();

                List<FilterViewHolder> subSection = new ArrayList<FilterViewHolder>();
                
                PowerQuery query = PowerQuery.whereLike(false, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title), startUpper + "%");
                
                FilterViewHolder temp = new FilterViewHolder();
                temp.mText = startUpper.equals(" ") ? "\" \"" : startUpper;
                temp.mQuery = query;
                
                for (Transactions transaction : transactions) {

                    query = PowerQuery.whereLike(false, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title), transaction.getTitle());
                    
                    FilterViewHolder subTemp = new FilterViewHolder();
                    subTemp.mText = transaction.getTitle().toUpperCase();
                    subTemp.mIsSubSection = true;
                    subTemp.mQuery = query;
                    
                    if (!subTemp.mText.substring(0, 1).equals(startUpper)) {

                        temp.mSubSection = subSection;
                        sectionData.add(temp);
                        
                        startUpper = subTemp.mText.substring(0, 1);
                        
                        subSection = new ArrayList<FilterViewHolder>();
                        subSection.add(subTemp);
                        
                        query = PowerQuery.whereLike(false, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title), startUpper + "%");
                        
                        temp = new FilterViewHolder();
                        temp.mText = startUpper;
                        temp.mQuery = query;
                        
                    } else {

                        subSection.add(subTemp);
                    }
                }
                
                temp.mSubSection = subSection;
                sectionData.add(temp);
                
                mSubLoaded = new boolean[sectionData.size()];
                
                mData.set(section, new Pair<String, List<FilterViewHolder>>(sectionTitle, sectionData));
                
                break;
            }
        }
    }
    
    public void expandSubSection(int groupPosition, int childPosition, List<FilterViewHolder> subSection) {
        
        if (!mSubLoaded[childPosition]) {
            mSubLoaded[childPosition] = true;
            List<FilterViewHolder> position = mData.get(groupPosition).second;
            position.addAll(childPosition + 1, subSection);
            notifyDataSetChanged();
        }
    }
    
    public class FilterComparator implements Comparator<FilterViewHolder> {
        
        @Override
        public int compare(FilterViewHolder holder1, FilterViewHolder holder2) {
            return holder1.mText.compareTo(holder2.mText);
        }
    }

    @Override
    protected boolean isSectionLoadable(int section) {
        
        return (section != 0);
    }
}
