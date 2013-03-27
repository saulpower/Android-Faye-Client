package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.model.EventMessage.AnchorChangeEvent;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

import de.greenrobot.event.EventBus;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateRangeItem extends Drawable {
    
    public final String TAG = this.getClass().getSimpleName();

    private final int PADDING = 4;
    
    protected Context mContext;
    private int mIndex;
    
    private int mDefaultColor, mDefaultBorderColor;
    protected Paint mPaintMonth;
    protected Paint mPaintYear;
    protected Paint mPaintBorder;
    protected Paint mPaintBorderTop;
    
    private Path mPathBottom;
    
    private Date mDate;
    private String mMonth, mYear;
    private Rect mMonthBounds = new Rect();
    private Rect mYearBounds = new Rect();
    private Rect mBottomBounds, mTopBounds;
    
    private SimpleDateFormat mFormatterMonth = new SimpleDateFormat("MMM");
    private SimpleDateFormat mFormatterYear = new SimpleDateFormat("yyyy");
    
    public int getIndex() {
        return mIndex;
    }
    
    public void setMonthColor(int color) {
        mPaintMonth.setColor(color);
        invalidateSelf();
    }
    
    public void setBorderColor(int color) {
        mPaintBorder.setColor(color);
        invalidateSelf();
    }
    
    public Date getDate() {
        return mDate;
    }

    public Calendar getCalendar() {
        return DateUtils.toCalendar(mDate);
    }
    
    public Date getEndDate() {
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate);
        cal.add(Calendar.MONTH, 1);
        
        return cal.getTime();
    }
    
    public DateRangeItem(Context context, int index, Date date, Rect bounds) {
        
        mContext = context;
        mIndex = index;
        mDate = date;
        
        setBounds(bounds);
        configureBounds();
        
        initPaints();
        initText();
        
        EventBus.getDefault().register(this);
    }
    
    private void initPaints() {
        
        Resources resources = mContext.getResources();
        
        mDefaultColor = resources.getColor(R.color.gray5);
        mDefaultBorderColor = resources.getColor(R.color.primaryColor);
        
        mPaintMonth = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMonth.setColor(mDefaultColor);
        mPaintMonth.setStyle(Paint.Style.FILL);
        mPaintMonth.setTypeface(Fonts.getFont(Fonts.PRIMARY));
        mPaintMonth.setTextSize(UiUtils.getScaledPixels(mContext, 14));
        mPaintMonth.setTextAlign(Align.LEFT);
        
        mPaintYear = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintYear.setColor(mDefaultColor);
        mPaintYear.setStyle(Paint.Style.FILL);
        mPaintYear.setTypeface(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mPaintYear.setTextSize(UiUtils.getScaledPixels(mContext, 14));
        mPaintYear.setTextAlign(Align.LEFT);
        
        mPaintBorderTop = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorderTop.setStyle(Paint.Style.STROKE);
        mPaintBorderTop.setColor(mDefaultBorderColor);
        mPaintBorderTop.setStrokeWidth(UiUtils.getDynamicPixels(mContext, 1.0f));
        
        mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorder.setStyle(Paint.Style.STROKE);
        mPaintBorder.setColor(mDefaultBorderColor);
        mPaintBorder.setStrokeWidth(UiUtils.getDynamicPixels(mContext, 1.0f));
        
        if (mDate.getMonth() != 0) {
            float intervalOn = UiUtils.getDynamicPixels(mContext, 1);
            float intervalOff = UiUtils.getDynamicPixels(mContext, 3);
            DashPathEffect dashPath = new DashPathEffect(new float[] {intervalOn, intervalOff}, 1.0f);
            mPaintBorder.setPathEffect(dashPath);
        }
    }
    
    private void initText() {

        mMonth = mFormatterMonth.format(mDate);
        mPaintMonth.getTextBounds(mMonth, 0, mMonth.length(), mMonthBounds);
        
        mYear = mFormatterYear.format(mDate);
        mPaintYear.getTextBounds(mYear, 0, mYear.length(), mYearBounds);
    }
    
    private void configureBounds() {
        
        Rect bounds = getBounds();
        
        mTopBounds = new Rect(bounds.left, bounds.top, bounds.right, (bounds.height() * 2 / 5));
        mBottomBounds = new Rect(bounds.left, mTopBounds.bottom, bounds.right, bounds.bottom);
        
        mPathBottom = new Path();
        mPathBottom.moveTo(mBottomBounds.left, mBottomBounds.top + PADDING);
        mPathBottom.lineTo(mBottomBounds.left, mBottomBounds.bottom - PADDING);
    }
    
    @Override
    public void draw(Canvas canvas) {
        
        canvas.drawPath(mPathBottom, mPaintBorder);
        canvas.drawText(mMonth, mBottomBounds.left + mBottomBounds.width() / 2 - mMonthBounds.width() / 2, mBottomBounds.top + mBottomBounds.height() / 2 + mMonthBounds.height() / 2, mPaintMonth);
        
        if (mDate.getMonth() == 0) {
            
            canvas.drawLine(mTopBounds.left, mTopBounds.top + 3, mTopBounds.left, mTopBounds.bottom - PADDING, mPaintBorderTop);
            canvas.drawText(mYear, mTopBounds.left + mTopBounds.width() / 2 - mYearBounds.width() / 2, 3 + mTopBounds.top + mTopBounds.height() / 2 + mYearBounds.height() / 2, mPaintYear);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaintMonth.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {}

    public void onEvent(AnchorChangeEvent event) {
        
        AnchorView left = event.getLeft(); 
        AnchorView right = event.getRight();
        
        int centerX = getBounds().left + getBounds().width() / 2;
        
        boolean leftCheck = left.getPosition() < centerX;
        boolean rightCheck = right.getPosition() > centerX;
        boolean leftCheck1 = left.getPosition() < getBounds().left;
        boolean rightCheck1 = right.getPosition() > getBounds().left;
        
        if (leftCheck1 && rightCheck1) {
            setBorderColor(Color.WHITE);
        } else {
            setBorderColor(mDefaultBorderColor);
        }
        
        if (leftCheck && rightCheck) {
            setMonthColor(Color.WHITE);
        } else {
            setMonthColor(mDefaultColor);
        }
    }

}
