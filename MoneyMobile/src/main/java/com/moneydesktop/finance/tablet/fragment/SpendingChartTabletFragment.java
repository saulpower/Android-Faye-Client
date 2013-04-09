package main.java.com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import main.java.com.moneydesktop.finance.model.EventMessage.NavigationEvent;
import main.java.com.moneydesktop.finance.shared.adapter.GrowPagerAdapter.OnScrollStateChangedListener;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.views.piechart.ChartListBridge;
import main.java.com.moneydesktop.finance.views.piechart.ExpandablePieChartView;
import de.greenrobot.event.EventBus;

public class SpendingChartTabletFragment extends SummaryTabletFragment implements OnScrollStateChangedListener {

    public final String TAG = this.getClass().getSimpleName();

    private ChartListBridge mBridge;
    private ExpandablePieChartView mChart;
    private ListView mList;

    private TextView mTitle, mCategoryTitle, mBackButton;
    private TextSwitcher mTotalAmount;

    private boolean mShowing = false;
    private boolean mVisible = true;

    private Handler mHandler;

    public static SpendingChartTabletFragment newInstance(int position) {

        SpendingChartTabletFragment frag = new SpendingChartTabletFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.SPENDING_SUMMARY;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_spending_summary_view, null);

        mHandler = new Handler();

        setupView();

        configureChart(false);

        mBridge = new ChartListBridge(mActivity, mChart, mList, mTotalAmount, mBackButton);

        return mRoot;
    }

    private void setupView() {

        mChart = (ExpandablePieChartView) mRoot.findViewById(R.id.chart);
        mList = (ListView) mRoot.findViewById(R.id.list);

        mTitle = (TextView) mRoot.findViewById(R.id.title);
        mCategoryTitle = (TextView) mRoot.findViewById(R.id.category_title);
        mBackButton = (TextView) mRoot.findViewById(R.id.back_button);
        TextView total = (TextView) mRoot.findViewById(R.id.total);
        mTotalAmount = (TextSwitcher) mRoot.findViewById(R.id.total_amount);

        Fonts.applySecondaryItalicFont(mTitle, 12);
        Fonts.applyPrimaryBoldFont(mCategoryTitle, 12);
        Fonts.applyPrimaryBoldFont(total, 12);
        Fonts.applySecondaryItalicFont(mBackButton, 14);

        mCategoryTitle.setText(mCategoryTitle.getText().toString().toUpperCase());
        mTitle.setText(getTitleText());
    }

    @Override
    public void onResume() {
        super.onResume();

        configureChart(true);

        EventBus.getDefault().register(this);
        mAdapter.setOnScrollStateChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public String getTitleText() {
        return getString(R.string.title_fragment_spending_summary).toUpperCase();
    }

    @Override
    public void transitionFragment(float percent, float scale) {
        super.transitionFragment(percent, scale);

        mShowing = percent < 0.01f;
    }

    @Override
    public void onScrollStateChanged(int state) {

        if (ViewPager.SCROLL_STATE_IDLE == state && mShowing) {

            configureChart(true);

        } else if (ViewPager.SCROLL_STATE_IDLE != state) {

            configureChart(false);
        }
    }

    public void onEvent(final EventMessage.VisibilityEvent event) {

        if (event.getType() == FragmentType.DASHBOARD) {
            mVisible = event.isVisible();
            configureChart(mVisible);
        }
    }

    public void onEvent(final DatabaseSaveEvent event) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (event.didDatabaseChange() && event.getChangedClassesList().contains(Transactions.class)) {
                    mBridge.updateChart();
                }
            }
        });
    }

    public void onEvent(NavigationEvent event) {

        if (event.isShowing() != null) {

            configureChart(!event.isShowing());
            return;
        }
    }

    private void configureChart(boolean showChart) {

        if (showChart && mShowing && mActivity.isOnHome() && mVisible) {
            mChart.onResume();
        } else {
            mChart.onPause();
        }
    }
}
