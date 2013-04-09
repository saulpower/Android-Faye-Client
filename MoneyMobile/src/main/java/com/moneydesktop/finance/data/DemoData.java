package main.java.com.moneydesktop.finance.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.AccountExclusionFlags;
import main.java.com.moneydesktop.finance.database.AccountType;
import main.java.com.moneydesktop.finance.database.Bank;
import main.java.com.moneydesktop.finance.database.BankAccount;
import main.java.com.moneydesktop.finance.database.BudgetItem;
import main.java.com.moneydesktop.finance.database.BusinessObject;
import main.java.com.moneydesktop.finance.database.Category;
import main.java.com.moneydesktop.finance.database.CategoryType;
import main.java.com.moneydesktop.finance.database.DatabaseDefaults;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.util.FileIO;

public class DemoData {

    public static final String TAG = "DemoData";

    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }

    // Category indexes
    private static final int CATEGORY_ID         = 0,
                             PARENT_ID             = 1,
                             NAME                 = 2,
                             CATEGORY_TYPE         = 3,
                             BUDGET_AMOUNT         = 5;

    // Bank indexes
    private static final int BANK_ID             = 0,
                             BANK_NAME             = 1,
                             IMAGE_NAME             = 2;

    // Bank Account indexes
    private static final int ACCOUNT_ID             = 0,
                             ACCOUNT_NAME         = 1,
                             ACT_BANK_ID         = 2,
                             ACCOUNT_TYPE         = 3,
                             BALANCE             = 4;

    // Transaction indexes
    private static final int BANK_ACCOUNT_ID     = 0,
                             ORIGINAL_TITLE         = 1,
                             TITLE                 = 2,
                             AMOUNT                 = 3,
                             REF                 = 4,
                             TRN_CAT_ID             = 5,
                             DAY_OFFSET             = 7;


    public static void createDemoData() {

        long start = System.currentTimeMillis();
        Log.i(TAG, "Started creating demo data");

        Preferences.saveBoolean(Preferences.KEY_IS_DEMO_MODE, true);

        DatabaseDefaults.ensureCategoryTypesLoaded();
        DatabaseDefaults.ensureAccountTypeGroupsLoaded();
        DatabaseDefaults.ensureAccountTypesLoaded();

        processCategories();
        processBanks();

        BankAccount.buildAccountBalances();

        loaded = true;

        Log.i(TAG, "Demo data created in " + (System.currentTimeMillis() - start) + " ms");
    }

    /*******************************************************************************
     * CATEGORY PROCESSING
     *******************************************************************************/

    private static void processCategories() {

        List<String[]> categories = importFile(R.raw.categories);
        Map<String, List<String[]>> index = makeIndex(categories, PARENT_ID);

        if (categories != null) {

            for (String[] category : categories) {

                // Only process categories without a parent id as those
                // with a parent id will be processed as children in the
                // processChildrenCategories method.
                if (category[PARENT_ID].length() <= 2) {

                    Category cat = createCategory(category, null);

                    if (index.containsKey(cat.getCategoryId())) {
                        processChildrenCategories(cat, index.get(cat.getCategoryId()));
                    }

                    cat.acceptChanges();
                }
            }
        }
    }

    private static void processChildrenCategories(Category parent, List<String[]> children) {

        for (String[] child : children) {

            Category category = createCategory(child, parent);
            category.setIgnoreWillSave(true);

            category.acceptChanges();
        }
    }

    private static Category createCategory(String[] row, Category parent) {

        String guid = row[CATEGORY_ID];

        Category category = new Category(Long.valueOf(guid.hashCode()));
        if (parent != null) {
            category.setParent(parent);
        }
        category.setCategoryId(guid);
        category.setCategoryName(row[NAME]);

        CategoryType type = (CategoryType) BusinessObject.getObject(CategoryType.class, row[CATEGORY_TYPE]);
        if (type != null) {
            category.setCategoryTypeId(type.getId());
        }

        String imageName = Category.getIconForId(category);
        category.setImageName(imageName);
        category.setIsSystem(true);

        if (row.length > BUDGET_AMOUNT) {

            double budgetAmount = Double.parseDouble(row[BUDGET_AMOUNT]);

            if (budgetAmount > 0)
                createBudgetItem(budgetAmount, category);
        }

        category.insertBatch();

        return category;
    }

    private static void createBudgetItem(double amount, Category category) {

        BudgetItem budgetItem = new BudgetItem(null);
        budgetItem.setAmount(amount);
        budgetItem.setCategory(category);
        budgetItem.setIgnoreWillSave(true);
        budgetItem.setIsActive(true);
        budgetItem.setIsDefault(true);

        budgetItem.insertBatch();
        budgetItem.acceptChanges();
    }


    /*******************************************************************************
     * BANK PROCESSING
     *******************************************************************************/

    private static void processBanks() {

        List<String[]> banks = importFile(R.raw.banks);
        List<String[]> bankAccounts = importFile(R.raw.bankaccounts);
        List<String[]> transactions = importFile(R.raw.txns);

        Map<String, List<String[]>> txnIndex = makeIndex(transactions, BANK_ACCOUNT_ID);
        Map<String, List<String[]>> baIndex = makeIndex(bankAccounts, ACT_BANK_ID);

        for (String[] bankRow : banks) {

            Bank bank = createBank(bankRow);

            if (baIndex.containsKey(bank.getBankId())) {

                for (String[] bankAccountInfo : baIndex.get(bank.getBankId()))
                    processBankAccount(bank, bankAccountInfo, txnIndex);
            }

            bank.acceptChanges();
        }

        // Process any bank accounts not associated with a bank
        for (String[] row : bankAccounts) {

            if (row[ACT_BANK_ID].length() < 2)
                processBankAccount(null, row, txnIndex);
        }
    }

    private static Bank createBank(String[] row) {

        String guid = row[BANK_ID];

        Bank bank = new Bank(Long.valueOf(guid.hashCode()));
        bank.setBankId(guid);
        bank.setDefaultClassId(Constant.PERSONAL);
        bank.setIsLinked(true);
        bank.setBankName(row[BANK_NAME]);
        bank.setLogoId(row[IMAGE_NAME]);
        bank.setProcessStatus(3);

        bank.insertBatch();

        return bank;
    }

    private static void processBankAccount(Bank bank, String[] bankAccountInfo, Map<String, List<String[]>> txnIndex) {

        BankAccount bankAccount = createBankAccount(bank, bankAccountInfo);

        if (txnIndex.containsKey(bankAccount.getAccountId())) {

            processTransactions(bankAccount, txnIndex.get(bankAccount.getAccountId()));
        }

        bankAccount.acceptChanges();
    }

    private static BankAccount createBankAccount(Bank bank, String[] row) {

        String guid = row[ACCOUNT_ID];

        BankAccount bankAccount = new BankAccount(Long.valueOf(guid.hashCode()));
        bankAccount.setAccountId(guid);
        bankAccount.setAccountName(row[ACCOUNT_NAME]);
        bankAccount.setBalance(Double.parseDouble(row[BALANCE]));
        bankAccount.setExclusionFlags((AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_EXPENSES.index() |
                                      AccountExclusionFlags.ACCOUNT_EXCLUSION_FLAGS_TRANSFERS_FROM_INCOME.index()));

        if (bank != null) {
            bankAccount.setBankName(bank.getBankName());
            bankAccount.setInstitutionId(bank.getBankId());
            bankAccount.setBank(bank);
        }

        bankAccount.setIsHolding(false);
        bankAccount.setIsLinked(false);
        bankAccount.setDefaultClassId(Constant.PERSONAL);

        AccountType accountType = (AccountType) BusinessObject.getObject(AccountType.class, row[ACCOUNT_TYPE]);
        if (accountType != null) {

            bankAccount.setAccountTypeId(accountType.getId());

            accountType.acceptChanges();
            accountType.updateBatch();
        }


        bankAccount.insertBatch();

        return bankAccount;
    }

    private static void processTransactions(BankAccount bankAccount, List<String[]> transactions) {

        for (String[] txnInfo : transactions) {

            Category category = (Category) BusinessObject.getObject(Category.class, txnInfo[TRN_CAT_ID]);

            if (category != null) {

                createTransaction(txnInfo, bankAccount, category);
            }
        }
    }

    private static Transactions createTransaction(String[] row, BankAccount bankAccount, Category category) {

        Transactions transaction = new Transactions(null);
        transaction.setBankAccountId(bankAccount.getId());
        transaction.setIsCleared(true);
        transaction.setIsMatched(true);
        transaction.setIsSplit(false);
        transaction.setIsProcessed(false);
        transaction.setIsReported(false);
        transaction.setIsVoid(false);
        transaction.setIsFlagged(false);
        transaction.setHasReceipt(false);
        transaction.setTitle(row[TITLE]);
        transaction.setOriginalTitle(row[ORIGINAL_TITLE]);
        transaction.setReference(row[REF]);
        transaction.setAmount(Double.parseDouble(row[AMOUNT]));
        transaction.setRawAmount(transaction.getAmount());
        transaction.setAmountReimbursable(transaction.getAmount());
        transaction.setIsBusiness(false);
        transaction.setIsExcluded(false);
        transaction.setIgnoreWillSave(true);
        transaction.setCategory(category);
        transaction.setTransactionType(transaction.isIncome() ? 1 : 2);

        int dayOffset = Integer.parseInt(row[DAY_OFFSET]);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, (dayOffset * -1));
        Date date = cal.getTime();

        transaction.setDate(date);
        transaction.setYearNumber(cal.get(Calendar.YEAR));
        transaction.setMonthNumber((cal.get(Calendar.MONTH) + 1));
        transaction.setQuarterNumber(((cal.get(Calendar.MONTH) / 3) + 1));
        transaction.setWeekNumber(cal.get(Calendar.WEEK_OF_YEAR));
        transaction.setDayNumber(cal.get(Calendar.DAY_OF_MONTH));

        transaction.insertBatch();
        transaction.acceptChanges();

        return transaction;
    }


    /*******************************************************************************
     * HELPER METHODS
     *******************************************************************************/

    private static List<String[]> importFile(int resource) {

        try {

            return FileIO.loadCSV(resource);

        } catch (IOException ex) {
            Log.e(TAG, "Could not import csv file", ex);
            return null;
        }
    }

    private static Map<String, List<String[]>> makeIndex(List<String[]> list, int index) {

        Map<String, List<String[]>> map = new HashMap<String, List<String[]>>();

        for (String[] item : list) {

            String parentId = item[index];

            if (parentId.length() > 2) {

                if (map.containsKey(parentId)) {

                    map.get(parentId).add(item);

                } else {

                    List<String[]> temp = new ArrayList<String[]>();
                    temp.add(item);
                    map.put(parentId, temp);
                }
            }
        }

        return map;
    }
}