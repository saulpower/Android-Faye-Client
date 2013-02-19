
package com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.util.Fonts;

public class IntroTabletFragment extends BaseFragment {
    private final int mIntroImageResourceId;
    private final int mIntroTitle;
    private final int mIntroDescription;

    public IntroTabletFragment(int introImageResourceId, int mIntroTitle, int mIntroDescription) {
        this.mIntroImageResourceId = introImageResourceId;
        this.mIntroTitle = mIntroTitle;
        this.mIntroDescription = mIntroDescription;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.tablet_intro_fragment, container, false);
        ImageView introImage = (ImageView) view.findViewById(R.id.introImage);
        introImage.setImageDrawable(getResources().getDrawable(mIntroImageResourceId));
        TextView introTextTitle = (TextView) view.findViewById(R.id.intro_text_title);
        String Title = getResources().getString(mIntroTitle);
        introTextTitle.setText(Title);
        Fonts.applyPrimaryBoldFont(introTextTitle, 32);
        String Description = getResources().getString(mIntroDescription);
        TextView introTextDescription = (TextView) view.findViewById(R.id.intro_text_description);
        introTextDescription.setText(Description);
        Fonts.applyPrimaryFont(introTextDescription, 24);
        return view;
    }

	@Override
	public FragmentType getType() {
		return null;
	}

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
