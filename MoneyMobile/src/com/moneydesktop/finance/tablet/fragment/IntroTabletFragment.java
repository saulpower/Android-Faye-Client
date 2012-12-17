package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
 
public class IntroTabletFragment extends BaseFragment {
    private final int introImageResourceId;
    private final int mIntroTitle;
    private final int mIntroDescription;
    public IntroTabletFragment(int introImageResourceId, int mIntroTitle, int mIntroDescription) {
        this.introImageResourceId = introImageResourceId;
        this.mIntroTitle = mIntroTitle;
        this.mIntroDescription = mIntroDescription;
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
 
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
 
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tablet_loading_fragment, container, false);
        ImageView introImage = (ImageView) view.findViewById(R.id.introImage);
        introImage.setImageDrawable(getResources().getDrawable(introImageResourceId));
        TextView introTextTitle = (TextView) view.findViewById(R.id.intro_text_title);
        String mTitle = getResources().getString(mIntroTitle);
        introTextTitle.setText(mTitle);
        Fonts.applyPrimaryBoldFont(introTextTitle, 32);
        String mDescription = getResources().getString(mIntroDescription);
        TextView introTextDescription = (TextView) view.findViewById(R.id.intro_text_description);
        introTextDescription.setText(mDescription);
        Fonts.applyPrimaryFont(introTextDescription, 24);
        return view;
    }

    @Override
    public String getFragmentTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onBackPressed() {
        // TODO Auto-generated method stub
        return false;
    }
}