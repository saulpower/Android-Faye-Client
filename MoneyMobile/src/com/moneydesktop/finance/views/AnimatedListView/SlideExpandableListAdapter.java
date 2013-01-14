package com.moneydesktop.finance.views.AnimatedListView;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.views.AccountTypeChildView;

import java.util.List;


public class SlideExpandableListAdapter extends AbstractSlideExpandableListAdapter {
	private int toggle_button_id;
    private int expandable_view_id;
    private static List<AccountType> mAccountTypesFiltered;

	public SlideExpandableListAdapter(ListAdapter wrapped, int toggle_button_id, int expandable_view_id, Context context, List<AccountType> accountTypesFiltered) {
		super(wrapped, context, accountTypesFiltered);
		this.toggle_button_id = toggle_button_id;
        this.expandable_view_id = expandable_view_id;
        this.mAccountTypesFiltered = accountTypesFiltered;
	}
	
    public SlideExpandableListAdapter(ListAdapter wrapped, Context context) {
        this(wrapped, R.id.account_type_group_container, R.id.expandable, context, mAccountTypesFiltered);
    }

	@Override
	public View getExpandToggleButton(View parent) {
		View view = parent.findViewById(toggle_button_id);
		return view;
	}

	@Override
    public View getExpandableView(View parent) {
        return parent.findViewById(expandable_view_id);
    }
}
