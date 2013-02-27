package com.moneydesktop.finance.views;

public class FrictionDynamics extends Dynamics {

    /** The friction factor */
    private float mFrictionFactor;

    /**
     * Creates a SimpleDynamics object
     * 
     * @param frictionFactor The friction factor. Should be between 0 and 1.
     *            A higher number means a slower dissipating speed.
     * @param snapToFactor The snap to factor. Should be between 0 and 1. A
     *            higher number means a stronger snap.
     */
    public FrictionDynamics(final float frictionFactor) {
        mFrictionFactor = frictionFactor;
    }

    @Override
    protected void onUpdate(final int dt) {

        // then update the position based on the current velocity
        mPosition += mVelocity * dt / 1000;

        // and finally, apply some friction to slow it down
        mVelocity *= mFrictionFactor;
    }
}
