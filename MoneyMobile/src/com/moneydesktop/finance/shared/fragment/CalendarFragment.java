package com.moneydesktop.finance.shared.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.flurry.android.FlurryAgent;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends PopupFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private CalendarPickerView mCalendar;

    private Transactions mTransaction;
    
    public static CalendarFragment newInstance(long id) {

        CalendarFragment fragment = new CalendarFragment();
        
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_ID, id);
        fragment.setArguments(args);
        
        return fragment;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.CALENDAR;
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.calendar_view, null);

        long id = getArguments().getLong(Constant.KEY_ID);
        mTransaction = (Transactions) DataController.getDao(Transactions.class).load(id);

        setupView();
        setupCalendar();

        // Log the user has set the date of a transaction
        FlurryAgent.logEvent("" + getType());
        
        return mRoot;
    }
    
    @Override
    public void popupVisible() {
        mCalendar.scrollToInitialMonth();
    }

    @Override
    public void isShowing() {
        super.isShowing();

        mCalendar.scrollToInitialMonth();
    }
    
    private void setupView() {

        mCalendar = (CalendarPickerView) mRoot.findViewById(R.id.calendar_view);
        mCalendar.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                updateTransaction();
            }
        });
    }

    private void setupCalendar() {

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.MONTH, -2);
        minDate.set(Calendar.DAY_OF_MONTH, 1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.MONTH, 2);
        maxDate.set(Calendar.DAY_OF_MONTH, 1);

        mCalendar.init(mTransaction.getDate(), minDate.getTime(), maxDate.getTime());
    }

    private void updateTransaction() {

        mTransaction.setDate(mCalendar.getSelectedDate());
        mTransaction.updateSingle();

        dismissPopup();
    }

    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_fragment_calendar).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
