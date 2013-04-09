package main.java.com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.ImageView;

import main.java.com.moneydesktop.finance.R;

public class CaretView extends ImageView {

    private float mWidth = 10;
    private float mHeight = 10;

    private float mDegrees = 0.0f;

    private CaretDrawable mCaret;

    public int getColor() {
        return mCaret.getColor();
    }

    public void setColor(int color) {
        mCaret.setColor(color);
    }

    public float getCaretWidth() {
        return mWidth;
    }

    public void setCaretWidth(float width) {
        this.mWidth = width;
        createCaret();
    }

    public float getCaretHeight() {
        return mHeight;
    }

    public void setCaretHeight(float height) {
        this.mHeight = height;
        createCaret();
    }

    public void setCaretRotation(float degrees) {
        mDegrees = degrees;
        invalidate();
    }

    public CaretView(Context context) {
        super(context);

        createCaret();
    }

    public CaretView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public CaretView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) getCaretWidth(), (int) getCaretHeight());
    }

    private void init(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CaretView);

        setCaretWidth(a.getDimension(R.styleable.CaretView_width, 10.0f));
        setCaretHeight(a.getDimension(R.styleable.CaretView_height, 10.0f));
        setCaretRotation(a.getFloat(R.styleable.CaretView_rotation, 0.0f));

        createCaret();

        setColor(a.getColor(R.styleable.CaretView_color, Color.WHITE));

        a.recycle();
    }

    private void createCaret() {

        mCaret = new CaretDrawable(new PointF(0, 0), getCaretWidth(), getCaretHeight());
        mCaret.setColor(getColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        canvas.rotate(mDegrees, getWidth()/2, getHeight()/2);
        mCaret.draw(canvas);

        canvas.restore();
    }
}
