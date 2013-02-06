package com.moneydesktop.finance.tablet.adapter;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.moneydesktop.finance.views.DeleteLinearLayout;

import java.util.List;

public abstract class DeleteBaseAdapter<T> extends ArrayAdapter<T> implements OnScrollListener {

    public DeleteBaseAdapter(Context context, int textViewResourceId, List<T> objects, ListView listView) {
        super(context, textViewResourceId, objects);
        
        listView.setOnScrollListener(this);
    }

    private DeleteLinearLayout mDeletingCell;
    
    public void setDeletingCell(DeleteLinearLayout mDeletingCell) {
        
        if (this.mDeletingCell != null) {
            this.mDeletingCell.fadeOutDelete();
        }
        this.mDeletingCell = mDeletingCell;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        
        if (mDeletingCell != null) {
            mDeletingCell.fadeOutDelete();
            mDeletingCell = null;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
}
