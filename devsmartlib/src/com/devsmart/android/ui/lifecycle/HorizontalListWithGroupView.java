package com.devsmart.android.ui.lifecycle;

import android.content.Context;
import android.util.AttributeSet;
import com.devsmart.android.ui.HorizontalListView;

public class HorizontalListWithGroupView extends HorizontalListView {

    private static int mGroupPostion;


    public HorizontalListWithGroupView(Context context, int groupPostion) {
        super(context, groupPostion);
        mGroupPostion = groupPostion;
    }

    public static int getGroupPosition () {
        return mGroupPostion;
    }
}
