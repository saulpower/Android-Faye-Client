/**
 * Copyright (c) 2012 Ephraim Tekle genzeb@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN 
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *  @author Ephraim A. Tekle
 *
 */
package com.moneydesktop.finance.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;

/**  
 * This class extends Animation to support a 3D flip view transition animation. Two instances of this class is 
 * required: one for the "from" view and another for the "to" view. 
 * 
 * NOTE: use {@link AnimationFactory} to use this class.
 * 
 *  @author Ephraim A. Tekle
 *  @author Saul Howard
 *
 */
public class FlipAnimation extends Animation { 
	
	public final String TAG = this.getClass().getSimpleName();
	
	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterY;
	private float mRotateX;
	private int width;
	private Camera mCamera;
	private boolean out;
	private FlipDirection direction;

	/**
	 * Constructs a new {@code FlipAnimation} object.Two {@code FlipAnimation} objects are needed for a complete transition b/n two views. 
	 * 
	 * @param fromDegrees the start angle in degrees for a rotation along the y-axis, i.e. in-and-out of the screen, i.e. 3D flip. This should really be multiple of 90 degrees.
	 * @param toDegrees the end angle in degrees for a rotation along the y-axis, i.e. in-and-out of the screen, i.e. 3D flip. This should really be multiple of 90 degrees.
	 * @param out whether the animation is moving the view out or in.
	 * @param centerY the y-axis value of the center of rotation.
	 * @param direction the direction the animation is moving, i.e. left-to-right or right-to-left.
	 * @param width the width of the view being rotated.
	 */
	public FlipAnimation(float fromDegrees, float toDegrees, boolean out,float centerY, FlipDirection direction, int width) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mRotateX = ((direction == FlipDirection.LEFT_RIGHT && !out) || (direction == FlipDirection.RIGHT_LEFT && out)) ? width : 0;
		mCenterY = centerY;
		this.width = width;
		this.direction = direction;
		this.out = out;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

		final float centerY = mCenterY;
		final Camera camera = mCamera;

		final Matrix matrix = t.getMatrix();

		// This is where we determine the amount to translate by
		int dirAmt = (direction == FlipDirection.LEFT_RIGHT) ? 1 : -1;
		int amt = (direction == FlipDirection.RIGHT_LEFT) ? width : 0;
		int start = (int) (out ? amt : (width / 2));
		float centerX = (width / 2 * interpolatedTime * dirAmt) + start;

		camera.save();
		
		camera.rotateY(degrees);
		camera.getMatrix(matrix);
		
		camera.restore();
		
		// Adjusted the matrix translation to rotate off-center of the x-axis
		matrix.preTranslate(-mRotateX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}
}
