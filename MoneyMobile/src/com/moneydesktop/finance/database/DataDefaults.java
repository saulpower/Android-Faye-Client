package com.moneydesktop.finance.database;

import java.util.List;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;

public class DataDefaults {
	
	/**
	 * Create default Category Types
	 */
	public static void ensureCategoryTypesLoaded() {
		
		CategoryTypeDao ctDao = ApplicationContext.getDaoSession().getCategoryTypeDao();
		List<CategoryType> categoryTypes = ctDao.queryBuilder().list();
		
		if (categoryTypes.size() == 0) {
			
			CategoryType.createCategoryType("2", ApplicationContext.getContext().getString(R.string.ct_exp)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("1", ApplicationContext.getContext().getString(R.string.ct_inc)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("4", ApplicationContext.getContext().getString(R.string.ct_asst)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("8", ApplicationContext.getContext().getString(R.string.ct_liab)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("16", ApplicationContext.getContext().getString(R.string.ct_equity)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("64", ApplicationContext.getContext().getString(R.string.ct_flow)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("128", ApplicationContext.getContext().getString(R.string.ct_bal)).insertBatch().acceptChanges();
			CategoryType.createCategoryType("32", ApplicationContext.getContext().getString(R.string.ct_stat)).insertBatch().acceptChanges();
		}
	}

	/**
	 * Create default Account Types
	 */
	public static void ensureAccountTypesLoaded() {
		
		AccountTypeDao atDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		List<AccountType> accountTypes = atDao.queryBuilder().list();
		
		if (accountTypes.size() == 0) {
			
			AccountType.createAccountType("0", ApplicationContext.getContext().getString(R.string.at_unknown), 1, 0, null).insertBatch().acceptChanges();
			AccountType.createAccountType("1", ApplicationContext.getContext().getString(R.string.at_checking), 1, 0, null).insertBatch().acceptChanges();
			AccountType.createAccountType("2", ApplicationContext.getContext().getString(R.string.at_saving), 1, 0, null).insertBatch().acceptChanges();
			AccountType.createAccountType("3", ApplicationContext.getContext().getString(R.string.at_loans), 1, 1, null).insertBatch().acceptChanges();
			AccountType.createAccountType("4", ApplicationContext.getContext().getString(R.string.at_cc), 1, 1, null).insertBatch().acceptChanges();
			AccountType.createAccountType("5", ApplicationContext.getContext().getString(R.string.at_inv), 1, 0, null).insertBatch().acceptChanges();
			AccountType.createAccountType("6", ApplicationContext.getContext().getString(R.string.at_loc), 1, 1, null).insertBatch().acceptChanges();
			AccountType.createAccountType("7", ApplicationContext.getContext().getString(R.string.at_mort), 1, 1, null).insertBatch().acceptChanges();
			AccountType.createAccountType("8", ApplicationContext.getContext().getString(R.string.at_prop), 1, 0, null).insertBatch().acceptChanges();
			AccountType.createAccountType("8.2", ApplicationContext.getContext().getString(R.string.at_art), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.0", ApplicationContext.getContext().getString(R.string.at_re), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.1", ApplicationContext.getContext().getString(R.string.at_veh), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.3", ApplicationContext.getContext().getString(R.string.at_jew), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.4", ApplicationContext.getContext().getString(R.string.at_fur), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.5", ApplicationContext.getContext().getString(R.string.at_app), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.6", ApplicationContext.getContext().getString(R.string.at_comp), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.7", ApplicationContext.getContext().getString(R.string.at_elec), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.8", ApplicationContext.getContext().getString(R.string.at_se), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("8.9", ApplicationContext.getContext().getString(R.string.at_misc), 1, 0, "8").insertBatch().acceptChanges();
			AccountType.createAccountType("9", ApplicationContext.getContext().getString(R.string.at_cash), 1, 0, null).insertBatch().acceptChanges();
		}
	}
	
	/**
	 * Create default Account Type Groups
	 */
	public static void ensureAccountTypeGroupsLoaded() {
		
		AccountTypeGroupDao atgDao = ApplicationContext.getDaoSession().getAccountTypeGroupDao();
		List<AccountTypeGroup> accountTypeGroups = atgDao.queryBuilder().list();
		
		if (accountTypeGroups.size() == 0) {
			
			AccountTypeGroup.createAccountTypeGroup("INVST", ApplicationContext.getContext().getString(R.string.atg_inv), "brokerage.png", 99).insertBatch().acceptChanges();
			AccountTypeGroup.createAccountTypeGroup("PROP", ApplicationContext.getContext().getString(R.string.atg_prop), "house.png", 3).insertBatch().acceptChanges();
			AccountTypeGroup.createAccountTypeGroup("SPEND", ApplicationContext.getContext().getString(R.string.atg_cash), "cash.png", 0).insertBatch().acceptChanges();
			AccountTypeGroup.createAccountTypeGroup("DEBT", ApplicationContext.getContext().getString(R.string.atg_ccd), "cc.png", 1).insertBatch().acceptChanges();
			AccountTypeGroup.createAccountTypeGroup("ODEBT", ApplicationContext.getContext().getString(R.string.atg_od), "cash.png", 2).insertBatch().acceptChanges();
		}
	}
}
