package main.java.com.moneydesktop.finance.shared.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.DataController;
import main.java.com.moneydesktop.finance.database.Category;
import main.java.com.moneydesktop.finance.shared.CategoryViewHolder;
import main.java.com.moneydesktop.finance.shared.fragment.CategoriesFragment;
import main.java.com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.ClearEditText;
import main.java.com.moneydesktop.finance.views.UltimateListView;

@TargetApi(11)
public class CategoryAdapter extends UltimateAdapter implements Filterable {

    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<Category, List<Category>>> mData;
    private List<Pair<Category, List<Category>>> mFilteredData;
    private Activity mActivity;
    private Filter mFilter;
    private Object mLock = new Object();
    private UltimateListView mListView;
    private ClearEditText mSearch;
    private CategoriesFragment mFragment;

    public CategoryAdapter(Activity activity, CategoriesFragment fragment, UltimateListView listView, List<Pair<Category, List<Category>>> data, ClearEditText search) {

        mActivity = activity;
        mFragment = fragment;
        mListView = listView;
        mData = data;
        mFilteredData = new ArrayList<Pair<Category, List<Category>>>(data);
        mSearch = search;
    }

    public void updateData(List<Pair<Category, List<Category>>> categories) {
        mData = categories;
        getFilter().filter(mSearch.getText().toString());
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        if (!isSectionLoaded(groupPosition)) {
            return null;
        }

        Object object = null;

        if (childPosition < mFilteredData.get(groupPosition).second.size()) {
            object = mFilteredData.get(groupPosition).second.get(childPosition);
        }

        return object;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (new String(groupPosition + "-" + childPosition)).hashCode();
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return (getChild(groupPosition, childPosition) == null) ? 1 : 0;
    }

    @Override
    public int getChildTypeCount() {
        return 2;
    }

    @Override
    public View getItemView(int section, int position, boolean isLastChild, View convertView, ViewGroup parent) {

        CategoryViewHolder viewHolder;
        View cell = convertView;

        if (cell == null) {

            cell = mActivity.getLayoutInflater().inflate(R.layout.category_item, parent, false);

            viewHolder = createViewHolder(cell);

        } else {

            viewHolder = (CategoryViewHolder) cell.getTag();
        }

        Category category = (Category) getChild(section, position);

        configureListeners(category != null, viewHolder);

        if (category != null) {
            viewHolder.parent = null;
            viewHolder.subCategory.setVisibility(View.GONE);
            viewHolder.itemTitle.setText(category.getCategoryName());
            Fonts.applyPrimaryFont(viewHolder.itemTitle, 12);
        } else {
            Category parentCat = mFilteredData.get(section).first;
            viewHolder.flipper.setDisplayedChild(0);
            viewHolder.parent = parentCat;
            viewHolder.subCategory.setVisibility(View.VISIBLE);
            viewHolder.itemTitle.setText(ApplicationContext.getContext().getString(R.string.new_sub_category));
            Fonts.applyPrimaryBoldFont(viewHolder.itemTitle, 12);
        }

        return cell;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        int size = mFilteredData.get(groupPosition).second.size();

        size = getLoadedAdjustment(size, groupPosition);

        // Add row for new sub categories
        size++;

        return size;
    }

