package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;

public class WheelView extends View {

	public static final String TAG = "WheelView";
    
	private Bitmap cover;
	private Paint paint, stroke, text;
	private float width, height;
	private Rect boundsYes, boundsNo;
	private String yes, no;
	
	public WheelView(Context context) {
		super(context);
		
		load(context);
	}

	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		load(context);
	}
	
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		load(context);
	}
	
	private void load(Context context) {

		Resources res = context.getResources();
		
		yes = res.getString(R.string.label_yes).toUpperCase();
		no = res.getString(R.string.label_no).toUpperCase();
		
		// Load the panel to base our circle off of its measurements
		this.cover = BitmapFactory.decodeResource(res, R.drawable.phone_switch_paper);
		
		width = cover.getHeight() * .8f;
		height = cover.getHeight() * .8f;
		
		// Setup the various paints we will need
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(res.getColor(R.color.primaryColor));
		paint.setStyle(Paint.Style.FILL);
	    paint.setAntiAlias(true);
	    
		stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		stroke.setStrokeWidth(6);
		stroke.setColor(Color.WHITE);
		stroke.setStyle(Paint.Style.STROKE);
		stroke.setAntiAlias(true);
		
		// Scale the font size for the display resolution
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics());
		
		// Setup the text paint
		text = new Paint(Paint.ANTI_ALIAS_FLAG);
		text.setColor(Color.WHITE);
		text.setAntiAlias(true);
		text.setTextSize(px);
		text.setTypeface(Fonts.getFont(Fonts.PRIMARY_BOLD));
		
		// Get the bounds of the yes/no text to help with positioning
		boundsYes = new Rect();
		text.getTextBounds(yes, 0, yes.length(), boundsYes);
		boundsNo = new Rect();
		text.getTextBounds(no, 0, no.length(), boundsNo);
	}
	
	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		// Draw the circle and give it a stroke
		c.drawCircle(getWidth()/2.0f, getHeight()/2.0f, height/2.0f, paint);
		c.drawCircle(getWidth()/2.0f, getHeight()/2.0f, height/2.0f, stroke);
		
		// Draw the yes and position it appropriately
		c.drawText(yes, getWidth()/4.0f - boundsYes.width()/2.0f, getHeight()/2.0f + boundsYes.height()/2.0f, text);
		
		// Rotate the canvas to write no
		float py = getHeight()/2.0f;
        float px = getWidth()/2.0f;
        c.rotate(180, px, py);
        
        // Draw the no and position it appropriately
		c.drawText(no, getWidth()/4.0f - boundsNo.width()/2.0f, getHeight()/2.0f + boundsNo.height()/2.0f, text);
        
        c.restore(); 
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension((int) (width * 1.08), (int) (height * 1.08));
    }

}
