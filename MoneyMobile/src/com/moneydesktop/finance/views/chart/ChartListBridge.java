package com.moneydesktop.finance.views.chart;

import java.text.DecimalFormat;

import android.app.Activity;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.shared.CategoryViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView.OnExpandablePieChartChangeListener;
import com.moneydesktop.finance.views.chart.ExpandablePieChartView.OnExpandablePieChartInfoClickListener;

public class ChartListBridge extends BaseAdapter implements OnExpandablePieChartChangeListener, OnExpandablePieChartInfoClickListener, OnItemClickListener {

    public final String TAG = this.getClass().getSimpleName();
    
	private DecimalFormat mFormatter = new DecimalFormat("$###,##0.00");
	
	private ExpandablePieChartView mChart;
	private ListView mList;
	private TextView mTotal;
	
	private CategoryPieChartAdapter mAdapter;
	private Activity mActivity;
	
	private Animation mIn, mOut;
	
	private boolean mInit = false;
	
	public ChartListBridge(Activity activity, ExpandablePieChartView chart, ListView list, TextView total) {
		
		mActivity = activity;
		
		mAdapter = new CategoryPieChartAdapter(mActivity);

		mTotal = total;
		
		mChart = chart;
		mChart.setExpandablePieChartInfoClickListener(this);
		mChart.setExpandableChartChangeListener(this);
		mChart.setAdapter(mAdapter);
		
		mList = list;
		mList.setOnItemClickListener(this);
		mList.setAdapter(this);
		
		loadAnimations();
	}
	
	private void loadAnimations() {
		
		mIn = AnimationUtils.loadAnimation(mActivity, R.anim.fade_in_fast);
		mOut = AnimationUtils.loadAnimation(mActivity, R.anim.fade_out_fast);
		mOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mList.setVisibility(View.INVISIBLE);
				notifyDataSetChanged();
				mList.setVisibility(View.VISIBLE);
				mList.startAnimation(mIn);
			}
		});
	}
	
	private void updateTotal(float amount) {

		String total = mActivity.getString(R.string.label_total);
		String text = total + " " + mFormatter.format(amount);
		SpannableString textSpan = new SpannableString(text);
		textSpan.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE), 
				total.length(), text.length(), 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mTotal.setText(textSpan);
	}
	
	/**
	 * ListView Adapter
	 */
	
	@Override
	public int getCount() {
		
		int count = 0;
		
		if (mChart.isExpanded()) {
			count = mAdapter.getChildrenCount(mChart.getSelectedGroup());
		} else {
			count = mAdapter.getGroupCount();
		}
		
		return count;
	}
	
	@Override
	public Object getItem(int position) {
		
		Object object = null;
		
		if (mChart.isExpanded()) {
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
        
        if (mChart.isExpanded()) {
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
		
		if (!mInit) {
			mInit = true;
			updateTotal(mAdapter.getTotal());
		}
	}

	@Override
	public void onChildChanged(int groupPosition, int childPosition) {
		Log.i(TAG, "onChildChanged");
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		mList.startAnimation(mOut);
		updateTotal(mAdapter.getGroupTotal(groupPosition));
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		mList.startAnimation(mOut);
		updateTotal(mAdapter.getTotal());
	}

	/**
	 * ListView Item Click Listener
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		if (mChart.isExpanded()) {
			mChart.setChildSelection(position);
		} else {
			mChart.setGroupSelection(position);
		}
	}
}
