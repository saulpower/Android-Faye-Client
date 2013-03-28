package com.moneydesktop.finance.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.database.BankAccountBalance;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;

import java.util.*;

/**
 * MoneyDesktop - MoneyMobile
 *
 * User: saulhoward
 * Date: 3/28/13
 *
 * Description: Used for any report needed to be generated
 */
public class Reports {


    /**
     * Returns the daily expense totals for the last x amount of days.
     *
     * @param days The amount of days to go back
     *
     * @return
     */
    public static List<Transactions> getDailyExpenseTotals(Date date, int days) {

        String query = String.format(Constant.QUERY_DAILY_TRANSACTIONS, getCategoryExclusions());

        Date start = DateUtil.getPastDateByDays(date, days);

        SQLiteDatabase db = ApplicationContext.getDb();
        Cursor cursor = db.rawQuery(query, new String[]{
                Long.toString(start.getTime()), Long.toString(date.getTime())
        });

        List<Transactions> expenses = createTransactions(start, date, cursor, Enums.TransactionsReport.DAILY);

        cursor.close();

        return expenses;
    }

    public static List<Transactions> getDailyExpenseTotalsForBankAccount(Date date, int days, final String bankAccountId) {

        Date start = DateUtil.getPastDateByDays(date, days);

        String query = String.format(Constant.QUERY_DAILY_BALANCE_FOR_BANK_ACCOUNT, bankAccountId);

        SQLiteDatabase db = ApplicationContext.getDb();
        Cursor cursor = db.rawQuery(query, new String[]{
                Long.toString(start.getTime()), Long.toString(date.getTime())
        });

        List<Transactions> expenses = createTransactions(start, date, cursor, Enums.TransactionsReport.DAILY);

        cursor.close();

        return expenses;
    }

    public static List<BankAccountBalance> getDailyBalanceTotalsForBankAccount(Date date, int days, final String bankAccountId) {

        Date start = DateUtil.getPastDateByDays(date, days);

        String query = String.format(Constant.QUERY_DAILY_BALANCE_FOR_BANK_ACCOUNT, bankAccountId);

        SQLiteDatabase db = ApplicationContext.getDb();
        Cursor cursor = db.rawQuery(query, new String[]{
                Long.toString(start.getTime()), Long.toString(date.getTime())
        });

        List<BankAccountBalance> balances = createAccountBalances(start, date, cursor, Enums.AccountBalanceReport.DAILY);

        cursor.close();

        return balances;
    }

    public static List<Transactions> getMonthlyExpenseTotals(Date date, int months) {

        // We include the current month
        months--;

        String query = String.format(Constant.QUERY_MONTHLY_TRANSACTIONS, getCategoryExclusions());

        Calendar cal = DateUtils.toCalendar(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = cal.getTime();

        Date start = DateUtil.getPastDateByMonths(cal.getTime(), months);

        SQLiteDatabase db = ApplicationContext.getDb();
        Cursor cursor = db.rawQuery(query, new String[]{
                Long.toString(start.getTime()), Long.toString(cal.getTime().getTime())
        });

        cal = DateUtils.toCalendar(start);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        cursor.moveToFirst();

        List<Transactions> expenses = createTransactions(cal.getTime(), end, cursor,
                Enums.TransactionsReport.MONTHLY);

        cursor.close();

        return expenses;
    }

    public static List<Transactions> getYearlyExpenseTotals(int years) {

        // include the current year
        years--;

        String query = String.format(Constant.QUERY_YEARLY_TRANSACTIONS, getCategoryExclusions());

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));

