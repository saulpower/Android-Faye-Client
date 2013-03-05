package com.moneydesktop.finance.shared.fragment;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.database.BusinessObjectBaseDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.handset.adapter.TransactionsHandsetAdapter;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.model.EventMessage.FilterEvent;
import com.moneydesktop.finance.model.EventMessage.GetLogonCredentialsFinished;
import com.moneydesktop.finance.model.EventMessage.MfaQuestionsRecieved;
import com.moneydesktop.finance.model.EventMessage.SyncEvent;
import com.moneydesktop.finance.model.EventMessage.UpdateSpecificBankStatus;
import com.moneydesktop.finance.shared.Services.SyncService;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter;
import com.moneydesktop.finance.shared.adapter.TransactionsAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.tablet.fragment.TransactionsPageTabletFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;

import de.greenrobot.event.EventBus;

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
