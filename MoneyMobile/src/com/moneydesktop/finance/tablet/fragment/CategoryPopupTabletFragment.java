package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.tablet.adapter.CategoryTabletAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.ClearEditText;
import com.moneydesktop.finance.views.SpinnerView;
import com.moneydesktop.finance.views.UltimateListView;

import java.util.List;

public class CategoryPopupTabletFragment extends PopupFragment implements OnChildClickListener, OnGroupClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private UltimateListView mCategoryList;
    private CategoryTabletAdapter mAdapter;
    private ClearEditText mSearch;
    private SpinnerView mSpinner;
    
    private Animation mFadeIn, mFadeOut;
    
    public static CategoryPopupTabletFragment newInstance() {

        CategoryPopupTabletFragment fragment = new CategoryPopupTabletFragment();
        
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_category_popup_view, null);
        
        loadAnimations();
        setupView();
        
        return mRoot;
    }
    
    @Override
    public void popupVisible() {

        setupCategoryList();
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
            }
        });
    }
    
    private void setupView() {

        mCategoryList = (UltimateListView) mRoot.findViewById(R.id.categories);
        mCategoryList.setDividerHeight(0);
        mCategoryList.setDivider(null);
        mCategoryList.setChildDivider(null);
        mCategoryList.setOnChildClickListener(this);
        mCategoryList.setOnGroupClickListener(this);
        
        mSearch = (ClearEditText) mRoot.findViewById(R.id.search);
        
        mSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                
                if (hasFocus) {
                    mActivity.setEditText(null);
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
        
        Fonts.applyPrimaryFont(mSearch, 10);
    }
    
    private void setupCategoryList() {
        
        new AsyncTask<Void, Void, List<Pair<Category, List<Category>>>>() {

            @Override
            protected List<Pair<Category, List<Category>>> doInBackground(Void... params) {

                if (isCancelled()) {
                    return null;
                }
                
                List<Pair<Category, List<Category>>> data = Category.loadCategoryData();
                
                return data;
            }

            @Override
            protected void onPostExecute(List<Pair<Category, List<Category>>> data) {
                
                if (isCancelled()) {
                    return;
                }
                
                mAdapter = new CategoryTabletAdapter(mActivity, mCategoryList, data);
                mCategoryList.setAdapter(mAdapter);
                
                mCategoryList.expandAll();
                mCategoryList.setVisibility(View.VISIBLE);
                mCategoryList.startAnimation(mFadeIn);
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

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        
        Category category = (Category) mAdapter.getChild(groupPosition, childPosition);
        dismissPopup(category);
        
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        mRoot.playSoundEffect(SoundEffectConstants.CLICK);
        
        dismissPopup((Category) mAdapter.getGroup(groupPosition));
        
        return true;
    }
    
    private void dismissPopup(Category category) {
        Intent resultIntent = new Intent();
        resultIntent.putExtras(getActivity().getIntent().getExtras());
        resultIntent.putExtra(Constant.EXTRA_CATEGORY_ID, category.getId());
        dismissPopup(Activity.RESULT_OK, resultIntent);
    }
}
