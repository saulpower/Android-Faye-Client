package com.moneydesktop.finance.tablet.adapter;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.adapters.AmazingAdapter;
import com.moneydesktop.finance.shared.FilterViewHolder;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.CaretView;

import java.util.ArrayList;
import java.util.List;

public class FiltersTabletAdapter extends AmazingAdapter {
    
    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<String, List<FilterViewHolder>>> mSections;
	private List<FilterViewHolder> mAllFilters = new ArrayList<FilterViewHolder>();
	private AmazingListView mListView;

	private Activity mActivity;

	public FiltersTabletAdapter(Activity activity, AmazingListView listView, List<Pair<String, List<FilterViewHolder>>> sections) {
		this.mActivity = activity;
		this.mListView = listView;
        this.mSections = sections;

        for (Pair<String, List<FilterViewHolder>> pairs : sections)
            this.mAllFilters.addAll(pairs.second);
        
        notifyNoMorePages();
	}

	public int getCount() {

        int res = 0;

        for (int i = 0; i < mSections.size(); i++)
            res += mSections.get(i).second.size();

        return res;
	}

	public FilterViewHolder getItem(int position) {
		
		return mAllFilters.get(position);
	}

    public long getItemId(int position) {
		return position;
	}

	@Override
	protected void onNextPageRequested(int page) {
	}

	@Override
	protected void bindSectionHeader(View view, int position, boolean displaySectionHeader) {

        if (displaySectionHeader) {

            RelativeLayout root = (RelativeLayout) view.findViewById(R.id.header);
            root.setVisibility(View.VISIBLE);
            
            TextView sectionTitle = (TextView) view.findViewById(R.id.header_text);
            sectionTitle.setText(getSections()[getSectionForPosition(position)]);
            Fonts.applyPrimaryBoldFont(sectionTitle, 12);

        } else {

            RelativeLayout root = (RelativeLayout) view.findViewById(R.id.header);
            root.setVisibility(View.GONE);
        }
	}

	@Override
	public View getAmazingView(int position, View convertView, ViewGroup parent) {
		
	    FilterViewHolder viewHolder;
		View res = convertView;
		
		if (res == null) {
		    
			res = mActivity.getLayoutInflater().inflate(R.layout.tablet_filter_item, null);

			viewHolder = new FilterViewHolder();
			
	        viewHolder.title = (TextView) res.findViewById(R.id.title);
	        viewHolder.subTitle = (TextView) res.findViewById(R.id.subtext);
	        viewHolder.caret = (CaretView) res.findViewById(R.id.caret);
	        
	        res.setTag(viewHolder);
	        
	        applyFonts(viewHolder);
	        
		} else {
		    
		    viewHolder = (FilterViewHolder) res.getTag();
		}

		FilterViewHolder filter = getItem(position);
		
		if (filter != null) {
		    viewHolder.title.setText(filter.text);
		    viewHolder.subTitle.setText(filter.subText);
		}

		return res;
	}
	
	private void applyFonts(FilterViewHolder viewHolder) {

        Fonts.applyPrimarySemiBoldFont(viewHolder.title, 12);
        Fonts.applyPrimarySemiBoldFont(viewHolder.subTitle, 10);
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {

        TextView sectionHeader = (TextView) header.findViewById(R.id.header_text);
        sectionHeader.setText(getSections()[getSectionForPosition(position)]);
        Fonts.applyPrimaryBoldFont(sectionHeader, 12);
	}

	@Override
	public int getPositionForSection(int section) {

        if (section < 0)
            section = 0;
        if (section >= mSections.size())
            section = mSections.size() - 1;
        int c = 0;

        for (int i = 0; i < mSections.size(); i++) {

            if (section == i)
                return c;

            c += mSections.get(i).second.size();
        }

        return 0;
	}

	@Override
	public int getSectionForPosition(int position) {

        int c = 0;

        for (int i = 0; i < mSections.size(); i++) {

            if (position >= c && position < c + mSections.get(i).second.size())
                return i;

            c += mSections.get(i).second.size();
        }

        return -1;
	}

	@Override
	public String[] getSections() {

        String[] res = new String[mSections.size()];

        for (int i = 0; i < mSections.size(); i++)
            res[i] = mSections.get(i).first;

        return res;
	}
}
