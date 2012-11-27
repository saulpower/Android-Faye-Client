package com.moneydesktop.finance.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.moneydesktop.finance.R;

public class Caret extends ImageView {
	
	private int color = Color.BLUE;
	private float width = 10;
	private float height = 10;
	
	private PointF point1;        
	private PointF point2;    
	private PointF point3;
	private Path path;

	private Paint paint;
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	    paint.setStrokeWidth(1);
	    paint.setColor(color);     
	    paint.setStyle(Paint.Style.FILL_AND_STROKE);
	    paint.setAntiAlias(true);
	}

	public float getCaretWidth() {
		return width;
	}

	public void setCaretWidth(float width) {
		this.width = width;
		createPath();
	}

	public float getCaretHeight() {
		return height;
	}

	public void setCaretHeight(float height) {
		this.height = height;
		createPath();
	}

	public Caret(Context context) {
		super(context);
		
		createPath();
	}
	
	public Caret(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(attrs);
	}
	
	public Caret(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(attrs);
	}
	
	private void createPath() {
		
		float center = height/5;
		
		point1 = new PointF(0, (height/2 - center));  
		point2 = new PointF(width, (height/2 - center));    
		point3 = new PointF((width/2), (height - center));

	    path = new Path();
	    path.setFillType(Path.FillType.EVEN_ODD);
	    
	    path.moveTo(point1.x,point1.y);
	    path.lineTo(point2.x,point2.y);
	    path.lineTo(point3.x,point3.y);
	    path.lineTo(point1.x,point1.y);
	    path.close();
	}

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) getCaretWidth(), (int) getCaretHeight());
    }
	
	private void init(AttributeSet attrs) {
		
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Caret);
		
	    setColor(a.getColor(R.styleable.Caret_color, Color.WHITE));
	    setCaretWidth(a.getDimension(R.styleable.Caret_width, 10.0f));
	    setCaretHeight(a.getDimension(R.styleable.Caret_height, 10.0f));
		    
		a.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	    canvas.drawPath(path, paint);
	}

}
