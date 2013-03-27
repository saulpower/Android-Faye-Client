package com.moneydesktop.finance.util;

import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class DateRange {
	
	private Calendar mCalendar;
	private Date mToday;
	
	private Date mStartDate;
	private Date mEndDate;
	
	public String getStartDateString() {
		return Long.toString(mStartDate.getTime());
	}
	
	public Date getStartDate() {
		return mStartDate;
	}
	
	public void setStartDate(Date mStartDate) {
		this.mStartDate = mStartDate;
	}

    public void setDayRange(Date start, int days) {

        days--;

        Calendar cal = DateUtils.toCalendar(start);
        cal.set(Calendar.HOUR_OF_DAY, 1);

        mStartDate = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, days);
        cal.set(Calendar.HOUR_OF_DAY, 23);

        mEndDate = cal.getTime();
    }

    public void setMonthRange(Date start, int months) {

        months--;

        Calendar cal = DateUtils.toCalendar(start);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        mStartDate = cal.getTime();

        cal.add(Calendar.MONTH, months);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        mEndDate = cal.getTime();
    }

    public void setQuarterRange(Date start, int quarters) {

        int quarter = getQuarterNumber(start);

        Calendar cal = DateUtils.toCalendar(start);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.MONTH, getQuarterStartMonth(quarter));

        mStartDate = cal.getTime();

        cal.add(Calendar.MONTH, quarters * 3 - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        mEndDate = cal.getTime();
    }

    public void setYearRange(Date start, int years) {

        years--;

        Calendar cal = DateUtils.toCalendar(start);
        cal.set(Calendar.DAY_OF_YEAR, 1);

        mStartDate = cal.getTime();

        cal.add(Calendar.YEAR, years);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));

        mEndDate = cal.getTime();
    }
	
	public String getEndDateString() {
		return Long.toString(mEndDate.getTime());
	}
	
	public Date getEndDate() {
		return mEndDate;
	}
	
	public void setEndDate(Date mEndDate) {
		this.mEndDate = mEndDate;
	}

	public static DateRange forCurrentMonth() {
		
		DateRange dateRange = new DateRange();
		dateRange.getCurrentMonth();
		
		return dateRange;
	}
	
	public DateRange() {

		mToday = new Date();
		
		mCalendar = Calendar.getInstance();
	}
	
	public DateRange(long start, long end) {
		this();
		
		mStartDate = new Date(start);
		mEndDate = new Date(end);
	}
	
	private void resetCalendar() {
		mCalendar.setTime(mToday);
		mCalendar.set(Calendar.HOUR_OF_DAY, 0);
		mCalendar.set(Calendar.MINUTE, 0);
		mCalendar.set(Calendar.SECOND, 0);
		mCalendar.set(Calendar.MILLISECOND, 0);
	}
	
	public void getCurrentMonth() {
		
		resetCalendar();
		
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		setStartDate(mCalendar.getTime());
		
		mCalendar.add(Calendar.MONTH, 1);
		mCalendar.add(Calendar.DAY_OF_MONTH, -1);
		mCalendar.set(Calendar.HOUR_OF_DAY, 24);
		mCalendar.set(Calendar.MINUTE, 59);
		
		setEndDate(mCalendar.getTime());
	}
	
	public void addMonthsToStart(int months) {
		
		resetCalendar();
		
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mCalendar.add(Calendar.MONTH, months);
		
		setStartDate(mCalendar.getTime());
	}

    /**
     * Returns the Quarter Number associated with a Date as an int. Used by getQuarterlyExpenseTotals
     *
     * @param date the date  to get the quarter number for
     *
     * @return quarter number
     */
    public static int getQuarterNumber(Date date) {
        return getQuarterNumber(DateUtils.toCalendar(date));
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
