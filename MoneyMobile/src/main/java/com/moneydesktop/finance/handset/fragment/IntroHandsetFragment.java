
package main.java.com.moneydesktop.finance.handset.fragment;

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

public class IntroHandsetFragment extends BaseFragment {
    private final int mIntroImageResourceId;

    public IntroHandsetFragment(int introImageResourceId) {
        this.mIntroImageResourceId = introImageResourceId;
    }

    @Override
    public FragmentType getType() {
        return null;
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
