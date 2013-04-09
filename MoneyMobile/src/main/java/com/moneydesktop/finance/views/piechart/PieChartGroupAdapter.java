package main.java.com.moneydesktop.finance.views.piechart;

public class PieChartGroupAdapter extends BasePieChartAdapter {

    public final String TAG = this.getClass().getSimpleName();

    private BaseExpandablePieChartAdapter mExpandableAdapter;

    public PieChartGroupAdapter(BaseExpandablePieChartAdapter expandableAdapter) {
        mExpandableAdapter = expandableAdapter;
    }

    @Override
    public int getCount() {
        return mExpandableAdapter.getGroupCount();
    }

    @Override
    public Object getItem(int position) {
        return mExpandableAdapter.getGroup(position);
    }

    @Override
    public PieSliceDrawable getSlice(PieChartView parent, PieSliceDrawable convertDrawable, int position, float offset) {
        return mExpandableAdapter.getGroupSlice(parent, convertDrawable, position, offset);
    }

    @Override
    public float getPercent(int position) {
        return mExpandableAdapter.getGroupAmount(position);
    }
}
