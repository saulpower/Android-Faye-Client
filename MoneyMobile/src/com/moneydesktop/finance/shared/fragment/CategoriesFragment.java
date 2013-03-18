package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.*;
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
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Category;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.shared.adapter.CategoryAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.ClearEditText;
import com.moneydesktop.finance.views.SpinnerView;
import com.moneydesktop.finance.views.UltimateListView;
import de.greenrobot.event.EventBus;

import java.util.List;

public class CategoriesFragment extends PopupFragment implements OnChildClickListener, OnGroupClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private UltimateListView mCategoryList;
    private CategoryAdapter mAdapter;
    private ClearEditText mSearch;
    private SpinnerView mSpinner;
    
    private Animation mFadeIn, mFadeOut;
    
    private Transactions mTransaction;
    
    public static CategoriesFragment newInstance(long transactionId) {

        CategoriesFragment fragment = new CategoriesFragment();
        
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_ID, transactionId);
        fragment.setArguments(args);
        
        return fragment;
    }

	@Override
	public FragmentType getType() {
		return null;
	}
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.category_view, null);
        
        long id = getArguments().getLong(Constant.KEY_ID);
        mTransaction = (Transactions) DataController.getDao(Transactions.class).load(id);
        
        loadAnimations();
        setupView();
        
        return mRoot;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (mPopupActivity == null) {
    		
    		mRoot.postDelayed(new Runnable() {
				
				@Override
				public void run() {
		    		setupCategoryList();
				}
			}, 450);
    	}
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
    @Override
    public void popupVisible() {

        setupCategoryList();
    }
    
    public void onEvent(DatabaseSaveEvent event) {
        
    	if (mAdapter != null && event.didDatabaseChange() && event.getChangedClassesList().contains(Category.class)) {
    		setupCategoryList();
    	}
    }
    
    private void loadAnimations() {
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_fast);
        mFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_fast);
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
                
                if (hasFocus && mPopupActivity != null) {
                    mPopupActivity.setEditText(null);
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
                    UiUtils.hideKeyboard(getActivity(), v);
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
                
                if (mAdapter == null) {
	                mAdapter = new CategoryAdapter(getActivity(), CategoriesFragment.this, mCategoryList, data, mSearch);
	                mCategoryList.setAdapter(mAdapter);
                } else {
                	mAdapter.updateData(data);
                }
                
                mCategoryList.expandAll();
                
                if (mCategoryList.getVisibility() != View.VISIBLE) {
	                mCategoryList.setVisibility(View.VISIBLE);
	                mCategoryList.startAnimation(mFadeIn);
	                mSpinner.startAnimation(mFadeOut);
                }
            }

        }.execute();
    }
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.filter_cats).toUpperCase();
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
    
    public void dismissPopup(Category category) {
    	
    	UiUtils.hideKeyboard(getActivity(), mSearch);
    	
        mTransaction.setCategoryId(category.getId());
        mTransaction.updateSingle();
        
        dismissPopup();
    }
}
