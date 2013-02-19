
package com.moneydesktop.finance.tablet.fragment;

import android.annotation.TargetApi;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.shared.fragment.GrowFragment;
import com.moneydesktop.finance.tablet.activity.DashboardTabletActivity;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.VerticalTextView;

@TargetApi(11)
public abstract class SummaryTabletFragment extends GrowFragment {

    public final String TAG = this.getClass().getSimpleName();

    private LinearLayout mLeft, mRight;
    private VerticalTextView mLeftText, mRightText;

    public abstract String getTitleText();
    
    public int getPosition() {
        return getArguments().getInt("position", -1);
    }
    
    public void setPosition(int position) {
        getArguments().putInt("position", position);
    }

    @Override
    protected void setupViews() {
    	super.setupViews();
    	
        mLeft = (LinearLayout) mRoot.findViewById(R.id.left);
        mRight = (LinearLayout) mRoot.findViewById(R.id.right);
        mLeftText = (VerticalTextView) mRoot.findViewById(R.id.title_left);
        mRightText = (VerticalTextView) mRoot.findViewById(R.id.title_right);

        applyFonts();
    }
    
	@Override
	protected void configureView() {
		super.configureView();

		mLeftText.setText(getTitleText().toUpperCase());
		mRightText.setText(getTitleText().toUpperCase());

		mLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				((DashboardTabletActivity) mActivity).showNextPage();
			}
		});

		mRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				((DashboardTabletActivity) mActivity).showPrevPage();
			}
		});
	}
    
    private void applyFonts() {
        Fonts.applyPrimaryBoldFont(mLeftText, 12);
        Fonts.applyPrimaryBoldFont(mRightText, 12);
    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
