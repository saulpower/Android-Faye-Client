package com.moneydesktop.finance.views.AnimatedListView;

import android.R;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.views.AccountTypeChildView;

import java.util.List;

/**
 * Wraps a ListAdapter to give it expandable list view functionality.
 * The main thing it does is add a listener to the getToggleButton
 * which expands the getExpandableView for each list item.
 *
 * @author tjerk
 * @date 6/9/12 4:41 PM
 */
public abstract class AbstractSlideExpandableListAdapter implements ListAdapter {
	private ListAdapter wrapped;
	private static Context mContext;
	private static List<AccountType> mAccountTypesFiltered;
	

	public AbstractSlideExpandableListAdapter(ListAdapter wrapped, Context context, List<AccountType> accountTypesFiltered) {
		this.wrapped = wrapped;
		this.mContext = context;
		this.mAccountTypesFiltered = accountTypesFiltered;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return wrapped.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int i) {
		return wrapped.isEnabled(i);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver dataSetObserver) {
		wrapped.registerDataSetObserver(dataSetObserver);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
		wrapped.unregisterDataSetObserver(dataSetObserver);
	}

	@Override
	public int getCount() {
		return wrapped.getCount();
	}

	@Override
	public Object getItem(int i) {
		return wrapped.getItem(i);
	}

	@Override
	public long getItemId(int i) {
		return wrapped.getItemId(i);
	}

	@Override
	public boolean hasStableIds() {
		return wrapped.hasStableIds();
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		view = wrapped.getView(position, view, viewGroup);
		enableFor(view, position);
		return view;
	}

	@Override
	public int getItemViewType(int i) {
		return wrapped.getItemViewType(i);
	}

	@Override
	public int getViewTypeCount() {
		return wrapped.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	private static View lastOpen = null;

	/**
	 * This method is used to get the Button view that should
	 * expand or collapse the Expandable View.
	 * <br/>
	 * Normally it will be implemented as:
	 * <pre>
	 * return (Button)parent.findViewById(R.id.expand_toggle_button)
	 * </pre>
	 *
	 * A listener will be attached to the button which will
	 * either expand or collapse the expandable view
	 *
	 * @see getExpandableView
	 * @param parent the list view item
	 * @return a child of parent which is a button
	 */
	public abstract View getExpandToggleButton(View parent);

	/**
	 * This method is used to get the view that will be hidden
	 * initially and expands or collapse when the ExpandToggleButton
	 * is pressed @see getExpandToggleButton
	 * <br/>
	 * Normally it will be implemented as:
	 * <pre>
	 * return parent.findViewById(R.id.expandable)
	 * </pre>
	 *
	 * @see getExpandToggleButton
	 * @param parent the list view item
	 * @return a child of parent which is a view (or often ViewGroup)
	 *  that can be collapsed and expanded
	 */
	public abstract View getExpandableView(View parent);

	public void enableFor(View parent, int position) {
		View more = getExpandToggleButton(parent);
		
		HorizontalScrollView itemToolbar = (HorizontalScrollView)getExpandableView(parent);
        AccountTypeChildView accountTypeChildView = new AccountTypeChildView(mContext, mAccountTypesFiltered.get(position).getBankAccounts(), parent);
        itemToolbar.addView(accountTypeChildView);
        
		enableFor(more, itemToolbar, position);
	}

	
	public static void enableFor(View button, final View target, final int position) {
	    button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				view.setAnimation(null);
				int type = target.getVisibility() == View.VISIBLE ? ExpandCollapseAnimation.COLLAPSE : ExpandCollapseAnimation.EXPAND;
				Animation anim = new ExpandCollapseAnimation(target, 330, type);
				((TextView)view.findViewById(com.moneydesktop.finance.R.id.account_type_group_indicator)).setText(mContext.getResources().getString(com.moneydesktop.finance.R.string.account_types_indicator_show));
				if(type == ExpandCollapseAnimation.EXPAND) {				    
					lastOpen = target;
					((TextView)view.findViewById(com.moneydesktop.finance.R.id.account_type_group_indicator)).setText(mContext.getResources().getString(com.moneydesktop.finance.R.string.account_types_indicator_hide));
				} else if(lastOpen == view) {
					lastOpen = null;
				}
				view.startAnimation(anim);
			}
		});
		// ensure the target is currently not visible
		target.setVisibility(View.GONE);
	}

}
