package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Pair;

import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.Transactions;

public class CategoryPieChartAdapter extends BaseExpandablePieChartAdapter {
	
	private AsyncTask<Integer, Void, List<Pair<Float, List<Float>>>> mBackgroundTask;

	private List<Pair<Float, List<Float>>> mCategories;
	
	public CategoryPieChartAdapter() {
		mCategories = new ArrayList<Pair<Float, List<Float>>>();
		loadCategories();
	}
	
	private void loadCategories() {
		
		mBackgroundTask = new AsyncTask<Integer, Void, List<Pair<Float, List<Float>>>>() {
            
            @Override
            protected List<Pair<Float, List<Float>>> doInBackground(Integer... params) {
            	
            	List<Pair<Float, List<Float>>> percents = new ArrayList<Pair<Float, List<Float>>>();
            	List<Float> others = new ArrayList<Float>();
            	float othersTotal = 0;
            	
            	float total = Transactions.getAllTransactionsTotal();
            	
            	List<Pair<Category, List<Category>>> categories = Category.loadCategoryData();
                
        		for (Pair<Category, List<Category>> parent : categories) {
        			
        			float categoryTotal = Category.getTotalForCategory(parent.first);
        			float categoryPercent = categoryTotal / total;
        			
        			// Compile all categories under 6% of the total into
        			// an "Other" category
        			if (categoryPercent < 0.06f) {
        				
        				others.add(categoryPercent);
        				othersTotal += categoryPercent;
        				
        				continue;
        			}
        			
        			List<Float> children = new ArrayList<Float>();
        			
        			for (Category category : parent.second) {
        				
        				float childPercent = Category.getTotalForCategory(category) / categoryTotal;
        				children.add(childPercent);
        			}
        			
        			percents.add(new Pair<Float, List<Float>>(categoryPercent, children));
        		}
        		
    			percents.add(new Pair<Float, List<Float>>(othersTotal, others));
        		
                return percents;
            }

            @Override
            protected void onPostExecute(List<Pair<Float, List<Float>>> categories) {

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
		
		float percent = (Float) getChild(groupPosition, childPosition);
		
		return percent;
	}

	@Override
	public float getGroupAmount(int groupPosition) {
		
		float percent = (Float) getGroup(groupPosition);
		
		return percent;
	}
}
