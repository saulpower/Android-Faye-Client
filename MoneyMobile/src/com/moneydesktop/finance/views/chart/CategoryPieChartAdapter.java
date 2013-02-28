package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Pair;

import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DateRange;

public class CategoryPieChartAdapter extends BaseExpandablePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();
	
	private AsyncTask<Integer, Void, List<Pair<Category, List<Category>>>> mBackgroundTask;

	private List<Pair<Category, List<Category>>> mCategories;
	
	public CategoryPieChartAdapter() {
		mCategories = new ArrayList<Pair<Category, List<Category>>>();
		loadCategories();
	}
	
	private void loadCategories() {
		
		mBackgroundTask = new AsyncTask<Integer, Void, List<Pair<Category, List<Category>>>>() {
            
            @Override
            protected List<Pair<Category, List<Category>>> doInBackground(Integer... params) {
            	
            	DateRange range = DateRange.forCurrentMonth();
            	
            	List<Pair<Category, List<Category>>> percents = new ArrayList<Pair<Category, List<Category>>>();
            	
            	List<Category> others = new ArrayList<Category>();
            	
            	Category other = new Category();
            	other.setCategoryName("Other");
            	other.setPercent(0f);
            	
            	float total = Transactions.getTransactionsTotal(Constant.QUERY_SPENDING_TOTAL, range);
            	
            	List<Pair<Category, List<Category>>> categories = Category.loadCategoryData(true);
                
        		for (Pair<Category, List<Category>> parent : categories) {
        			
        			float categoryTotal = Category.getTotalForCategory(parent.first, range);
        			float categoryPercent = categoryTotal / total;
        			
        			parent.first.setPercent(categoryPercent);
        			
        			// Compile all categories under 3% of the total into
        			// an "Other" category
        			if (categoryPercent <= 0.03f && categoryPercent > 0) {
        				
        				others.add(parent.first);
        				other.setPercent(other.getPercent() + categoryPercent);
        				
        				continue;
        				
        			} else if (categoryPercent == 0) {
        				
        				continue;
        			}
        			
        			for (Category category : parent.second) {
        				
        				float childPercent = Category.getTotalForCategory(category, range) / categoryTotal;
        				category.setPercent(childPercent);
        			}
        			
        			percents.add(new Pair<Category, List<Category>>(parent.first, parent.second));
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
				
                notifyDataSetInvalidated();
            }

        }.execute();
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mCategories.get(groupPosition).second.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mCategories.get(groupPosition).second.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
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
		
		return category.getPercent();
	}

	@Override
	public float getGroupAmount(int groupPosition) {
		
		Category category = (Category) getGroup(groupPosition);
		
		return category.getPercent();
	}
}
