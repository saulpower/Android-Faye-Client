package com.moneydesktop.finance.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

@TargetApi(11)
public class SettingButton extends BaseView {
    
    public final String TAG = this.getClass().getSimpleName();

    private float mSize;
    
    private String mIconText;
    private String mTitleText;
    
    private TextView mSubTextView;
    
    private PointF mIconPoint, mTitlePoint;
    
    private int mTextColor, mIconColor;
    private Paint mIconPaint, mLinePaint,  mTitlePaint, mBorderPaint;
    
    private Rect mIconBounds = new Rect();
    private Rect mTitleBounds = new Rect();
    
    private OnClickListener mListener;

    public void setIconSize(float size) {
        
        if (mIconPaint == null) {

            initIconPaint();
        }
        
        mIconPaint.setTextSize(size);
        updateIconBounds();
        invalidate();
    }
    
    public void setIconText(String iconText) {
        
        mIconText = iconText;
        updateIconBounds();
        invalidate();
    }
    
    public void setTitleText(String text) {
        
        mTitleText = text.toUpperCase();

        mTitlePaint.getTextBounds(mTitleText, 0, mTitleText.length(), mTitleBounds);
        mTitlePoint = new PointF((mSize / 2) - (mTitleBounds.width()/2), (float) ((mSize / 1.6) + (mTitleBounds.height() / 2)));
        
        invalidate();
    }
    
    public void setSubText(String subText) {
        
        mSubTextView.setText(subText);
        invalidate();
    }
    
    public int getIconColor() {
        
        if (mIconPaint != null) {
            return mIconPaint.getColor();
        }
        
        return 0;
    }

    public void setIconColor(int iconColor) {
        
        mIconPaint.setColor(iconColor);
        invalidate();
    }

    public int getTextColor() {
        
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        
        if (mTitlePaint == null) {
            initPaints();
        }
        
        mTitlePaint.setColor(textColor);
        mLinePaint.setColor(textColor);
        mSubTextView.setTextColor(textColor);
        
        invalidate();
    }
    
    public void setTouching(boolean touching) {
        
        setTextColor(touching ? Color.WHITE : mTextColor);
        setBackgroundColor(touching ? mTextColor : Color.WHITE);
        setIconColor(touching ? Color.WHITE : mIconColor);
        
        invalidate();
    }
    
    @Override
    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }
    
    private void initIconPaint() {

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIconPaint.setStyle(Paint.Style.FILL);
        mIconPaint.setTypeface(Fonts.getFont(Fonts.GLYPH));
        mIconPaint.setTextAlign(Align.CENTER);
    }
    
    private void updateIconBounds() {

        mIconPaint.getTextBounds(mIconText, 0, mIconText.length(), mIconBounds);
        mIconPoint = new PointF((mSize / 2), (float) ((mSize / 2.95) + (mIconBounds.height() / 2)));
    }
    
    private void initSubTextView() {
        
        if (mSubTextView == null) {
            mSubTextView = new TextView(getContext());
            mSubTextView.setWidth((int) mSize);
            mSubTextView.setHeight((int) mSize);
            mSubTextView.setTop(0);
            mSubTextView.setRight((int) mSize);
            mSubTextView.setBottom((int) mSize);
            mSubTextView.setLeft(0);
            mSubTextView.setPadding(10, (int) (mSize * 0.7), 10, 0);
            mSubTextView.setGravity(Gravity.CENTER);
            mSubTextView.setTextSize(14);
            mSubTextView.setTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        }
    }
    
    public SettingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {

        mSize = (int) UiUtils.getDynamicPixels(getContext(), 225);

        initSubTextView();
        initIconPaint();
        
        if (attrs != null) {
            
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Settings);
    
            mIconColor = a.getColor(R.styleable.Settings_iconColor, getContext().getResources().getColor(R.color.primaryColor));
            setIconColor(mIconColor);
            mTextColor = a.getColor(R.styleable.Settings_textColor, getContext().getResources().getColor(R.color.gray3));
            setTextColor(mTextColor);
            
            setIconText(a.getString(R.styleable.Settings_icon));
            setTitleText(a.getString(R.styleable.Settings_title));
            setSubText(a.getString(R.styleable.Settings_text));
            
            setIconSize(a.getDimension(R.styleable.Settings_iconSize, 80));
            
            a.recycle();
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } 
    }
    
    private void initPaints() {

        DashPathEffect dashPath = new DashPathEffect(new float[] {1, 1}, 1.0f);
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);
        mLinePaint.setPathEffect(dashPath);

        mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitlePaint.setStyle(Paint.Style.FILL);
        mTitlePaint.setTypeface(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mTitlePaint.setTextSize(24);
        
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(1);
        mBorderPaint.setColor(getContext().getResources().getColor(R.color.light_gray1));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            mSize = MeasureSpec.getSize(widthMeasureSpec);
        }
        
        setMeasuredDimension((int) mSize, (int) mSize);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        
        final int action = ev.getAction();
        
        switch (action) {
        
            case MotionEvent.ACTION_DOWN: {
                
                setTouching(true);
                
                break;
            }
            
            case MotionEvent.ACTION_UP: {
                
                setTouching(false);
                
                if (isPointInsideView(ev.getRawX(), ev.getRawY(), this)){
                    
                    if (mListener != null) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        mListener.onClick(this);
                    }
                }
                
                break;
            }
        }
        
        return true;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.drawText(mIconText, mIconPoint.x, mIconPoint.y, mIconPaint);
        canvas.drawLine(mSize/5, (int) (mSize/1.85), mSize*4/5, (int) (mSize/1.85), mLinePaint);
        canvas.drawText(mTitleText, mTitlePoint.x, mTitlePoint.y, mTitlePaint);
        mSubTextView.draw(canvas);
        canvas.drawLine(0, mSize-1, mSize, mSize-1, mBorderPaint);
    }
}
