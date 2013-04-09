package main.java.com.moneydesktop.finance.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.util.UiUtils;

import java.util.List;

public class PopupWindowAtLocation extends FrameLayout {

    final int mX;
    final int mY;
    final Context mContext;
    String[] mButtonTitles;

    List<OnClickListener> mButtonClickListeners;
    ViewGroup mParentView;
    ViewGroup mRoot;
    LayoutInflater mInflater;
    int mScreenHeight;
    int mScreenWidth;
    View mTouchedView;
    TransparentView mTransparentView;
    RelativeLayout mSubOverlay;

    private Paint bg;
    private int mLeftMargin;
    private int mTopMargin;
    private int mWidth;
    private int mHeight;
    private Rect mRect;
    private View mPassedInView;

    /**
     * @param context          -- the context
     * @param parentView       -- the layout view that the popUp will be displayed in
     * @param positionX        -- the X position for the popUp to be aligned with
     * @param positionY        -- the Y position for the popUp to be aligned with
     * @param buttonTitles     -- the text that will be displayed for each of the buttons. Note** MUST be put in the list in the same order as the onClickListeners
     * @param onClickListeners -- onClick listeners for the buttons supplied. Note** MUST be put in the list in the same order as the Button Titles
     * @param touchedView
     */
    public PopupWindowAtLocation(Context context, ViewGroup parentView, int positionX, int positionY, String[] buttonTitles, List<OnClickListener> onClickListeners, View touchedView) {
        super(context);

        mContext = context;
        mX = positionX;
        mY = positionY;
        mButtonClickListeners = onClickListeners;
        mButtonTitles = buttonTitles;
        mParentView = parentView;
        mTouchedView = touchedView;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = (RelativeLayout) mInflater.inflate(R.layout.popup_with_buttons, null);
        mSubOverlay = (RelativeLayout) mRoot.findViewById(R.id.popup_sub_overlay);


        LinearLayout.LayoutParams overlayParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mRoot.setLayoutParams(overlayParams);

        mScreenHeight = UiUtils.getScreenHeight((Activity) mContext);
        mScreenWidth = UiUtils.getScreenWidth((Activity) mContext);

        Animation loadPopupAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fast);
        mSubOverlay.startAnimation(loadPopupAnimation);
        Animation backgroundFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fast);
        mRoot.startAnimation(backgroundFadeIn);

        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = (RelativeLayout) mInflater.inflate(R.layout.popup_with_buttons,
                null);
        mSubOverlay = (RelativeLayout) mRoot
                .findViewById(R.id.popup_sub_overlay);

        mRoot.startAnimation(backgroundFadeIn);

        mParentView.addView(mRoot);

        populateView();
    }

//    public PopupWindowAtLocation(Context context, ViewGroup parentView,
//                                 int positionX, int positionY, View touchedView,
//                                 ViewGroup inflatedView) {
//        super(context);
//
//        mContext = context;
//        mX = positionX;
//        mY = positionY;
//        mParentView = parentView;
//        mTouchedView = touchedView;
//
//
//        mRoot = inflatedView;
//        mScreenHeight = UiUtils.getScreenHeight((Activity) mContext);
//        mScreenWidth = UiUtils.getScreenWidth((Activity) mContext);
//
//        Animation backgroundFadeIn = new ScaleAnimation((float) .5, 1, (float) .5, 1, mX+UiUtils.convertDpToPixel(120,mContext), mY+UiUtils.convertDpToPixel(50,mContext));
//        //backgroundFadeIn.initialize(mRoot.getWidth(), mRoot.getHeight(), this.getWidth(), this.getHeight());
//        backgroundFadeIn.setDuration(200);
//        backgroundFadeIn.setInterpolator(new OvershootInterpolator());
//        mRoot.startAnimation(backgroundFadeIn);
//
//        mParentView.addView(mRoot);
//        populateInflatedView();
//
//    }
//
//    public boolean popupWillDisplayOffScreenBottom() {
//        return ((mY + (1 * UiUtils
//                .convertDpToPixel(78, mContext))) > mScreenHeight) ? true
//                : false;
//    }
//
//    public boolean popupWillDisplayOffScreenTop() {
//        return (mY < 0) ? true : false;
//    }
//
//    public boolean popupWillDisplayOffScreenLeft() {
//        return (mX < 0) ? true : false;
//    }
//
//    public boolean popupWillDisplayOffScreenRight() {
//        return (mX + UiUtils.getDynamicPixels(mContext, 240) > mParentView.getRight()) ? true
//                : false;
//
//    }

