package main.java.com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.shared.fragment.BaseFragment;
import main.java.com.moneydesktop.finance.shared.fragment.CalendarFragment;
import main.java.com.moneydesktop.finance.shared.fragment.TransactionDetailBaseFragment;

import java.util.ArrayList;
import java.util.List;

public class TransactionDetailHandsetFragment extends TransactionDetailBaseFragment {

    public final String TAG = this.getClass().getSimpleName();

    private int mFragmentResource = R.id.transactions_fragment;
    private LinearLayout mContainer;

    public void setFragmentResource(int resource) {
        mFragmentResource = resource;
    }

    public static TransactionDetailHandsetFragment newInstance(Activity activity, int fragmentResource) {

        TransactionDetailHandsetFragment frag = new TransactionDetailHandsetFragment();
        frag.inflateView(activity);
        frag.setFragmentResource(fragmentResource);

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.TRANSACTION_DETAIL;
    }

    public void inflateView(Activity activity) {
        mRoot = activity.getLayoutInflater().inflate(R.layout.handset_transaction_detail_view, null);
        initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (mRoot != null) {

            View oldParent = (View) mRoot.getParent();

            if (oldParent != null && oldParent != container) {
                ((ViewGroup) oldParent).removeView(mRoot);
            }

            return mRoot;

        } else {

            mRoot = inflater.inflate(R.layout.handset_transaction_detail_view, null);
            initialize();
        }

        return mRoot;
    }

    @Override
    protected void initialize() {
        super.initialize();

        mContainer = (LinearLayout) mRoot.findViewById(R.id.root);
    }

    public void setTransactionId(long guid) {
        getArguments().putLong(Constant.KEY_GUID, guid);
        loadTransaction();
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_fragment_transaction).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    protected void showFragment(BaseFragment fragment) {
        mActivity.pushFragment(mFragmentResource, fragment);
    }

    @Override
    protected void selectDate(View view) {
        showFragment(CalendarFragment.newInstance(mTransaction.getId()));
    }

    @Override
    public void isShowing() {
        super.isShowing();

        setupMenuItems();
    }

    private void setupMenuItems() {

        List<Pair<Integer, List<int[]>>> menu = new ArrayList<Pair<Integer, List<int[]>>>();

        List<int[]> communication = new ArrayList<int[]>();
        communication.add(new int[]{R.string.nav_icon_email, R.string.label_email_transaction});
        menu.add(new Pair<Integer, List<int[]>>(R.string.label_communication, communication));

        if (mTransaction.getIsManual()) {
            List<int[]> edit = new ArrayList<int[]>();
            edit.add(new int[]{R.string.nav_icon_trash, R.string.label_delete_transaction});
            menu.add(new Pair<Integer, List<int[]>>(R.string.label_edit, edit));
        }

        mActivity.configureRightMenu(menu, getType());
    }

    public void onEvent(EventMessage.MenuEvent event) {

        if (event.getFragmentType().equals(getType())) {
            switch (event.getAction()) {
                case ((0 << 8) + 0):
                    emailTransaction(mContainer);
                    break;
                case ((1 << 8) + 0):
                    confirmDeleteTransaction();
                    break;
            }
        }
    }

    @Override
    protected void deleteTransaction() {
        super.deleteTransaction();

        mActivity.popFragment();
    }
}
