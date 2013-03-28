package com.moneydesktop.finance.views.barchart;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.CategoryViewHolder;
import com.moneydesktop.finance.shared.activity.BaseActivity;

@SuppressLint("NewApi")
public class TransactionsListAdapter extends BaseAdapter implements OnItemClickListener {

    public final String TAG = this.getClass().getSimpleName();

	private BarChartView mChart;
	private ListView mList;
	private int mSelection = 0;	private Animation mIn, mOut, mBackOut;
    private BaseActivity mActivity;

	public TransactionsListAdapter(BaseActivity activity, BarChartView chart, ListView list) {
		this(activity, chart);

        mActivity = activity;

		mList = list;
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mList.setItemsCanFocus(true);
		mList.setOnItemClickListener(this);
		mList.setAdapter(this);

		loadAnimations();
	}

	public TransactionsListAdapter(BaseActivity activity, BarChartView chart) {
       	mActivity = activity;
		mChart = chart;
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

				// Set selection to selected item
				mList.setSelection(mSelection);
				mList.setItemChecked(mSelection, true);

				// Animate ListView in
				mList.setVisibility(View.VISIBLE);
				mList.startAnimation(mIn);
			}
		});

		mBackOut = AnimationUtils.loadAnimation(mActivity, R.anim.out_left_fast);
		mBackOut.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mList.startAnimation(mOut);
            }
        });
	}

	@Override
	public int getCount() {
		int count = 0;
		return count;
	}

    @Override
    public Object getItem(int position) {
        return null;
    }

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		CategoryViewHolder viewHolder;
        View cell = convertView;

		return cell;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

}