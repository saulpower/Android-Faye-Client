package main.java.com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.database.Bank;
import main.java.com.moneydesktop.finance.shared.fragment.FixBankFragment;
import main.java.com.moneydesktop.finance.util.Fonts;

public class AccountOptionFixBankHandsetFragment extends FixBankFragment{

    private Bank mBank;
    private TextView mTitle, mMessage, mContinue;

    public void setBank(Bank mBank) {
        this.mBank = mBank;
    }

    @Override
    public FragmentType getType() {
        return null;
    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public static AccountOptionFixBankHandsetFragment newInstance(Bank bank) {

        AccountOptionFixBankHandsetFragment frag = new AccountOptionFixBankHandsetFragment();
        frag.setBank(bank);

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.handset_account_option_fix_bank_view, null);

        mTitle = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_title);
        mMessage = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_description);
        mContinue = (TextView)mRoot.findViewById(R.id.handset_bank_broken_notification_continue);

        Fonts.applyPrimaryBoldFont(mTitle, 18);
        Fonts.applyPrimaryBoldFont(mMessage, 10);
        Fonts.applyPrimaryBoldFont(mTitle, 14);

        setupView();

        return mRoot;
    }

    private void setupView() {

        if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_MFA.index())) {
            mMessage.setText(getString(R.string.status_description_6));
            mContinue.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    loadMfaFragment();
                }
            });
        } else if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_LOGIN_FAILED.index())) {
            mMessage.setText(getString(R.string.status_description_5));
            mContinue.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    loadCredentialsFragment();
                }
            });
        } else if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_EXCEPTION.index())) {
            mContinue.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    loadCredentialsFragment();
                }
            });
        }
    }

    private void loadMfaFragment() {
        AccountOptionMfaQuestionHandsetFragment frag = AccountOptionMfaQuestionHandsetFragment.newInstance(mBank);
        mActivity.pushFragment(getId(), frag);
    }

    private void loadCredentialsFragment() {
        AccountOptionsCredentialsHandsetFragment frag = AccountOptionsCredentialsHandsetFragment.newInstance(mBank);
        mActivity.pushFragment(getId(), frag);
    }

}
