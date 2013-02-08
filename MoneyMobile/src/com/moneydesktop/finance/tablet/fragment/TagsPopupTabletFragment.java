package com.moneydesktop.finance.tablet.fragment;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.tablet.adapter.TagsTabletAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.ClearEditText;
import com.moneydesktop.finance.views.SpinnerView;

import de.greenrobot.event.EventBus;

public class TagsPopupTabletFragment extends PopupFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private ListView mTagsList;
    private TagsTabletAdapter mAdapter;
    private ClearEditText mSearch;
    private SpinnerView mSpinner;
    private int mPreviousHeight = 0;
    private boolean mShowing = false;
    
    private Animation mFadeIn, mFadeOut;
    
    public static TagsPopupTabletFragment newInstance() {

        TagsPopupTabletFragment fragment = new TagsPopupTabletFragment();
        
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_tag_popup_view, null);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            
            @Override
            public void onGlobalLayout() {
                
                if (mPreviousHeight != mRoot.getHeight() && mShowing) {
                    mPreviousHeight = mRoot.getHeight();
                    mActivity.setMargin(mPreviousHeight != 0);
                }
             }
        });
        
        loadAnimations();
        setupView();
        
        return mRoot;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
    @Override
    public void popupVisible() {

        setupTagList();
    }
    
    public void onEvent(DatabaseSaveEvent event) {
        
    	if (mAdapter != null && event.didDatabaseChange() && event.getChangedClassesList().contains(Tag.class)) {
    		setupTagList();
    	}
    }
    
    private void loadAnimations() {
        mFadeIn = AnimationUtils.loadAnimation(mActivity, R.anim.fade_in_fast);
        mFadeOut = AnimationUtils.loadAnimation(mActivity, R.anim.fade_out_fast);
        mFadeOut.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mSpinner.setVisibility(View.GONE);
                mShowing = true;
            }
        });
    }
    
    private void setupView() {

        mTagsList = (ListView) mRoot.findViewById(R.id.categories);
        mTagsList.setDividerHeight(0);
        mTagsList.setDivider(null);
        
        mSearch = (ClearEditText) mRoot.findViewById(R.id.search);
        mSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                
                if (hasFocus) {
                    mActivity.setEditText(mSearch);
                }
            }
        });
        
        mSearch.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                mAdapter.getFilter().filter(mSearch.getText().toString());
                mActivity.setEditText(mSearch);
            }
        });
        
        mSearch.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UiUtils.hideKeyboard(mActivity, v);
                    return true;
                }
                return false;
            }
        });
        
        mSpinner = (SpinnerView) mRoot.findViewById(R.id.spinner);
        
        applyFonts();
    }
    
    private void applyFonts() {
        
        Fonts.applySecondaryItalicFont(mSearch, 10);
    }

    private void setupTagList() {
        
        new AsyncTask<Void, Void, List<Tag>>() {

            @Override
            protected List<Tag> doInBackground(Void... params) {

                if (isCancelled()) {
                    return null;
                }
                
                List<Tag> data = Tag.loadAll();

                return data;
            }

            @Override
            protected void onPostExecute(List<Tag> data) {
                
                if (isCancelled()) {
                    return;
                }
                
                if (mAdapter == null) {
	                mAdapter = new TagsTabletAdapter(mActivity, R.layout.tablet_account_summary, data, mTagsList, mSearch);
	                mTagsList.setAdapter(mAdapter);
                } else {
                	mAdapter.updateData(data);
                }
                
                mTagsList.setVisibility(View.VISIBLE);
                mTagsList.startAnimation(mFadeIn);
                mSpinner.startAnimation(mFadeOut);
            }

        }.execute();
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
