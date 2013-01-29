package com.moneydesktop.finance.test;

import java.util.List;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.data.Enums.DataState;
import com.moneydesktop.finance.database.AccountType;
import com.moneydesktop.finance.database.AccountTypeDao;
import com.moneydesktop.finance.database.AccountTypeDao.Properties;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankAccount;
import com.moneydesktop.finance.database.BankAccountDao;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.database.DaoSession;
import com.moneydesktop.finance.handset.activity.DashboardHandsetActivity;

public class MoneyMobileTest extends ActivityInstrumentationTestCase2<DashboardHandsetActivity> {

	public static final String TAG = "MoneyMobileTest";
	
	private DashboardHandsetActivity activity;
	private DaoSession session;
	
	public MoneyMobileTest() {
		super("com.moneydesktop.finance.activity.handset", DashboardHandsetActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		activity = getActivity();
		getInstrumentation().getTargetContext().getApplicationContext();
		
		session = ApplicationContext.getDaoSession();
	}
	
//	private void addBankAccount() {
//
//		// Remove old version of this bank account
//		BankAccountDao baDao = session.getBankAccountDao();
//		List<BankAccount> bankAccounts = baDao.queryBuilder().where(com.moneydesktop.finance.database.BankAccountDao.Properties.AccountNumber.eq("1234567890")).list();
//		
//		baDao.deleteInTx(bankAccounts);
//		
//		BankDao bankDao = session.getBankDao();
//		List<Bank> banks = bankDao.loadAll();
//		
//		// Set the account type to "Checking"
//		AccountTypeDao atDao = session.getAccountTypeDao();
//		AccountType accountType = atDao.queryBuilder().where(Properties.AccountTypeId.eq("1")).unique();
//		
//		Bank bank = banks.get(0);
//		
//		BankAccount bankAccount = new BankAccount(null);
//		bankAccount.setAccountName("My Test Account");
//		bankAccount.setAccountNumber("1234567890");
//		bankAccount.setAccountType(accountType);
//		bankAccount.setBalance(9500.50);
//		bankAccount.setBank(bank);
//		
//		bankAccount.insertBatch();
//		
//		DataController.save();
//	}
//	
//	public void testInsertSync() {
//		
//		addBankAccount();
//		
//		SyncEngine.sharedInstance().debugSync();
//		
//		BankAccountDao baDao = session.getBankAccountDao();
//		BankAccount bankAccount = baDao.queryBuilder().where(com.moneydesktop.finance.database.BankAccountDao.Properties.AccountNumber.eq("1234567890")).unique();
//		
//		assertNotNull(bankAccount);
//		assertEquals("My Test Account", bankAccount.getAccountName());
//		assertNotNull(bankAccount.getAccountId());
//		assertEquals(DataState.DATA_STATE_UNCHANGED, bankAccount.getBusinessObjectBase().getDataStateEnum());
//		
//		Log.i(TAG, "AccountId: " + bankAccount.getAccountId());
//	}
}
