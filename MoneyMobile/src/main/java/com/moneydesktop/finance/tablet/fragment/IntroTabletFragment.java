
package main.java.com.moneydesktop.finance.tablet.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.shared.fragment.BaseFragment;
import main.java.com.moneydesktop.finance.util.Fonts;

public class IntroTabletFragment extends BaseFragment {
    private final int mIntroImageResourceId;

    public IntroTabletFragment(int introImageResourceId) {
        this.mIntroImageResourceId = introImageResourceId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tablet_intro_fragment, container, false);
        ImageView introImage = (ImageView) view.findViewById(R.id.introImage);
        introImage.setImageDrawable(getResources().getDrawable(mIntroImageResourceId));
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
