package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.BarViewModel;
import com.moneydesktop.finance.views.BarGraphView;
import com.moneydesktop.finance.views.BaseBarChartAdapter;
import com.moneydesktop.finance.views.BasicBarChartAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionsSummaryTabletFragment extends SummaryTabletFragment {
    public static TransactionsSummaryTabletFragment newInstance(int position) {   
        TransactionsSummaryTabletFragment frag = new TransactionsSummaryTabletFragment();
        Bundle args = new Bundle();
        args.putInt("position",position);
        frag.setArguments(args);
        
        return frag;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        TransactionsDao transactions = ApplicationContext.getDaoSession().getTransactionsDao();
        mRoot = inflater.inflate(R.layout.tablet_transaction_summary_view, null);
        /*mRoot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                
                 mActivity.showFragment(FragmentType.TRANSACTIONS);
                
            }
            
        });*/
        
        setupViews();
        configureView();
        BarGraphView v = (BarGraphView) mRoot.findViewById(R.id.tablet_transaction_summary_graph);
        ArrayList<BarViewModel> l = new ArrayList<BarViewModel>();
        Random r = new Random();
        for(int i = 0; i < 30; i++){
            l.add(new BarViewModel("",r.nextInt(100)+1,100));
        }
        BaseBarChartAdapter adapter = new BasicBarChartAdapter(l);
        v.setAdapter(adapter);
        v.setMax(100);
        v.setLabel(false);
        return mRoot;
    }
    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_activity_accounts);
    }
    @Override
    public String getTitleText() {
        // TODO Auto-generated method stub
        return "Recent Transactions";
    }
}
