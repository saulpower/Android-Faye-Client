package com.moneydesktop.finance.util;

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
}
