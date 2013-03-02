package com.moneydesktop.finance.views.chart;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Pair;

import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.util.DateRange;

public class CategoryPieChartAdapter extends BaseExpandablePieChartAdapter {
    
    public final String TAG = this.getClass().getSimpleName();
    
	private Context mContext;

	private List<Pair<Category, List<Category>>> mCategories;
	
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
            	other.setCategoryName("Other");
            	other.setParentPercent(0f);
            	
            	float total = 0f;
            	int count = -1;
            	
            	List<Pair<Category, List<Category>>> categories = Category.loadCategoryData(true);

            	while (total == 0f) {
            		
            		for (Pair<Category, List<Category>> parent : categories) {
                		total += Category.getTotalForCategory(parent.first, range);
                	}
            		
            		if (total == 0f) {
            			total = 0f;
            			range.addMonthsToStart(count);
            			count--;
            		}
            	}
            	
        		for (Pair<Category, List<Category>> parent : categories) {
        			
        			float categoryTotal = Category.getTotalForCategory(parent.first, range);
        			float categoryPercent = categoryTotal / total;
        			
        			parent.first.setParentPercent(categoryPercent);
        			parent.second.add(parent.first);
        			
        			// Compile all categories under 3% of the total into
        			// an "Other" category
        			if (parent.second.size() == 0 || (categoryPercent <= 0.03f && categoryPercent > 0)) {
        				
        				others.add(parent.first);
        				other.setParentPercent(other.getParentPercent() + categoryPercent);
        				
        				continue;
        				
        			} else if (categoryPercent == 0) {
        				
        				continue;
        			}
        			
        			List<Category> remove = new ArrayList<Category>();
        			
        			for (Category category : parent.second) {
        				
        				float childPercent = Category.getTotalForChildCategory(category, range) / categoryTotal;
        				category.setChildPercent(childPercent);
        				
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
		
		return category.getChildPercent();
	}

	@Override
	public float getGroupAmount(int groupPosition) {
		
		Category category = (Category) getGroup(groupPosition);
		
		return category.getParentPercent();
	}

	@Override
	public PieSliceDrawable getChildSlice(PieChartView parent, PieSliceDrawable convertDrawable, int groupPosition, int childPosition, float offset) {
		
		PieSliceDrawable sliceDrawable = convertDrawable;
		
		if (sliceDrawable == null) {
			sliceDrawable = new PieSliceDrawable(parent, mContext);
		}

		Float percent = getChildAmount(groupPosition, childPosition);
		
		int color = adjustColor(groupPosition, childPosition);
		
		if (groupPosition == getGroupCount() - 1) {
			color = getRandomColor(getGroupCount() + childPosition);
		}
		
		sliceDrawable.setDegreeOffset(offset);
		sliceDrawable.setPercent(percent);
		sliceDrawable.setSliceColor(color);
		
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
		sliceDrawable.setSliceColor(getRandomColor(groupPosition));
		
		return sliceDrawable;
	}
	
	private int getRandomColor(int position) {
		
		position = position > 15 ? position % 16 : position;
		
		return mContext.getResources().getColor(Constant.RANDOM_COLORS[position]);
	}
	
	private int adjustColor(int groupPosition, int childPosition) {
		
		int parentColor = getRandomColor(groupPosition);

		if (childPosition == 0) {
			return parentColor;
		}
		
	     float[] pixelHSV = new float[3];
		Color.colorToHSV(parentColor, pixelHSV);
		pixelHSV[2] -= (0.08f * childPosition);
		
		return Color.HSVToColor(pixelHSV);
	}
}
