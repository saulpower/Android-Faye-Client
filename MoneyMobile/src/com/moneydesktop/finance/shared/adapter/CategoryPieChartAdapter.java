package com.moneydesktop.finance.shared.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.util.DateRange;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.chart.BaseExpandablePieChartAdapter;
import com.moneydesktop.finance.views.chart.InfoDrawable;
import com.moneydesktop.finance.views.chart.PieChartView;
import com.moneydesktop.finance.views.chart.PieSliceDrawable;

public class CategoryPieChartAdapter extends BaseExpandablePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private DecimalFormat mFormatter = new DecimalFormat("$###,##0.00");
    
	private Context mContext;
	
	private float mTotal = 0f;

	private List<Pair<Category, List<Category>>> mCategories;
	
	public float getTotal() {
		return mTotal;
	}

	public CategoryPieChartAdapter(Context context) {
		
		mContext = context;
		
		mCategories = new ArrayList<Pair<Category, List<Category>>>();
		loadCategories();
	}
	
	private void loadCategories() {
		
		new AsyncTask<Integer, Void, List<Pair<Category, List<Category>>>>() {
            
            @Override
            protected List<Pair<Category, List<Category>>> doInBackground(Integer... params) {
            	
            	DateRange range = DateRange.forCurrentMonth();
            	
            	List<Pair<Category, List<Category>>> percents = new ArrayList<Pair<Category, List<Category>>>();
            	
            	List<Category> others = new ArrayList<Category>();
            	
            	Category other = new Category();
            	other.setCategoryName(ApplicationContext.getContext().getString(R.string.label_others));
            	other.setParentPercent(0f);
            	
            	int count = -1;
            	
            	List<Pair<Category, List<Category>>> categories = Category.loadCategoryData(true);

            	while (mTotal == 0f) {
            		
            		for (Pair<Category, List<Category>> parent : categories) {
            			mTotal += Category.getTotalForCategory(parent.first, range);
                	}
            		
            		if (mTotal == 0f) {
            			mTotal = 0f;
            			range.addMonthsToStart(count);
            			count--;
            		}
            	}
            	
        		for (Pair<Category, List<Category>> parent : categories) {
        			
        			float categoryTotal = Category.getTotalForCategory(parent.first, range);
        			float categoryPercent = categoryTotal / mTotal;
        			
        			parent.first.setParentPercent(categoryPercent);
        			parent.first.setParentTotal(categoryTotal);
        			parent.second.add(parent.first);
        			
        			// Compile all categories under 3% of the total into
        			// an "Other" category
        			if (parent.second.size() == 0 || (categoryPercent <= 0.03f && categoryPercent > 0)) {
        				
        				others.add(parent.first);
        				other.setParentPercent(other.getParentPercent() + categoryPercent);
        				other.setParentTotal(other.getParentTotal() + categoryTotal);
        				
        				continue;
        				
        			} else if (categoryPercent == 0) {
        				
        				continue;
        			}
        			
        			List<Category> remove = new ArrayList<Category>();
        			
        			for (Category category : parent.second) {
        				
        				float childTotal = Category.getTotalForChildCategory(category, range);
        				float childPercent = childTotal / categoryTotal;
        				category.setChildPercent(childPercent);
        				category.setChildTotal(childTotal);
        				
        				if (childPercent == 0) {
        					remove.add(category);
        				}
        			}
        			
        			// remove any category with 0%
        			parent.second.removeAll(remove);
        			
        			percents.add(new Pair<Category, List<Category>>(parent.first, parent.second));
        		}
        		
        		for (Category category : others) {
    				
    				float childPercent = category.getParentPercent() / other.getParentPercent();
    				category.setChildPercent(childPercent);
    				category.setChildTotal(category.getParentTotal());
    			}
        		
    			percents.add(new Pair<Category, List<Category>>(other, others));
        		
                return percents;
            }

            @Override
            protected void onPostExecute(List<Pair<Category, List<Category>>> categories) {

                if (isCancelled()) {
                    return;
                }

                mCategories.clear();
                mCategories.addAll(categories);
				
                notifyDataSetChanged();
            }

        }.execute();
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		
		if (getGroupCount() <= groupPosition || getChildrenCount(groupPosition) <= childPosition) return null;
		
		return mCategories.get(groupPosition).second.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		if (mCategories.size() <= groupPosition) return 0;
		
		return mCategories.get(groupPosition).second.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		if (getGroupCount() <= groupPosition) return null;
		
		return mCategories.get(groupPosition).first;
	}

	@Override
	public int getGroupCount() {
		return mCategories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
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
	public float getChildAmount(int groupPosition, int childPosition) {
		
		Category category = (Category) getChild(groupPosition, childPosition);

		if (category == null) return 0f;
		
		return category.getChildPercent();
	}

	@Override
	public float getGroupAmount(int groupPosition) {
		
		Category category = (Category) getGroup(groupPosition);
		
		if (category == null) return 0f;
		
		return category.getParentPercent();
	}
	
	public float getGroupTotal(int groupPosition) {
		
		Category category = (Category) getGroup(groupPosition);
		
		if (category == null) return 0f;
		
		return category.getParentTotal();
	}

	@Override
	public PieSliceDrawable getChildSlice(PieChartView parent, PieSliceDrawable convertDrawable, int groupPosition, int childPosition, float offset) {
		
		PieSliceDrawable sliceDrawable = convertDrawable;
		
		if (sliceDrawable == null) {
			sliceDrawable = new PieSliceDrawable(parent, mContext);
		}

		Float percent = getChildAmount(groupPosition, childPosition);
		
		sliceDrawable.setDegreeOffset(offset);
		sliceDrawable.setPercent(percent);
		sliceDrawable.setSliceColor(getChildColor(groupPosition, childPosition));
		
		return sliceDrawable;
	}

	@Override
	public PieSliceDrawable getGroupSlice(PieChartView parent, PieSliceDrawable convertDrawable, int groupPosition, float offset) {

		PieSliceDrawable sliceDrawable = convertDrawable;
		
		if (sliceDrawable == null) {
			sliceDrawable = new PieSliceDrawable(parent, mContext);
		}

		Float percent = getGroupAmount(groupPosition);
		
		sliceDrawable.setDegreeOffset(offset);
		sliceDrawable.setPercent(percent);
		sliceDrawable.setSliceColor(getGroupColor(groupPosition));
		
		return sliceDrawable;
	}

	@Override
	public void configureGroupInfo(InfoDrawable info, PieSliceDrawable slice, int groupPosition) {

		Category cat = (Category) getGroup(groupPosition);
		
		info.animateTransition(mFormatter.format(cat.getParentTotal()), slice.getSliceColor(), cat.getCategoryName());
	}

	@Override
	public void configureChildInfo(InfoDrawable info, PieSliceDrawable slice, int groupPosition, int childPosition) {

		Category cat = (Category) getChild(groupPosition, childPosition);
		
		info.animateTransition(mFormatter.format(cat.getChildTotal()), slice.getSliceColor(), cat.getCategoryName());
	}

	@Override
	public int getChildColor(int groupPosition, int childPosition) {

		int color = UiUtils.getAdjustedColor(groupPosition, childPosition);
		
		if (groupPosition == getGroupCount() - 1) {
			color = UiUtils.getRandomColor(getGroupCount() + childPosition);
		}
		
		return color;
	}

	@Override
	public int getGroupColor(int groupPosition) {

		return UiUtils.getRandomColor(groupPosition);
	}
}
