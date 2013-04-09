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
package main.java.com.moneydesktop.finance.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import main.java.com.moneydesktop.finance.animation.AnimationFactory.FlipDirection;

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
public class FlipXAnimation extends Animation {

    public final String TAG = this.getClass().getSimpleName();

    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mAxisX, mAxisY;
    private Camera mCamera;

    /**
     * Constructs a new {@code FlipAnimation} object.Two {@code FlipAnimation} objects are needed for a complete transition b/n two views.
     *
     * @param direction the direction the animation is moving, i.e. top-to-bottom or bottom-to-top.
     * @param xCenter the x-axis of the view being rotated.
     * @param yCenter the y-axis of the view being rotated.
     */
    public FlipXAnimation(FlipDirection direction, int xCenter, int yCenter) {
        mFromDegrees = direction.getStartDegreeForFirstView();
        mToDegrees = direction.getEndDegreeForFirstView();
        mAxisX = xCenter;
        mAxisY = yCenter;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        final float fromDegrees = mFromDegrees;
        float change = (mToDegrees - fromDegrees);
        float degrees = fromDegrees + (change * interpolatedTime);

        final Camera camera = mCamera;

        final Matrix matrix = t.getMatrix();

        camera.save();

        camera.rotateX(degrees);
        camera.getMatrix(matrix);

        camera.restore();

        // Adjusted the matrix translation to rotate off-center of the x-axis
        matrix.preTranslate(-mAxisX, -mAxisY);
        matrix.postTranslate(mAxisX, mAxisY);
    }
}
