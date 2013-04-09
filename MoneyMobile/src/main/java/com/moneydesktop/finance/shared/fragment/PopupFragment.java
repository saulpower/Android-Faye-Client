package main.java.com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import de.greenrobot.event.EventBus;

public abstract class PopupFragment extends BaseFragment {

    protected PopupTabletActivity mPopupActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof PopupTabletActivity) {
            this.mPopupActivity = (PopupTabletActivity) activity;
        }
    }

    protected void dismissPopup() {

        if (mPopupActivity != null) {
            mPopupActivity.dismissPopup();
        } else {
            EventBus.getDefault().post(new EventMessage().new BackEvent());
        }
    }

    public void popupVisible() {}
}
