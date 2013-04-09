package main.java.com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.model.EventMessage.ChartImageEvent;
import main.java.com.moneydesktop.finance.shared.fragment.GrowFragment;
import main.java.com.moneydesktop.finance.util.Fonts;
import de.greenrobot.event.EventBus;

public class SpendingChartSummaryHandsetFragment extends GrowFragment {

    public final String TAG = this.getClass().getSimpleName();
    private ImageView mChartImage;
    private TextView mTitle;

    public ImageView getChartImage() {
        return mChartImage;
    }

    public void setChartImage(ImageView mChartImage) {
        this.mChartImage = mChartImage;
    }

    public static SpendingChartSummaryHandsetFragment newInstance(int position) {

        SpendingChartSummaryHandsetFragment fragment = new SpendingChartSummaryHandsetFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.SPENDING_SUMMARY;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.handset_spending_summary_view, null);

        mTitle = (TextView) mRoot.findViewById(R.id.title);
        Fonts.applySecondaryItalicFont(mTitle, 8);

        mChartImage = (ImageView) mRoot.findViewById(R.id.chart_image);

        mRoot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mActivity.showFragment(FragmentType.SPENDING, true);
            }
        });

        return mRoot;
    }

    public void onEvent(ChartImageEvent event) {

        if (mChartImage != null) {
            mChartImage.setImageBitmap(event.getImage());
        }
    }

    @Override
    public String getFragmentTitle() {
        return null;
    }
}
