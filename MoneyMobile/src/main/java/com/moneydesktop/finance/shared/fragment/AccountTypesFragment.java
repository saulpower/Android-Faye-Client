package main.java.com.moneydesktop.finance.shared.fragment;


import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.data.SyncEngine;
import main.java.com.moneydesktop.finance.database.*;

import java.util.List;

public abstract class AccountTypesFragment extends BaseFragment{

    public final String TAG = this.getClass().getSimpleName();
    protected QueryProperty mBusinessObjectBaseTable = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BankDao.Properties.BusinessObjectId, BusinessObjectBaseDao.Properties.Id);
    protected QueryProperty mBankAccountTable = new QueryProperty(BankAccountDao.TABLENAME, AccountTypeDao.Properties.BusinessObjectId, BankAccountDao.Properties.BusinessObjectId);

    protected QueryProperty mWhereDataState = new QueryProperty(BusinessObjectBaseDao.TABLENAME, BusinessObjectBaseDao.Properties.DataState, "!= ?");
    protected QueryProperty mWhereBankName = new QueryProperty(BankDao.TABLENAME, BankDao.Properties.BankName, "!= ?");
    protected QueryProperty mAccountTypeWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
    protected QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);

    protected List<Bank> mBankList;

    @Override
    public FragmentType getType() {
        return FragmentType.ACCOUNT_TYPES;
    }

    protected void getAllBanks() {
        BankDao bankDao = ApplicationContext.getDaoSession().getBankDao();
        PowerQuery query = new PowerQuery(bankDao);

        query.join(mBusinessObjectBaseTable)
        .where(mWhereDataState, "3");

        mBankList = bankDao.queryRaw(query.toString(), query.getSelectionArgs());
    }


    /**
     * Updates status for all banks.
     */
    public void updateAllBankStatus() {
        for (Bank bank : mBankList) {
            SyncEngine.sharedInstance().beginBankStatusUpdate(bank);
        }
    }

    /**
     * Updates status for given bank.
     */
    public void updateBankStatus(Bank bank) {
        SyncEngine.sharedInstance().beginBankStatusUpdate(bank);
    }


}
