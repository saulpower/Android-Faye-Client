package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableLayout extends RelativeLayout implements Checkable {

    public final String TAG = this.getClass().getSimpleName();

    private static final int[] STATE_CHECKABLE = { android.R.attr.state_checked };

    private boolean mChecked = false;

    public CheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {

        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mChecked) mergeDrawableStates(drawableState, STATE_CHECKABLE);

        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {

        mChecked = checked;
        refreshDrawableState();
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

}
