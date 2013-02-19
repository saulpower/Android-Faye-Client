package com.moneydesktop.finance.tablet.fragment;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.BasicBarChartAdapter;

public class TransactionsSummaryTabletFragment extends SummaryTabletFragment {
   BarGraphView mGraph;
   BasicBarChartAdapter mAdapter;
    public static TransactionsSummaryTabletFragment newInstance(int position) {
        
        TransactionsSummaryTabletFragment frag = new TransactionsSummaryTabletFragment();
        
        Bundle args = new Bundle();
        args.putInt("position",position);
        frag.setArguments(args);
        
        return frag;
    }

	@Override
	public FragmentType getType() {
		return FragmentType.TRANSACTION_SUMMARY;
	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_transaction_summary_view, null);
        
        mGraph = (BarGraphView) mRoot.findViewById(R.id.tablet_transaction_summary_graph);
        mAdapter = new BasicBarChartAdapter(new ArrayList());
        mGraph.setAdapter(mAdapter);
        setGraphViewMonthly(new Date());
        View daily = mRoot.findViewById(R.id.tablet_transaction_daily_button);
        daily.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){                
                 setGraphViewDaily(new Date());                
            }
            
        });
        View monthly = mRoot.findViewById(R.id.tablet_transaction_monthly_button);
        monthly.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){                
                 setGraphViewMonthly(new Date());                
            }
            
        });
        View quarterly = mRoot.findViewById(R.id.tablet_transaction_quarterly_button);
        quarterly.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){                
                 setGraphViewQuarterly(new Date());                
            }
            
        });
        View yearly = mRoot.findViewById(R.id.tablet_transaction_yearly_button);
        yearly.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){                
                 setGraphViewYearly(new Date());                
            }
            
        });
        
        return mRoot;
    }
    private void setGraphViewMonthly(Date end){
        List<Double[]> data = Transactions.getMonthlyExpenseTotals(end);
        double max = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i)[1] > max){
                max = data.get(i)[1];
            }
        }      
        
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("MMM yyyy");
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    StringBuffer date = new StringBuffer();
                    date = format.format(data.get(c)[0],date,new FieldPosition(0));
                    barList.add(new BarViewModel(date.toString(),data.get(c)[1],max));
                }              
            }
            mAdapter.setNewList(barList);
            mGraph.setMax(max);
            mGraph.setLabel(true);
            mGraph.setLabelFontSize(14);    
    }
    private void setGraphViewQuarterly(Date end){
        List<Double[]> data = Transactions.getQuarterlyExpenseTotals(end);
        double max = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i)[1] > max){
                max = data.get(i)[1];
            }
        }      
        
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    StringBuffer date = new StringBuffer();
                    date = format.format(data.get(c)[0],date,new FieldPosition(0));
                    int quarter = (int)(double)data.get(c)[2];
                    String addVal = getResources().getString(R.string.quarter) + ": " + Integer.toString(quarter) + ", " + date.toString();
                    barList.add(new BarViewModel(addVal,data.get(c)[1],max));
                }              
            }
            mAdapter.setNewList(barList);
            mGraph.setMax(max);
            mGraph.setLabel(true);
            mGraph.setLabelFontSize(14);    
    }
    private void setGraphViewYearly(Date end){
        List<Double[]> data = Transactions.getYearlyExpenseTotals(end);
        double max = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i)[1] > max){
                max = data.get(i)[1];
            }
        }      
        
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    StringBuffer date = new StringBuffer();
                    date = format.format(data.get(c)[0],date,new FieldPosition(0));
                    barList.add(new BarViewModel(date.toString(),data.get(c)[1],max));
                }              
            }
            mAdapter.setNewList(barList);
            mGraph.setMax(max);
            mGraph.setLabel(true);
            mGraph.setLabelFontSize(14);    
    }
    private void setGraphViewDaily(Date end) {
        List<Double[]> data = Transactions.get30DayExpenseTotals(end);
        double max = 0;
        for(int i = 0; i < data.size(); i++){
            if(data.get(i)[1] > max){
                max = data.get(i)[1];
            }
        }      
        
        ArrayList<BarViewModel> barList = new ArrayList<BarViewModel>();
        SimpleDateFormat format = new SimpleDateFormat("dd");
        for(int c = 0; c < data.size(); c++){
                if( data.get(c)[1] > 0){
                    StringBuffer date = new StringBuffer();
                    date = format.format(data.get(c)[0],date,new FieldPosition(0));
                    barList.add(new BarViewModel(date.toString(),data.get(c)[1],max));
                }              
            }
            mAdapter.setNewList(barList);
            mGraph.setMax(max);
            mGraph.setLabel(true);
            mGraph.setLabelFontSize(14);         
        }
    
    @Override
    public String getTitleText() {
        return getString(R.string.title_fragment_transaction_summary);
    }
}