        Date end = cal.getTime();

        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.YEAR, -years);

        Date start = cal.getTime();

        SQLiteDatabase db = ApplicationContext.getDb();
        Cursor cursor = db.rawQuery(query, new String[]{
                Long.toString(start.getTime()), Long.toString(end.getTime())
        });

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));

        cursor.moveToFirst();

        List<Transactions> expenses = createTransactions(cal.getTime(), end, cursor, Enums.TransactionsReport.YEARLY);

        cursor.close();

        return expenses;
    }

    /**
     * Returns the expense transaction totals associated with the last 4 quarters of transactions.
     *
     * @param quarters the number of quarters to report
     */
    public static List<Transactions> getQuarterlyExpenseTotals(Date date, int quarters) {

        // include the current quarter
        quarters--;

        List<Transactions> expenses = new ArrayList<Transactions>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -3 * quarters);

        String query = String.format(Constant.QUERY_QUARTERLY_TRANSACTIONS, getCategoryExclusions());

        SQLiteDatabase db = ApplicationContext.getDb();

        for (int counter = 0; counter < 4; counter++) {

            int quarter = DateUtil.getQuarterNumber(cal);

            Cursor cursor = db.rawQuery(query, new String[]{
                    Long.toString(cal.get(Calendar.YEAR)), Integer.toString(quarter)
            });

            double amount = 0.0;

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                amount = cursor.getDouble(0);
            }

            expenses.add(createExpense(cal.getTime(), amount, quarter));

            //Setting cal to the previous quarter
            cal.add(Calendar.MONTH, 3);

            cursor.close();
        }

        return expenses;
    }

    private static List<Transactions> createTransactions(Date start, Date end, Cursor cursor, Enums.TransactionsReport report) {

        Calendar current = DateUtils.toCalendar(start);

        List<Transactions> expenses = new ArrayList<Transactions>();

        Date date = null;

        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {

            date = new Date(cursor.getLong(0));
            double amount = cursor.getDouble(1);

            while (current.getTime().compareTo(date) < 0) {

                expenses.add(createExpense(current.getTime(), 0.0));

                incrementCalendar(current, report);
            }

            expenses.add(createExpense(date, amount));

            incrementCalendar(current, report);
            cursor.moveToNext();
        }

        while (DateUtils.truncatedCompareTo(current.getTime(), end, Calendar.DAY_OF_MONTH) <= 0) {

            expenses.add(createExpense(current.getTime(), 0.0));

            incrementCalendar(current, report);
        }

        // Sort the transactions so they are in order by date
        Collections.sort(expenses, new Comparator<Transactions>() {
            public int compare(Transactions t1, Transactions t2) {
                return t1.getDate().compareTo(t2.getDate());
            }
        });

        return expenses;
    }


    private static List<BankAccountBalance> createAccountBalances(Date start, Date end, Cursor cursor, Enums.AccountBalanceReport report) {

        Calendar current = DateUtils.toCalendar(start);

        List<BankAccountBalance> balances = new ArrayList<BankAccountBalance>();

        Date date = null;

        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {

            date = new Date(cursor.getLong(0));
            double amount = cursor.getDouble(1);

            balances.add(createAccountBalance(date, amount));

            incrementCalendar(current, report);
            cursor.moveToNext();
        }

        // Sort the accountBalances so they are in order by date
        Collections.sort(balances, new Comparator<BankAccountBalance>() {
            public int compare(BankAccountBalance t1, BankAccountBalance t2) {
                return t1.getDate().compareTo(t2.getDate());
            }
        });

        return balances;
    }



    private static void incrementCalendar(Calendar calendar, Enums.TransactionsReport report) {

        switch (report) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    private static void incrementCalendar(Calendar calendar, Enums.AccountBalanceReport report) {

        switch (report) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
        }
    }

    /**
     * Creates a sql query string that can be appended to a query to exclude transactions
     * based on income category and transfer category (if excluded).
     *
     * @return A sql query string
     */
    private static String getCategoryExclusions() {
        return getCategoryExclusions("");
    }

    /**
     * Creates a sql query string that can be appended to a query to exclude transactions
     * based on income category and transfer category (if excluded).
     *
     * @param categoryTableVariable The category table variable name if one exists (ex. CATEGORY C,
     *                              would provide 'C.' as the variable name)
     *
     * @return A sql query string
     */
    private static String getCategoryExclusions(String categoryTableVariable) {

        CategoryDao categoryDao = ApplicationContext.getDaoSession().getCategoryDao();
        List<Category> categories = categoryDao.loadAll();

        StringBuilder categoryFilter = new StringBuilder();

        for (Category category : categories) {

            if (category.isIncome() || category.isTransfer()) {
                categoryFilter.append(String.format("%sCATEGORY_ID != %d AND ", categoryTableVariable,
                        category.getId()));
            }
        }

        return categoryFilter.substring(0, (categoryFilter.length() - 4));
    }

    private static Transactions createExpense(Date date, double amount) {
        return createExpense(date, amount, -1);
    }

    private static Transactions createExpense(Date date, double amount, int quarter) {

        if (amount < 0f) {
            amount = 0f;
        }

        Transactions expense = new Transactions();
        expense.setDate(date);
        expense.setAmount(amount);
        expense.setQuarterNumber(quarter);

        return expense;
    }




    private static BankAccountBalance createAccountBalance(Date date, double amount) {

        if (amount < 0f) {
            amount = 0f;
        }

        BankAccountBalance balance = new BankAccountBalance();
        balance.setDate(date);
        balance.setBalance(amount);


        return balance;
    }
}
