package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.util.UiUtils;

public class UpArrowButton extends Button {

    public final String TAG = this.getClass().getSimpleName();

    private final int ARROW_PADDING = 5;

    private boolean mShowArrow;
    private Bitmap mArrow;
    private float mArrowPadding = 0f;

    public UpArrowButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mShowArrow = false;
        mArrow = BitmapFactory.decodeResource(getResources(), R.drawable.tablet_dashboard_uparrow);

        mArrowPadding = UiUtils.getDynamicPixels(getContext(), ARROW_PADDING);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        mShowArrow = false;

        if (isState(View.PRESSED_ENABLED_STATE_SET) ||
            isState(View.SELECTED_STATE_SET)) {
            mShowArrow = true;
        }

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowArrow == true) {
            canvas.drawBitmap(mArrow, (getWidth() / 2) - (mArrow.getWidth() / 2), mArrowPadding, null);
        }
    }

    private boolean isState(int[] drawableState) {

        for (int state : drawableState) {

            boolean contains = false;

            for (int compareState : getDrawableState()) {

                if (state == compareState) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                return false;
            }
        }

        return true;
    }
}