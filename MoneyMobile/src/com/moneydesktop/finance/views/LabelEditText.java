package com.moneydesktop.finance.views;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class LabelEditText extends EditText {
	protected Paint labelPaint;
	protected String text;
	protected Rect bounds = new Rect();
	public LabelEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
		// TODO Auto-generated constructor stub
	}
	private void init(AttributeSet attrs) {	
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditText);
		makePaint();
		setLabelText(a.getString(R.styleable.LabelEditText_labelText));
	    setTextColor(a.getColor(R.styleable.LabelEditText_labelColor, Color.WHITE));
	    setLabelSize(a.getDimension(R.styleable.LabelEditText_labelSize, 10.0f));
		a.recycle();
	}
	public void setTextColor(int color) {
			if(labelPaint == null){
				makePaint();
		}
		
	    labelPaint.setColor(color);     
	 invalidate();
	}
	public void makePaint(){
		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setStyle(Paint.Style.FILL);
		labelPaint.setTypeface(Fonts.getFont(Fonts.SECONDARY_ITALIC));
		labelPaint.setTextSize(9);
	}
	public void setLabelText(String t){
		text = t;
		if(labelPaint == null){
			makePaint();
	}
		labelPaint.getTextBounds(t, 0, t.length(), bounds);
		invalidate();
	}
	public void setLabelSize(float labelSize){
		if(labelPaint == null){
			makePaint();
	}
	
		labelPaint.setTextSize(labelSize);    
		invalidate();
	}
	@Override public void onDraw (Canvas c) {
		super.onDraw(c);
		c.drawText(text, 10, (bounds.height()+10), labelPaint);
	}

}