    @Override
    public Object getGroup(int groupPosition) {

        Object object = null;

        if (groupPosition >= 0 && groupPosition < mFilteredData.size()) {
            object = mFilteredData.get(groupPosition).first;
        }

        return object;
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

            cell = mActivity.getLayoutInflater().inflate(R.layout.category_item_header, parent, false);

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

        final CategoryViewHolder viewHolder = new CategoryViewHolder();

        viewHolder.icon = (TextView) cell.findViewById(R.id.icon);
        viewHolder.title = (TextView) cell.findViewById(R.id.title);
        viewHolder.itemTitle = (TextView) cell.findViewById(R.id.item_title);
        viewHolder.subCategory = (TextView) cell.findViewById(R.id.sub_category);
        viewHolder.newCategory = (EditText) cell.findViewById(R.id.new_category);
        viewHolder.cancel = (TextView) cell.findViewById(R.id.cancel);

        viewHolder.flipper = (ViewFlipper) cell.findViewById(R.id.flipper);
        viewHolder.addCategory = (LinearLayout) cell.findViewById(R.id.add);
        viewHolder.info = (LinearLayout) cell.findViewById(R.id.info);

        if (viewHolder.cancel != null) {

            viewHolder.info.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    viewHolder.flipper.showNext();

                    // Make adjustments to show EditText when covered by keyboard
                    if (mActivity instanceof PopupTabletActivity) {
                        ((PopupTabletActivity) mActivity).setEditText(viewHolder.newCategory);
                    }

                    viewHolder.newCategory.requestFocus();
                    UiUtils.showKeyboard(mActivity, viewHolder.newCategory);
                }
            });

            viewHolder.cancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    viewHolder.flipper.showPrevious();
                    viewHolder.newCategory.clearFocus();
                    UiUtils.hideKeyboard(mActivity, viewHolder.newCategory);
                }
            });

            viewHolder.newCategory.setOnEditorActionListener(new OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        createSubCategory(v.getText().toString(), viewHolder.parent);
                        v.setText("");
                        v.clearFocus();
                        viewHolder.flipper.showPrevious();
                        UiUtils.hideKeyboard(mActivity, v);

                        return true;
                    }

                    return false;
                }
            });
        }

        applyFonts(viewHolder);

        cell.setTag(viewHolder);

        return viewHolder;
    }

    private void configureListeners(boolean isCategory, final CategoryViewHolder viewHolder) {

        viewHolder.info.setClickable(!isCategory);
    }

    private void applyFonts(CategoryViewHolder viewHolder) {

        Fonts.applyGlyphFont(viewHolder.icon, 26);
        Fonts.applyPrimaryBoldFont(viewHolder.title, 14);
        Fonts.applyGlyphFont(viewHolder.cancel, 18);
        Fonts.applyPrimaryFont(viewHolder.newCategory, 10);
        Fonts.applyPrimaryBoldFont(viewHolder.subCategory, 20);
    }

    private void createSubCategory(String name, Category parent) {

        if (name == null || name.equals("") || parent == null || parent.getId() == null) return;

        long id = DataController.createRandomGuid(Category.class);

        Category cat = new Category(id);
        cat.setCategoryId(String.valueOf(id));
        cat.setCategoryName(name);
        cat.setImageName(parent.getImageName());
        cat.setParent(parent);
        cat.setIsSystem(false);
        cat.setCategoryType(parent.getCategoryType());
        cat.insertSingle();

        mFragment.dismissPopup(cat);
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

        Category category = (Category) getGroup(section);
        CategoryViewHolder holder = (CategoryViewHolder) header.getTag();

        if (category != null) {
            holder.icon.setText(category.getImageName());
            holder.title.setText(category.getCategoryName().replace("+", "&"));
        } else {
            holder.icon.setText(R.string.icon_frown);
            holder.title.setText(R.string.label_no_categories);
        }
    }

    @Override
    protected boolean supportsEmptyList() {
        return true;
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
                final List<Pair<Category, List<Category>>> list = mData;
                final int count = mData.size();

                final List<Pair<Category, List<Category>>> newSection = new ArrayList<Pair<Category, List<Category>>>(count);

                for (Pair<Category, List<Category>> section : list) {

                    boolean useSection = section.first.getCategoryName().toLowerCase().contains(filterText);
                    final List<Category> sectionItems = section.second;
                    final int sectionCount = section.second.size();

                    final List<Category> newSectionItems = new ArrayList<Category>(sectionCount);

                    for (Category cat : sectionItems) {

                        if (cat.getCategoryName().toLowerCase().contains(filterText)) {
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
