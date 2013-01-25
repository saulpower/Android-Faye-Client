package com.moneydesktop.finance.tablet.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.views.BarView;

public class TestActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        final BarView b = new BarView(this,"test bar",125,250);
        LinearLayout l = (LinearLayout) findViewById(R.id.test_view);
        l.addView(b);
        LayoutParams layout2 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,4);
        b.setLayoutParams(layout2);
        b.showLabel(true);
        Button button1 = new Button(this);
        l.addView(button1);
        button1.setText("goto 0");
        button1.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
               b.setAmountAnimated(25);
               b.setColorAnimated(Color.GREEN);
            }
        });
        LayoutParams layout1 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
        button1.setLayoutParams(layout1);
        
        Button button2 = new Button(this);
        l.addView(button2);
        button2.setText("goto 250");
        button2.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
               b.setAmountAnimated(250);
               b.setColorAnimated(Color.RED);
            }
        });
        button2.setLayoutParams(layout1);
        
        Button button3 = new Button(this);
        l.addView(button3); 
        button3.setText("goto 125");
        button3.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
               b.setAmountAnimated(125);
               b.setColorAnimated(Color.YELLOW);
            }
        });
        button3.setLayoutParams(layout1);
        
    }
}
