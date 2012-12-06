package com.moneydesktop.finance.model;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class PointEvaluator implements TypeEvaluator<PointF> {
	
    public PointF evaluate(float fraction, PointF startPoint, PointF endPoint) {
    	
        float x = startPoint.x + fraction * (endPoint.x - startPoint.x);
        float y = startPoint.y + fraction * (endPoint.y - startPoint.y);
        
        return new PointF(x, y);
    }
}