//    private void populateInflatedView() {
//        RelativeLayout popup = (RelativeLayout) mRoot
//                .findViewById(R.id.bar_graph_pop_up_relative);
//
//        RelativeLayout.LayoutParams overlayParams = (RelativeLayout.LayoutParams) popup
//                .getLayoutParams();
//
//        overlayParams.leftMargin = mX;
//        overlayParams.topMargin = mY;
//
//        if (popupWillDisplayOffScreenBottom()) {
//            overlayParams.topMargin = (int) (mScreenHeight
//                    * UiUtils.convertDpToPixel(76, mContext));
//        }
//        if (popupWillDisplayOffScreenTop()) {
//            overlayParams.topMargin = 0;
//        }
//        if (popupWillDisplayOffScreenRight()) {
//            overlayParams.leftMargin = (int) (mParentView.getWidth() - UiUtils.convertDpToPixel(240, mContext));
//        }
//        if (popupWillDisplayOffScreenLeft()) {
//            overlayParams.leftMargin = popup.getWidth();
//        }
//
//        popup.setLayoutParams(overlayParams);
//        popup.requestLayout();
//    }

    private void populateView() {
        RelativeLayout overlay = (RelativeLayout) mRoot.findViewById(R.id.popup_overlay);
        RelativeLayout subOverlay = (RelativeLayout) mRoot.findViewById(R.id.popup_sub_overlay);
        LinearLayout buttonContainer = (LinearLayout) mRoot.findViewById(R.id.popup_container);

        for (int i = 0; i < mButtonTitles.length; i++) {

            View popupButton = mInflater.inflate(R.layout.popup_button_layout, null);

            TextView button = (TextView) popupButton.findViewById(R.id.popup_button);
            button.setText(mButtonTitles[i]);
            button.setOnClickListener(mButtonClickListeners.get(i));

            buttonContainer.addView(popupButton);
        }

        RelativeLayout.LayoutParams subOverlayParams = (RelativeLayout.LayoutParams) subOverlay.getLayoutParams();
        subOverlayParams.leftMargin = mX;
        subOverlayParams.topMargin = mY;

        mTransparentView = (TransparentView) mRoot.findViewById(R.id.transparent_account_view);
        mTransparentView.setViewVisibility(View.VISIBLE);
        mTransparentView.setTransparentArea(
                mX - (int) UiUtils.convertDpToPixel(5, mContext),
                mY,
                mTouchedView.getWidth()
                        - (int) UiUtils.convertDpToPixel(12, mContext),
                mTouchedView.getHeight());

        /*
         * Should the popup get drawn off the screen, compensate for that based
         * upon the number of buttons being drawn.
         */
        boolean popupWillDisplayOffScreenBottom = ((mY + (mButtonTitles.length * UiUtils.convertDpToPixel(78, mContext))) > mScreenHeight) ? true : false;
        boolean popupWillDisplayOffScreenTop = (mY < 0) ? true : false;
        boolean popupWillDisplayOffScreenRight = ((mX + UiUtils.getMinimumPanalWidth((Activity) mContext) + 100) > mScreenWidth) ? true : false;

        if (popupWillDisplayOffScreenBottom) {
            subOverlayParams.topMargin = (int) (mScreenHeight - mButtonTitles.length * UiUtils.convertDpToPixel(76, mContext));
        }
        if (popupWillDisplayOffScreenTop) {
            subOverlayParams.topMargin = 0;
        }
        if (popupWillDisplayOffScreenRight) {
            subOverlayParams.leftMargin = (int) (mScreenWidth - (UiUtils.getMinimumPanalWidth((Activity) mContext) * 2.2));
            mTransparentView.setTransparentArea(subOverlayParams.leftMargin + subOverlayParams.width + mTouchedView.getWidth() + (int) UiUtils.convertDpToPixel(8, mContext), mY, mTouchedView.getWidth() + 10, mTouchedView.getHeight());
        }

        subOverlay.setLayoutParams(subOverlayParams);

        overlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOutTransparency();
            }
        });
    }


    public void fadeOutTransparency() {
        Animation dismissPopupAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_fast);
        dismissPopupAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
        mRoot.startAnimation(dismissPopupAnimation);
        mParentView.removeView(mRoot);
    }

}