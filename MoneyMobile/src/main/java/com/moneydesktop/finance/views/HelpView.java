package main.java.com.moneydesktop.finance.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Preferences;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;

import java.util.ArrayList;

/**
 * MoneyDesktop - MoneyMobile
 * <p/>
 * User: saulhoward
 * Date: 4/2/13
 * <p/>
 * Description:
 */
public class HelpView extends ViewGroup implements View.OnClickListener {

    public final String TAG = this.getClass().getSimpleName();

    private static final int RADIUS = 5;
    private static final int WIDTH = 2;
    private static final int OFFSET = 15;

    public enum Direction {
        LEFT, UP, RIGHT, DOWN
    }

    public enum TextSide {
        LEFT, RIGHT
    }

    private Window mWindow;

    private ArrayList<HelpModel> mHelps;
    private Paint mConnectorPaint;

    private int mRadius;
    private int mWidth;
    private int mOffset;

    private Dialog mDialog;
    private TextView mExit, mNoShow;

    public void setDialog(Dialog mDialog) {
        this.mDialog = mDialog;
    }

    public HelpView(Context context) {
        this(context, null);
    }

    public HelpView(Activity activity) {
        this((Context) activity);

        mWindow = activity.getWindow();
    }

    public HelpView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public HelpView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {

        setWillNotDraw(false);

        if (getLayoutParams() == null) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            setLayoutParams(params);
        }

        mRadius = (int) UiUtils.getDynamicPixels(getContext(), RADIUS);
        mWidth = (int) UiUtils.getDynamicPixels(getContext(), WIDTH);
        mOffset = (int) UiUtils.getDynamicPixels(getContext(), OFFSET);

        mHelps = new ArrayList<HelpModel>();

        mConnectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mConnectorPaint.setColor(Color.WHITE);
        mConnectorPaint.setStrokeWidth(mWidth);

        setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mExit == null && mNoShow == null) {
            setupTexts();
        }

        for (HelpModel helpModel : mHelps) {
            helpModel.createTextView(changed);
        }
    }

    private void setupTexts() {

        Point exitPoint = new Point(getWidth() * 19 / 20, mOffset);
        mExit = createTextView(getResources().getString(R.string.icon_cancel), 30, Fonts.getFont(Fonts.GLYPH), 0, exitPoint);

        Point showPoint = new Point(getWidth() * 9 / 10, getHeight() * 9 / 10);
        mNoShow = createTextView(getResources().getString(R.string.help), 10, Fonts.getFont(Fonts.PRIMARY_BOLD), 20, showPoint);
        mNoShow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Preferences.saveBoolean(Preferences.KEY_SHOW_TIPS, false);
                mDialog.dismiss();
            }
        });
    }

    private TextView createTextView(String text, int textSize, Typeface typeface, int padding, Point point) {

        padding = (int) UiUtils.getDynamicPixels(getContext(), padding);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(getContext());
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(Fonts.getFontSize(textSize));
        textView.setTypeface(typeface);
        textView.setPadding(padding, padding, padding, padding);

        textView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        int[] layout = new int[] {
                point.x, point.y, point.x + textView.getMeasuredWidth(), point.y + textView.getMeasuredHeight()
        };

        layout = adjustLayout(layout);

        textView.layout(layout[0], layout[1], layout[2], layout[3]);

        addView(textView);

        return textView;
    }

    /**
     * Edge detection, keeps the TextView in the bounds of the screen.
     *
     * @param layout
     * @return
     */
    private int[] adjustLayout(int[] layout) {

        while (layout[0] < mOffset) {
            layout[0]++;
            layout[2]++;
        }

        while (layout[1] < mOffset) {
            layout[1]++;
            layout[3]++;
        }

        while (layout[2] > getWidth() - mOffset) {
            layout[0]--;
            layout[2]--;
        }

        while (layout[3] > getWidth() - mOffset) {
            layout[1]--;
            layout[3]--;
        }

        return layout;
    }

    public void addHelp(View view, Direction direction, TextSide textSide, int distance, int textId, int textWidth) {
        addHelp(view, null, direction, textSide, distance, textId, textWidth);
    }

    public void addHelp(View view, Point locationOffset, Direction direction, int distance, int textId,
                        int textWidth) {
        addHelp(view, locationOffset, direction, TextSide.RIGHT, distance, getContext().getString(textId), textWidth);
    }

    public void addHelp(View view, Point locationOffset, Direction direction, TextSide textSide, int distance, int textId, int textWidth) {
        addHelp(view, locationOffset, direction, textSide, distance, getContext().getString(textId), textWidth);
    }

    public void addHelp(View view, Point locationOffset, Direction direction, TextSide textSide, int distance, String text, int textWidth) {

        HelpModel help = new HelpModel();
        help.view = view;
        help.locationOffset = locationOffset;
        help.direction = direction;
        help.textSide = textSide;
        help.distance = (int) UiUtils.getDynamicPixels(getContext(), distance);
        help.text = text;
        help.textWidth = (int) UiUtils.getDynamicPixels(getContext(), textWidth);
        help.initialize();

        mHelps.add(help);
    }

    @Override
    public void onDraw(Canvas canvas) {

        for (HelpModel helpModel : mHelps) {
            drawHelp(canvas, helpModel);
        }

        super.onDraw(canvas);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mDialog.dismiss();
    }

    private void drawHelp(Canvas canvas, HelpModel helpModel) {

        canvas.drawCircle(helpModel.location[0], helpModel.location[1], mRadius, mConnectorPaint);
        canvas.drawLine(helpModel.location[0], helpModel.location[1], helpModel.endPoint.x, helpModel.endPoint.y, mConnectorPaint);
    }

    class HelpModel {

        TextView textView;
        View view;
        Direction direction;
        TextSide textSide = TextSide.RIGHT;
        int distance;
        String text;
        int textWidth;
        int[] location;
        Point locationOffset;
        Point endPoint;

        public HelpModel() {
        }

        public void initialize() {

            location = new int[2];
            view.getLocationOnScreen(location);

            // Set the location to the center of the view and adjusts for the status bar
            location[0] += view.getWidth() / 2;
            location[1] += view.getHeight() / 2 - UiUtils.getStatusBarHeight(mWindow);

            if (locationOffset != null) {
                location[0] += locationOffset.x;
                location[1] += locationOffset.y;
            }

            updateEndPoint();
        }

        private void updateEndPoint() {

            switch (direction) {

                case LEFT:
                    endPoint = new Point(location[0] - distance, location[1]);
                    break;

                case UP:
                    endPoint = new Point(location[0], location[1] - distance);
                    break;

                case RIGHT:
                    endPoint = new Point(location[0] + distance, location[1]);
                    break;

                case DOWN:
                    endPoint = new Point(location[0], location[1] + distance);
                    break;
            }
        }

        public void createTextView(boolean changed) {

            if (textView != null && !changed) return;

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(textWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

            textView = new TextView(getContext());
            textView.setLayoutParams(params);
            textView.setText(text);
            textView.setTextColor(getContext().getResources().getColor(R.color.gray7));
            textView.setBackgroundColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(mRadius, mRadius, mRadius, mRadius);

            Fonts.applyPrimaryBoldFont(textView, 12);

            textView.measure(MeasureSpec.EXACTLY | textWidth, MeasureSpec.UNSPECIFIED);

            int[] layout = getLayout();

            textView.layout(layout[0], layout[1], layout[2], layout[3]);
            addView(textView);
        }

        private int[] getLayout() {

            int[] layout = new int[4];

            switch (direction) {

                case LEFT:
                    layout[0] = endPoint.x - textView.getMeasuredWidth();
                    layout[1] =  endPoint.y - mOffset;
                    break;

                case UP:
                    layout[0] = endPoint.x - mOffset;
                    layout[1] =  endPoint.y - textView.getMeasuredHeight();
                    break;

                case RIGHT:
                    layout[0] = endPoint.x;
                    layout[1] =  endPoint.y - mOffset;
                    break;

                case DOWN:
                    layout[0] = endPoint.x - mOffset;
                    layout[1] =  endPoint.y;
                    break;
            }

            layout[2] = layout[0] + textView.getMeasuredWidth();
            layout[3] = layout[1] + textView.getMeasuredHeight();

            layout = adjustTextSide(layout);

            return adjustLayout(layout);
        }

        private int[] adjustTextSide(int[] layout) {

            if (textSide == TextSide.RIGHT) return layout;

            layout[0] = layout[0] - textView.getMeasuredWidth() + mOffset * 2;
            layout[2] = layout[0] + textView.getMeasuredWidth();

            return layout;
        }
    }
}
