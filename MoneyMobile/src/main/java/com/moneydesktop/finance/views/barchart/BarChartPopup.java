package main.java.com.moneydesktop.finance.views.barchart;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.animation.OvershootInterpolator;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.CaretDrawable;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA.
 * User: saulhoward
 * Date: 3/26/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class BarChartPopup extends Drawable {

    public final String TAG = this.getClass().getSimpleName();

    private static final int CARET_SIZE = 20;

    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");

    private Context mContext;

    private Paint mDatePaint;
    private Paint mAmountPaint;
    private Paint mDetailPaint;
    private Paint mBgPaint;

    private String mDate, mAmount, mDetail;

    private Point mDatePoint = new Point();
    private Point mAmountPoint = new Point();
    private Point mDetailPoint = new Point();
    private Rect mDateBounds = new Rect();
    private Rect mAmountBounds = new Rect();
    private Rect mDetailBounds = new Rect();

    private CaretDrawable mCaret;
    private BarView mCurrentBar;
    private BarViewModel mCurrentModel;

    private float mScale = 1f;
    private float mCaretSize;

    private float mAmountNum = 0f;
    private float mPrevAmountNum = 0f;
    private float mAmountChange = 0f;

    private int mMaxWidth;

    private ObjectAnimator mShow, mHide;

    public BarChartPopup(Context context, Callback cb, int width, int height, int maxWidth) {

        mContext = context;
        mMaxWidth = maxWidth;
        mCaretSize = UiUtils.getDynamicPixels(getContext(), CARET_SIZE) + 0.5f;

        Rect bounds = new Rect(0, 0, width, height);
        bounds.bottom -= mCaretSize * 0.75f;
        setBounds(bounds);

        setCallback(cb);
        initializePaints();
        initializeCaret();

        setPaintAlpha(0);

        mAmount = mFormatter.format(0.0);
        mDetail = mContext.getString(R.string.button_transactions);
        mDate = "";
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
    }

    private void initializePaints() {

        int textGray = getContext().getResources().getColor(R.color.gray3);
        int bgGray = getContext().getResources().getColor(R.color.gray1);
        int primary = getContext().getResources().getColor(R.color.primaryColor);

        mDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDatePaint.setTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
        mDatePaint.setTextSize(Fonts.getPaintFontSize(18));
        mDatePaint.setColor(textGray);

        mAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAmountPaint.setTypeface(Fonts.getFont(Fonts.PRIMARY_BOLD));
        mAmountPaint.setTextSize(Fonts.getPaintFontSize(32));
        mAmountPaint.setColor(primary);

        mDetailPaint = new Paint(mDatePaint);
        mDetailPaint.setTextSize(Fonts.getPaintFontSize(18));

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(bgGray);
    }

    private void initializeCaret() {
        int bgGray = getContext().getResources().getColor(R.color.gray1);

        PointF point = new PointF(getBounds().width() / 2 - mCaretSize, getBounds().height() - mCaretSize / 4);

        mCaret = new CaretDrawable(getCallback(), point, mCaretSize, mCaretSize);
        mCaret.setColor(bgGray);
    }

    public Context getContext() {
        return mContext;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
        invalidateSelf();
    }

    public void setAmount(String mAmount) {
        this.mAmount = mAmount;
        invalidateSelf();
    }

    public void setDetail(String mDetail) {
        this.mDetail = mDetail;
        invalidateSelf();
    }

    public void setBarModel(BarViewModel model) {
        mCurrentModel = model;

        mAmountChange = mCurrentModel.getAmount() - mAmountNum;

        mPrevAmountNum = mAmountNum;
        mAmountNum = mCurrentModel.getAmount();
    }

    /**
     * Change the bar the popup is currently representing.
     *
     * @param bar The {@link BarView} the popup is placed over
     * @param model The {@link BarViewModel} the popup represents
     */
    public void changePopup(BarView bar, BarViewModel model) {

        if (mCurrentBar == bar && mCurrentModel == model) return;

        mCurrentBar = bar;
        setBarModel(model);

        if (mBgPaint.getAlpha() != 0) {
            hide(true);
        } else {
            updateText();
            updatePosition();
            show();
        }
    }

    /**
     * Show the popup
     */
    public void show() {

        if (mBgPaint.getAlpha() == 255) return;

        cancelAnimations();

        setAlpha(255);

        mShow = ObjectAnimator.ofFloat(this, "popupScale", 0f, 1f);
        mShow.setDuration(250);
        mShow.setInterpolator(new OvershootInterpolator());
        mShow.start();
    }

    /**
     * Hide the popup
     */
    public void hide() {
        hide(false);
    }

    /**
     * Hides the popup with the option to show it if it has
     * changed position and value.
     *
     * @param show
     */
    private void hide(final boolean show) {

        if (mBgPaint.getAlpha() == 0 && !show) return;

        cancelAnimations();

        mHide = ObjectAnimator.ofInt(this, "alpha", 255, 0);
        mHide.setDuration(250);
        mHide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {

                if (show) {
                    updateText();
                    updatePosition();
                    show();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mHide.start();
    }

    private void cancelAnimations() {

        if (mHide != null && mHide.isRunning()) {
            mHide.cancel();
        }

        if (mShow != null && mShow.isRunning()) {
            mShow.cancel();
        }
    }

    /**
     * Updates the amount as a percentage of change.  Used to animate
     * changes in a bar the popup is representing.
     *
     * @param percent
     */
    public void updateAmount(float percent) {

        float newAmount = mPrevAmountNum + mAmountChange * percent;
        mAmount = mFormatter.format(newAmount);
        updateTextPositions();

        invalidateSelf();
    }

    /**
     * Updates the text values and updates the text layout position
     */
    private void updateText() {

        mAmount = mFormatter.format(mAmountNum);
        mDate = mCurrentModel.getPopupText();

        if (mCurrentModel.getAmount() == 0) {
            mDetail = "";
        } else {
            mDetail = mContext.getString(R.string.button_transactions);
        }

        updateTextPositions();
    }

    /**
     * Updates the caret and background positions as well as updating
     * the text positioning as the popup has moved
     */
    void updatePosition() {

        // Update caret position
        PointF point = new PointF(mCurrentBar.getLeft() + (mCurrentBar.getBarBounds().exactCenterX() - mCaretSize / 2f), mCurrentBar.getBarBounds().top - mCaretSize * 3 / 4);
        mCaret.setPosition(point);

        // Update bounds position
        Rect bounds = getBounds();

        int left = mCurrentBar.getLeft() + (int) mCurrentBar.getBarBounds().exactCenterX() - bounds.width() / 2;

        if (left < 0) left = 0;
        if (left + bounds.width() > mMaxWidth) left = mMaxWidth - bounds.width();

        int top = (mCurrentBar.getBarBounds().top - bounds.height() - (int) (mCaretSize / 2f));
        int right = left + bounds.width();
        int bottom = top + bounds.height();

        setBounds(left, top, right, bottom);
        updateTextPositions();
    }

    /**
     * Updates the text positioning of all items
     */
    private void updateTextPositions() {

        mDatePaint.getTextBounds(mDate, 0, mDate.length(), mDateBounds);
        mAmountPaint.getTextBounds(mAmount, 0, mAmount.length(), mAmountBounds);
        mDetailPaint.getTextBounds(mDetail, 0, mDetail.length(), mDetailBounds);

        Rect bounds = getBounds();

        mAmountPoint.x = bounds.centerX() - mAmountBounds.centerX();
        mAmountPoint.y = bounds.centerY() + mAmountBounds.height() / 2;

        mDetailPoint.x = bounds.centerX() - mDetailBounds.centerX();
        mDetailPoint.y = mAmountPoint.y + mAmountBounds.height() / 4 + mDetailBounds.height();

        mDatePoint.x = bounds.centerX() - mDateBounds.centerX();
        mDatePoint.y = mAmountPoint.y - (int) (mAmountBounds.height() * 1.25);

    }

    void setPopupScale(float scale) {
        mScale = scale;
        invalidateSelf();
    }

    /**
     * Draw in its bounds (set via setBounds) respecting optional effects such
     * as alpha (set via setAlpha) and color filter (set via setColorFilter).
     *
     * @param canvas The canvas to draw into
     */
    @Override
    public void draw(Canvas canvas) {

        canvas.save();
        canvas.scale(mScale, mScale, getBounds().exactCenterX(), getBounds().exactCenterY());

        canvas.drawRect(getBounds(), mBgPaint);
        mCaret.draw(canvas);

        canvas.drawText(mAmount, 0, mAmount.length(), mAmountPoint.x, mAmountPoint.y, mAmountPaint);
        canvas.drawText(mDate, 0, mDate.length(), mDatePoint.x, mDatePoint.y, mDatePaint);
        canvas.drawText(mDetail, 0, mDetail.length(), mDetailPoint.x, mDetailPoint.y, mDetailPaint);

        canvas.restore();
    }

    /**
     * Specify an alpha value for the drawable. 0 means fully transparent, and
     * 255 means fully opaque.
     */
    @Override
    public void setAlpha(int alpha) {
        setPaintAlpha(alpha);
        invalidateSelf();
    }

    private void setPaintAlpha(int alpha) {

        mCaret.setAlpha(alpha);
        mBgPaint.setAlpha(alpha);
        mDatePaint.setAlpha(alpha);
        mDetailPaint.setAlpha(alpha);
        mAmountPaint.setAlpha(alpha);
    }

    /**
     * Specify an optional colorFilter for the drawable. Pass null to remove
     * any filters.
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    /**
     * Return the opacity/transparency of this Drawable.  The returned value is
     * one of the abstract format constants in
     * {@link android.graphics.PixelFormat}:
     * {@link android.graphics.PixelFormat#UNKNOWN},
     * {@link android.graphics.PixelFormat#TRANSLUCENT},
     * {@link android.graphics.PixelFormat#TRANSPARENT}, or
     * {@link android.graphics.PixelFormat#OPAQUE}.
     * <p/>
     * <p>Generally a Drawable should be as conservative as possible with the
     * value it returns.  For example, if it contains multiple child drawables
     * and only shows one of them at a time, if only one of the children is
     * TRANSLUCENT and the others are OPAQUE then TRANSLUCENT should be
     * returned.  You can use the method {@link #resolveOpacity} to perform a
     * standard reduction of two opacities to the appropriate single output.
     * <p/>
     * <p>Note that the returned value does <em>not</em> take into account a
     * custom alpha or color filter that has been applied by the client through
     * the {@link #setAlpha} or {@link #setColorFilter} methods.
     *
     * @return int The opacity class of the Drawable.
     * @see android.graphics.PixelFormat
     */
    @Override
    public int getOpacity() {
        return 0;
    }
}
