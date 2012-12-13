package com.moneydesktop.finance.tablet.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;

@TargetApi(11)
public class TransactionsTabletFragment extends BaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	private static TransactionsTabletFragment sFragment;
	private RelativeLayout mGroup;
	
	public static TransactionsTabletFragment newInstance() {
			
		sFragment = new TransactionsTabletFragment();
	
        Bundle args = new Bundle();
        sFragment.setArguments(args);
        
        return sFragment;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mActivity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();

        this.mActivity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRoot = inflater.inflate(R.layout.tablet_transactions_view, null);
		mGroup = (RelativeLayout) mRoot.findViewById(R.id.root);
		
		TransactionsPageTabletFragment frag = TransactionsPageTabletFragment.newInstance();
      
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, frag);
        ft.commit();
		
		return mRoot;
	}
	
	public void showTransactionDetails(View view, int offset) {
	    
	    int[] location = new int[2];
	    
        view.getLocationOnScreen(location);
        
	    Bitmap b = loadBitmapFromView(view);
	    ImageView image = new ImageView(getActivity());
	    image.setImageBitmap(b);
	    image.setX(location[0] + 50);
	    image.setY((location[1] + offset));
	    view.setVisibility(View.GONE);
	    
	    mGroup.addView(image);
	    
//	    ObjectAnimator animY = ObjectAnimator.ofFloat(image, "y", image.getY(), image.getY() + 100);
//        ObjectAnimator animX = ObjectAnimator.ofFloat(image, "x", image.getX(), image.getX() + 100);
//	    
//        AnimatorSet set = new AnimatorSet();
//        set.play(animX).with(animY);
//        set.setDuration(5000);
//        set.start();
	}
	
	public Bitmap loadBitmapFromView(View v) {
	    
	    Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);                
	    Canvas c = new Canvas(b);
	    v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
	    v.draw(c);
	    
	    return b;
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transactions);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
