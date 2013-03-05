package com.moneydesktop.finance.views.chart;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.shared.CategoryViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView.OnExpandablePieChartChangeListener;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView.OnExpandablePieChartInfoClickListener;

@SuppressLint("NewApi")
public class ChartListBridge extends BaseAdapter implements OnExpandablePieChartChangeListener, OnExpandablePieChartInfoClickListener, OnItemClickListener, ViewFactory {

    public final String TAG = this.getClass().getSimpleName();
    
	private DecimalFormat mFormatter = new DecimalFormat("$###,##0.00");
	
	private ExpandablePieChartView mChart;
	private ListView mList;
	private TextSwitcher mTotal;
	private TextView mBackButton;
	
	private CategoryPieChartAdapter mAdapter;
	private Activity mActivity;
	
	private boolean mExpanded = false;
	private int mSelection = 0;
	
	private Animation mIn, mOut, mBackIn, mBackOut;
	
	private boolean mInit = false;
	
	public ChartListBridge(Activity activity, ExpandablePieChartView chart, ListView list, TextSwitcher total, TextView backButton) {
		
		mActivity = activity;
		
		mAdapter = new CategoryPieChartAdapter(mActivity);

		mTotal = total;
		mTotal.setFactory(this);
		mTotal.setInAnimation(activity, R.anim.fade_in_fast);
		mTotal.setOutAnimation(activity, R.anim.fade_out_fast);
		
		mChart = chart;
		mChart.setExpandablePieChartInfoClickListener(this);
		mChart.setExpandableChartChangeListener(this);
		mChart.setAdapter(mAdapter);
		
		mList = list;
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mList.setItemsCanFocus(true);
		mList.setOnItemClickListener(this);
		mList.setAdapter(this);
		
		mBackButton = backButton;
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (mChart.isExpanded()) {

					mChart.toggleGroup();
				}
			}
		});
		
		loadAnimations();
	}
	
	private void loadAnimations() {
		
		mIn = AnimationUtils.loadAnimation(mActivity, R.anim.fade_in_fast);
		mIn.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				if (mChart.isExpanded()) {

					mBackButton.setVisibility(View.VISIBLE);
					mBackButton.startAnimation(mBackIn);
				}
			}
		});
		mOut = AnimationUtils.loadAnimation(mActivity, R.anim.fade_out_fast);
		mOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
				mList.setVisibility(View.INVISIBLE);
				
				mExpanded = mChart.isExpanded();
				notifyDataSetChanged();

				// Set selection to selected item
				mList.setSelection(mSelection);
				mList.setItemChecked(mSelection, true);
				
				mList.setVisibility(View.VISIBLE);
				mList.startAnimation(mIn);
			}
		});
		
		mBackIn = AnimationUtils.loadAnimation(mActivity, R.anim.in_left_fast);
		mBackOut = AnimationUtils.loadAnimation(mActivity, R.anim.out_left_fast);
		mBackOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mBackButton.setVisibility(View.GONE);
				mList.startAnimation(mOut);
			}
		});
	}
	
	private void updateTotal(float amount) {
		
		mTotal.setText(mFormatter.format(amount));
	}
	
	/**
	 * ListView Adapter
	 */
	
	@Override
	public int getCount() {
		int count = 0;
		
		if (mExpanded) {
			count = mAdapter.getChildrenCount(mChart.getSelectedGroup());
		} else {
			count = mAdapter.getGroupCount();
		}
		
		return count;
	}
	
	@Override
	public Object getItem(int position) {
		
		Object object = null;
		
		if (mExpanded) {
			object = mAdapter.getChild(mChart.getSelectedGroup(), position);
		} else {
			object = mAdapter.getGroup(position);
		}
		
		return object;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		CategoryViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.spending_category_item, parent, false);
            
            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (CategoryViewHolder) cell.getTag();
        }

        Category category = (Category) getItem(position);
        
        float total = 0;
        int color;
        
        if (mExpanded) {
        	total = category.getChildTotal();
        	color = mAdapter.getChildColor(mChart.getSelectedGroup(), position);
        } else {
        	total = category.getParentTotal();
        	color = mAdapter.getGroupColor(position);
        }
        
        viewHolder.title.setText(category.getCategoryName());
        viewHolder.amount.setText(mFormatter.format(total));
        viewHolder.color.setBackgroundColor(color);
        
		return cell;
	}
	
	private CategoryViewHolder createViewHolder(View cell) {

        final CategoryViewHolder viewHolder = new CategoryViewHolder();

        viewHolder.item = (RelativeLayout) cell.findViewById(R.id.item);
        viewHolder.color = cell.findViewById(R.id.color);
        viewHolder.title = (TextView) cell.findViewById(R.id.title);
        viewHolder.amount = (TextView) cell.findViewById(R.id.amount);
        
        Fonts.applyPrimaryFont(viewHolder.title, 12);
        Fonts.applyPrimaryFont(viewHolder.amount, 12);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
	}
	
	/**
	 * Expandable Pie Chart Listeners
	 */

	@Override
	public void onInfoClicked(int groupPosition, int childPosition) {
		Log.i(TAG, "onInfoClicked");
	}

	@Override
	public void onGroupChanged(int groupPosition) {

		mList.setItemChecked(groupPosition, true);
		mList.setSelection(groupPosition);
		
		if (!mInit) {
			mInit = true;
			updateTotal(mAdapter.getTotal());
		}
	}

	@Override
	public void onChildChanged(int groupPosition, int childPosition) {
		mList.setSelection(childPosition);
		mList.setItemChecked(childPosition, true);
	}

	@Override
	public void onGroupExpanded(int groupPosition, int childPosition) {
		
		mSelection = childPosition;
		
		mList.startAnimation(mOut);
		updateTotal(mAdapter.getGroupTotal(groupPosition));
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {

		mSelection = groupPosition;
		
		mBackButton.startAnimation(mBackOut);
		updateTotal(mAdapter.getTotal());
	}

	/**
	 * ListView Item Click Listener
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		if (mChart.isExpanded()) {
			
			if (mChart.getSelectedChild() == position) {
				mChart.toggleGroup();
				return;
			}
			
			mChart.setChildSelection(position);
			
		} else {
			
			if (mChart.getSelectedGroup() == position) {
				mChart.toggleGroup();
				return;
			}
			
			mChart.setGroupSelection(position);
		}
	}

	/**
	 * TextSwitcher factory
	 */
	
	@Override
	public View makeView() {
		
		TextView textView = new TextView(mActivity);
	    FrameLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    textView.setLayoutParams(params);
	    textView.setGravity(Gravity.RIGHT);
	    textView.setTextColor(Color.WHITE);
	    textView.setBackgroundColor(Color.TRANSPARENT);

		Fonts.applyPrimaryBoldFont(textView, 12);
		
		return textView;
	}
}
