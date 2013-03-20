package com.moneydesktop.finance.handset.adapter;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.shared.MenuViewHolder;
import com.moneydesktop.finance.shared.adapter.UltimateAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.UltimateListView;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MenuRightHandsetAdapter extends UltimateAdapter implements OnGroupClickListener, OnChildClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private List<Pair<Integer, List<int[]>>> mData;
    private Activity mActivity;
    private UltimateListView mListView;
    private FragmentType mCurrentFragmentType;
    
    public void configureMenu(List<Pair<Integer, List<int[]>>> data, FragmentType currentFragmentType) {
        mData = data;
        mCurrentFragmentType = currentFragmentType;
        addDefaultMenuItems();
    	notifyDataSetChanged();
    	mListView.expandAll();
    }

    public MenuRightHandsetAdapter(Activity activity, UltimateListView listView) {
        
        mActivity = activity;
        mListView = listView;
        resetMenu();
        
        mListView.setOnGroupClickListener(this);
        mListView.setOnChildClickListener(this);
    }
    
    public void resetMenu() {
    	configureMenu(new ArrayList<Pair<Integer, List<int[]>>>(), mCurrentFragmentType);
    }
    
    private void addDefaultMenuItems() {

    	List<int[]> items = new ArrayList<int[]>();
    	items.add(new int[] {R.string.nav_icon_feedback, R.string.label_feedback_menu });
    	
    	mData.add(new Pair<Integer, List<int[]>>(R.string.menu_help, items));
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        
        return mData.get(groupPosition).second.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (new String(groupPosition + "-" + childPosition)).hashCode();
    }

    @Override
    public View getItemView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        
    	MenuViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.handset_menu_right_item, parent, false);
            
            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (MenuViewHolder) cell.getTag();
        }
        
        int[] item = (int[]) getChild(groupPosition, childPosition);
        
        if (item != null) {
            viewHolder.icon.setText(item[0]);
            viewHolder.title.setText(mActivity.getString(item[1]).toUpperCase());
        }
        
        return cell;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mData.get(groupPosition).second.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mData.get(groupPosition).first;
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return (new String("section" + groupPosition)).hashCode();
    }

    @Override
    public View getSectionView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        
        final MenuViewHolder viewHolder;
        View cell = convertView;
        
        if (cell == null) {
            
            cell = mActivity.getLayoutInflater().inflate(R.layout.handset_menu_right_header, parent, false);

            viewHolder = createViewHolder(cell);
            
        } else {
            
            viewHolder = (MenuViewHolder) cell.getTag();
        }
        
        String title = mActivity.getString((Integer) getGroup(groupPosition));
        viewHolder.title.setText(title.toUpperCase());
        
        return cell;
    }
    
    private MenuViewHolder createViewHolder(View cell) {

    	MenuViewHolder viewHolder = new MenuViewHolder();

        viewHolder.title = (TextView) cell.findViewById(R.id.title);
        viewHolder.icon = (TextView) cell.findViewById(R.id.icon);
        
        applyFonts(viewHolder);
        
        cell.setTag(viewHolder);
        
        return viewHolder;
    }
    
    private void applyFonts(MenuViewHolder viewHolder) {

        Fonts.applyPrimaryFont(viewHolder.title, 8);
        Fonts.applyNavIconFont(viewHolder.icon, 24);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void configureHeader(View header, int section) {

        MenuViewHolder holder = (MenuViewHolder) header.getTag();
        holder.title.setText(((String) getGroup(section)).toUpperCase());
    }

    @Override
    protected void loadSection(final int section) {}

    @Override
    protected boolean isSectionLoadable(int section) {
        return false;
    }

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		
		if (groupPosition == (getGroupCount() - 1)) {
			EventBus.getDefault().post(new EventMessage().new FeedbackEvent());
		} else {
			EventBus.getDefault().post(new EventMessage().new MenuEvent(groupPosition, childPosition, mCurrentFragmentType));
		}
		
		return true;
	}
}
