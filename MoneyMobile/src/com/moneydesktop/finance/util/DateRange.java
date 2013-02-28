package com.moneydesktop.finance.util;

import java.util.Calendar;
import java.util.Date;

public class DateRange {
	
	private Calendar mCalendar;
	
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

		mCalendar = Calendar.getInstance();
	}
	
	public void getCurrentMonth() {
		
		mCalendar.setTime(new Date());
		mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		
		setStartDate(mCalendar.getTime());
		
		mCalendar.add(Calendar.MONTH, 1);
		mCalendar.add(Calendar.DAY_OF_MONTH, -1);
		
		setEndDate(mCalendar.getTime());
	}
}
