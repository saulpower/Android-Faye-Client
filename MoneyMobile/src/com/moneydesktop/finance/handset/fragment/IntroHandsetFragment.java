
package com.moneydesktop.finance.handset.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;

public class IntroHandsetFragment extends BaseFragment {
    private final int mIntroImageResourceId;
    private final int mIntroTitle;
    private final int mIntroDescription;

    public IntroHandsetFragment(int introImageResourceId, int mIntroTitle, int mIntroDescription) {
        this.mIntroImageResourceId = introImageResourceId;
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
        View view = inflater.inflate(R.layout.handset_intro_fragment, container, false);
        ImageView introImage = (ImageView) view.findViewById(R.id.introImage);
        introImage.setImageDrawable(getResources().getDrawable(mIntroImageResourceId));
        TextView introTextTitle = (TextView) view.findViewById(R.id.handset_intro_text_title);
        String Title = getResources().getString(mIntroTitle);
        introTextTitle.setText(Title);
        Fonts.applyPrimaryBoldFont(introTextTitle,
                UiUtils.convertPixelsToDp(32, this.getActivity()));
        String Description = getResources().getString(mIntroDescription);
        TextView introTextDescription = (TextView) view
                .findViewById(R.id.handset_intro_text_description);
        introTextDescription.setText(Description);
        Fonts.applyPrimaryFont(introTextDescription,
                UiUtils.convertPixelsToDp(24, this.getActivity()));
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
