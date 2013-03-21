package com.moneydesktop.finance.shared.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.BusinessObjectBase;
import com.moneydesktop.finance.database.Tag;
import com.moneydesktop.finance.model.EventMessage.DatabaseSaveEvent;
import com.moneydesktop.finance.shared.adapter.TagsAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.ClearEditText;
import com.moneydesktop.finance.views.SpinnerView;
import de.greenrobot.event.EventBus;

import java.util.List;

public class TagsFragment extends PopupFragment {
    
    public final String TAG = this.getClass().getSimpleName();

    private ListView mTagsList;
    private RelativeLayout mContainer;
    private TagsAdapter mAdapter;
    private ClearEditText mSearch;
    private SpinnerView mSpinner;
    private int mPreviousHeight = 0;
    private boolean mShowing = false;
    
    private Animation mFadeIn, mFadeOut;
    
    public static TagsFragment newInstance(long id) {

        TagsFragment fragment = new TagsFragment();
        
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_ID, id);
        fragment.setArguments(args);
        
        return fragment;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }

	@Override
	public FragmentType getType() {
		return null;
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tag_view, null);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            
            @Override
            public void onGlobalLayout() {
                
                if (mPreviousHeight != mRoot.getHeight() && mShowing && mPopupActivity != null) {
                    mPreviousHeight = mRoot.getHeight();
                    mPopupActivity.setMargin(mPreviousHeight != 0);
                }
             }
        });
        
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
		    		setupTagList();
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

        setupTagList();
    }
    
    public void onEvent(DatabaseSaveEvent event) {
        
    	if (mAdapter != null && event.didDatabaseChange() && event.getChangedClassesList().contains(BusinessObjectBase.class)) {
    		setupTagList();
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
            	mContainer.removeView(mSpinner);
                mShowing = true;
            }
        });
    }
    
    private void setupView() {

    	mContainer = (RelativeLayout) mRoot.findViewById(R.id.container);
    	
        mTagsList = (ListView) mRoot.findViewById(R.id.categories);
        mTagsList.setDividerHeight(0);
        mTagsList.setDivider(null);
        
        mSearch = (ClearEditText) mRoot.findViewById(R.id.search);
        mSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                
                if (hasFocus && mPopupActivity != null) {
                    mPopupActivity.setEditText(mSearch);
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
                if (mPopupActivity != null) mPopupActivity.setEditText(mSearch);
            }
        });
        
        mSearch.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UiUtils.hideKeyboard(mPopupActivity, v);
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
                	
                	long id = getArguments().getLong(Constant.KEY_ID);
                	
                	if (id == 0L) {
                		Log.e(TAG, "No BusinessObject");
                		dismissPopup();
                	}
                	
	                mAdapter = new TagsAdapter(getActivity(), R.layout.tag_view, data, mTagsList, mSearch, id);
	                mTagsList.setAdapter(mAdapter);
	                
	                mTagsList.setVisibility(View.VISIBLE);
	                mTagsList.startAnimation(mFadeIn);
	                mSpinner.startAnimation(mFadeOut);
	                
                } else {
                	
                	mAdapter.updateData(data);
                }
            }

        }.execute();
    }
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.filter_tags).toUpperCase();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
