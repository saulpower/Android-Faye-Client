package main.java.com.moneydesktop.finance.util;

import java.util.Calendar;
import java.util.Date;

/**
 * MoneyDesktop - MoneyMobile
 * <p/>
 * User: saulhoward
 * Date: 3/28/13
 * <p/>
 * Description:
 */
public class DateUtil {

    public static Date getPastDateByDays(Date date, int days) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, -days);

        return cal.getTime();
    }

    public static Date getPastDateByMonths(Date date, int months) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -months);

        return cal.getTime();
    }

    /**
     * Returns the Quarter Number associated with a Date as an int. Used by getQuarterlyExpenseTotals
     *
     * @param date the date  to get the quarter number for
     *
     * @return quarter number
     */
    public static int getQuarterNumber(Date date) {
        return getQuarterNumber(org.apache.commons.lang.time.DateUtils.toCalendar(date));
    }

    /**
     * Returns the Quarter Number associated with a Date as an int. Used by getQuarterlyExpenseTotals
     *
     * @param calendar the date  to get the quarter number for
     */
    public static int getQuarterNumber(Calendar calendar) {

        if (calendar.get(Calendar.MONTH) >= 0 && calendar.get(Calendar.MONTH) <= 2) {
            return 1;
        }

        if (calendar.get(Calendar.MONTH) >= 3 && calendar.get(Calendar.MONTH) <= 5) {
            return 2;
        }

        if (calendar.get(Calendar.MONTH) >= 6 && calendar.get(Calendar.MONTH) <= 8) {
            return 3;
        }

        if (calendar.get(Calendar.MONTH) >= 9 && calendar.get(Calendar.MONTH) <= 11) {
            return 4;
        }

        return -1;
    }

    public static int getQuarterStartMonth(int quarter) {

        switch (quarter) {
            case 1:
                return 0;
            case 2:
                return 3;
            case 3:
                return 6;
            case 4:
                return 9;
        }

        return -1;
    }
}
