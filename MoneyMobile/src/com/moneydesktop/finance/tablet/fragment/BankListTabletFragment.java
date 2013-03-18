package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.adapter.BankAccountTabletAdapter;
import com.moneydesktop.finance.views.navigation.AnimatedNavView;
import com.moneydesktop.finance.views.navigation.AnimatedNavView.NavigationListener;

import java.util.List;

public class BankListTabletFragment extends BaseFragment implements NavigationListener, AdapterView.OnItemClickListener {

    private ListView mBanks;
    private List<BankAccount> mBankList;
    private AnimatedNavView mNavView;

	@Override
	public String getFragmentTitle() {
		return mActivity.getString(R.string.title_fragment_add_transaction).toUpperCase();
	}

	@Override
	public boolean onBackPressed() {

        ((DropDownTabletActivity)mActivity).dismissDropdown();

		return true;
	}

	public static BankListTabletFragment newInstance() {

        BankListTabletFragment mFragment = new BankListTabletFragment();

        Bundle args = new Bundle();
        mFragment.setArguments(args);

        return mFragment;
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mNavView = ((DropDownTabletActivity) mActivity).getAnimatedNavView();

        mRoot = inflater.inflate(R.layout.tablet_manual_bank_list, null);

        setupView();
        setupListAdapter();

        return mRoot;
    }

    private void setupListAdapter() {

        BankAccountDao dao = (BankAccountDao) DataController.getDao(BankAccount.class);
        mBankList = dao.queryBuilder().where(BankAccountDao.Properties.IsLinked.eq(Boolean.valueOf(false))).list();

        BankAccountTabletAdapter adapter = new BankAccountTabletAdapter(mActivity, mBankList);
        mBanks.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        mNavView.setNavigationListener(this);
    }

    private void setupView() {
        mBanks = (ListView) mRoot.findViewById(R.id.bank_list);
        mBanks.setOnItemClickListener(this);
    }

	@Override
	public FragmentType getType() {
		return FragmentType.MANUAL_BANK_LIST;
	}

	@Override
	public void onNavigationPopped() {

	}

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        BankAccount account = (BankAccount) view.getTag();

        if (account != null) {

            addNewTransaction(account);

        } else {

            addManualAccount();
        }
    }

    private void addNewTransaction(BankAccount account) {

        Transactions transactions = Transactions.createNewTransaction(account);
        transactions.insertSingle();

        ((DropDownTabletActivity)mActivity).dismissDropdown();
    }

    private void addManualAccount() {

        BaseFragment fragment = AddBankTabletFragment.newInstance(true);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.in_right, R.anim.out_left, R.anim.in_left, R.anim.out_right);
        ft.replace(R.id.fragment, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}