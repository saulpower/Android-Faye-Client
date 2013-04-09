package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;

public class WheelView extends View {

    public static final String TAG = "WheelView";

    private Bitmap mCover;
    private Paint mPaint, mStroke, mText;
    private float mWidth, mHeight;
    private Rect mBoundsYes, mBoundsNo;
    private String mYes, mNo;
    private float mRotation = 0;

    public WheelView(Context context) {
        super(context);

        load(context);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        load(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        load(context);
    }

    public float getRotation() {
        return mRotation;
    }

    public void setRotation(float mRotation) {
        this.mRotation = mRotation;
        invalidate();
    }

    private void load(Context context) {

        Resources res = context.getResources();

        mYes = res.getString(R.string.label_yes).toUpperCase();
        mNo = res.getString(R.string.label_no).toUpperCase();

        // Load the panel to base our circle off of its measurements
        this.mCover = BitmapFactory.decodeResource(res, R.drawable.phone_switch_paper);

        mWidth = mCover.getHeight() * .8f;
        mHeight = mCover.getHeight() * .8f;

        // Setup the various paints we will need
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(res.getColor(R.color.primaryColor));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStroke.setStrokeWidth(UiUtils.getDynamicPixels(getContext(), 3));
        mStroke.setColor(Color.WHITE);
        mStroke.setStyle(Paint.Style.STROKE);
        mStroke.setAntiAlias(true);

        // Scale the font size for the display resolution
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics());

        // Setup the text paint
        mText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mText.setColor(Color.WHITE);
        mText.setAntiAlias(true);
        mText.setTextSize(px);
        mText.setTypeface(Fonts.getFont(Fonts.PRIMARY_BOLD));

        // Get the bounds of the yes/no text to help with positioning
        mBoundsYes = new Rect();
        mText.getTextBounds(mYes, 0, mYes.length(), mBoundsYes);
        mBoundsNo = new Rect();
        mText.getTextBounds(mNo, 0, mNo.length(), mBoundsNo);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float py = getHeight()/2.0f;
        float px = getWidth()/2.0f;

        canvas.save();

        canvas.rotate(mRotation, px, py);

        // Draw the circle and give it a stroke
        canvas.drawCircle(getWidth()/2.0f, getHeight()/2.0f, mHeight/2.0f, mPaint);
        canvas.drawCircle(getWidth()/2.0f, getHeight()/2.0f, mHeight/2.0f, mStroke);

        // Draw the yes and position it appropriately
        canvas.drawText(mYes, getWidth()/4.0f - mBoundsYes.width()/2.0f, getHeight()/2.0f + mBoundsYes.height()/2.0f, mText);

        // Rotate the canvas to write no

        canvas.rotate(180, px, py);

        // Draw the no and position it appropriately
        canvas.drawText(mNo, getWidth()/4.0f - mBoundsNo.width()/2.0f, getHeight()/2.0f + mBoundsNo.height()/2.0f, mText);

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension((int) (mWidth * 1.08), (int) (mHeight * 1.08));
    }

}
