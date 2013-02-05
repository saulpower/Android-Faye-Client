package com.moneydesktop.finance.tablet.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.PowerQuery;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage.DataUpdateEvent;
import com.moneydesktop.finance.model.EventMessage.FilterEvent;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter.OnDataLoadedListener;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.AmazingListView;
import com.moneydesktop.finance.views.DateRangeView;
import com.moneydesktop.finance.views.DateRangeView.FilterChangeListener;
import com.moneydesktop.finance.views.HeaderView;
import com.moneydesktop.finance.views.HorizontalScroller;
import com.moneydesktop.finance.views.LineView;

import de.greenrobot.event.EventBus;

@TargetApi(11)
public class TransactionsPageTabletFragment extends BaseFragment implements OnItemClickListener, FilterChangeListener, OnDataLoadedListener, OnItemLongClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private AmazingListView mTransactionsList;
    private TransactionsTabletAdapter mAdapter;
    private DateRangeView mDateRange;
    private HorizontalScroller mScroller;
    private ParentTransactionInterface mParent;
    
    private boolean mFixLine = false;
    private LineView mLine;

    private int[] mLocation = new int[2];

    private String mAccountId;
    private TxFilter mTxFilter;
    private QueryProperty mAccountIdProp = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.BankAccountId);
    private QueryProperty mIsProcessed = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.IsProcessed);
    private QueryProperty mOrderBy = new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date);
    private boolean mDirection = true;
    private String mSearchTitle = "%";
    private PowerQuery mQueries;
    
    private HeaderView mDate, mPayee, mCategory, mAmount;
    private EditText mSearch;
    
    private boolean mLoaded = false;
    private boolean mWaiting = true;
    
    private Animation mFadeIn, mFadeOut;
    
    private AsyncTask<Integer, Void, TransactionsTabletAdapter> mBackgroundTask;
    
    private TextWatcher mWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            mSearchTitle = "%" + mSearch.getText().toString().toLowerCase() + "%";
            setupTransactionsList();
        }
    };
    
    public ParentTransactionInterface getParent() {
        return mParent;
    }

    public void setParent(ParentTransactionInterface mParent) {
        this.mParent = mParent;
    }
    
    public void setAccountId(String mAccountId) {
        this.mAccountId = mAccountId;
    }

    public void setTxFilter(TxFilter mTxFilter) {
        this.mTxFilter = mTxFilter;
    }
    
    public void setLineFix() {
        mFixLine = true;
    }

    public static TransactionsPageTabletFragment newInstance(ParentTransactionInterface parent, Intent intent) {
            
        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
        fragment.setParent(parent);
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
        
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }

    public static TransactionsPageTabletFragment newInstance(ParentTransactionInterface parent) {
            
        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
        fragment.setParent(parent);
        fragment.setLineFix();
        
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
        
        mRoot = inflater.inflate(R.layout.tablet_transaction_page_view, null);
        
        setupView();
        setupAnimations();
        
        setupTransactionsList();
        
        mRoot.post(new Runnable() {
            
            @Override
            public void run() {
                mRoot.getLocationOnScreen(mLocation);
            }
        });
        
        return mRoot;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
        
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(false);
        }
    }
    
    private void setupView() {

        mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
        mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
        mTransactionsList.setEmptyView(mActivity.getLayoutInflater().inflate(R.layout.empty_view, null));
        mTransactionsList.setOnItemClickListener(this);
        mTransactionsList.setOnItemLongClickListener(this);
        
        mScroller = (HorizontalScroller) mRoot.findViewById(R.id.date_scroller);
        
        mDateRange = (DateRangeView) mRoot.findViewById(R.id.date_range);
        mDateRange.setScroller(mScroller);
        mDateRange.setDateRangeChangeListener(this);
        
        mDate = (HeaderView) mRoot.findViewById(R.id.date_header);
        mDate.setOnFilterChangeListener(this);
        mPayee = (HeaderView) mRoot.findViewById(R.id.payee_header);
        mPayee.setOnFilterChangeListener(this);
        mCategory = (HeaderView) mRoot.findViewById(R.id.category_header);
        mCategory.setOnFilterChangeListener(this);
        mAmount = (HeaderView) mRoot.findViewById(R.id.amount_header);
        mAmount.setOnFilterChangeListener(this);
        
        mSearch = (EditText) mRoot.findViewById(R.id.search);
        mSearch.addTextChangedListener(mWatcher);
        
        // Bug with line not being lined up correctly
        if (mFixLine) {
            mLine = (LineView) mRoot.findViewById(R.id.line_bug);
            LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.leftMargin = (int) UiUtils.getDynamicPixels(getActivity(), 1);
            mLine.setLayoutParams(params);
            mFixLine = false;
        }
        
        setupListeners();
        applyFonts();
    }
    
    private void setupAnimations() {
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_now);
        mFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out_now);
        mFadeOut.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
                mTransactionsList.setVisibility(View.INVISIBLE);
                mAdapter.applyNewData();
                mTransactionsList.setVisibility(View.VISIBLE);
                mTransactionsList.startAnimation(mFadeIn);
            }
        });
    }
    
    private void setupListeners() {
        
        mDate.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mDate, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Date));
            }
        });
        
        mPayee.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mPayee, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Title));
            }
        });
        
        mCategory.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mCategory, new QueryProperty(CategoryDao.TABLENAME, CategoryDao.Properties.CategoryName));
            }
        });
        
        mAmount.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                applySort(mAmount, new QueryProperty(TransactionsDao.TABLENAME, TransactionsDao.Properties.Amount));
            }
        });
        

        mSearch.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    UiUtils.hideKeyboard(mActivity, v);
                }
                
                return false;
            }
        });
        
        mDate.performClick();
    }
    
    private void applyFonts() {
        
        Fonts.applyPrimaryBoldFont(mDate, 12);
        Fonts.applyPrimaryBoldFont(mPayee, 12);
        Fonts.applyPrimaryBoldFont(mCategory, 12);
        Fonts.applyPrimaryBoldFont(mAmount, 12);
        Fonts.applyPrimaryBoldFont(mSearch, 14);
    }
    
    private void applySort(HeaderView clicked, QueryProperty field) {
        
        mOrderBy = field;
        
        final boolean wasShowing = clicked.isShowing();
        
        mDate.setIsShowing(false);
        mPayee.setIsShowing(false);
        mCategory.setIsShowing(false);
        mAmount.setIsShowing(false);
        
        clicked.setIsShowing(true);
        
        if (wasShowing) {
            clicked.setIsAscending(!clicked.isAscending(), true);
        } else {
            clicked.setIsAscending(false, false);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);
        
        if (transaction != null && mParent != null) {

            mParent.showTransactionDetails(view, mLocation[1], transaction);
            
            if (!transaction.getIsProcessed()) {
                transaction.setIsProcessed(true);
                transaction.updateSingle();
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        
        TransactionViewHolder holder = (TransactionViewHolder) view.getTag();
        
        int[] catLocation = new int[2];
        holder.category.getLocationOnScreen(catLocation);
        
        int adjustedX = catLocation[0] + holder.category.getWidth();
        int adjustedY = catLocation[1] + (view.getHeight() / 2);
        
        Intent intent = new Intent(getActivity(), PopupTabletActivity.class);
        intent.putExtra(Constant.EXTRA_POSITION_X, adjustedX);
        intent.putExtra(Constant.EXTRA_POSITION_Y, adjustedY);
        intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.POPUP_CATEGORIES);
        intent.putExtra(Constant.EXTRA_POSITION, position);
        intent.putExtra(Constant.EXTRA_SOURCE_CODE, Constant.CODE_CATEGORY_LIST);
        startActivityForResult(intent, Constant.CODE_CATEGORY_LIST);
        
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        
        int sourceCode = data.getIntExtra(Constant.EXTRA_SOURCE_CODE, -1);
        
        if (sourceCode == Constant.CODE_CATEGORY_LIST) {
            
            int position = data.getIntExtra(Constant.EXTRA_POSITION, -1);
            long categoryId = data.getLongExtra(Constant.EXTRA_CATEGORY_ID, -1);
            
            if (position != -1 && categoryId != -1) {
                updateTransactionCategory(position, categoryId);
            }
            
            return;
            
        } else if (sourceCode == Constant.CODE_CATEGORY_DETAIL) {
        
            mParent.parentOnActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void updateTransactionCategory(int position, long categoryId) {
        
        Transactions transaction = (Transactions) mTransactionsList.getItemAtPosition(position);
        transaction.setCategoryId(categoryId);
        transaction.updateSingle();
        
        mAdapter.notifyDataSetChanged();
    }
    
    private void setupTransactionsList() {
        
        mLoaded = false;
        
        if (mAdapter == null) {
            mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList);
            mAdapter.setOnDataLoadedListener(this);
            mTransactionsList.setAdapter(mAdapter);
        }

        addAccountFilter();
        
        mAdapter.setDateRange(mDateRange.getStartDate(), mDateRange.getEndDate());
        mAdapter.setOrder(mOrderBy, mDirection);
        mAdapter.setSearch(mSearchTitle);
        mAdapter.setQueries(mQueries);
        mAdapter.initializeData();
    }
    
    @Override
    public void dataLoaded() {
        
        mLoaded = true;

        if (mTransactionsList.getVisibility() == View.VISIBLE) {
            mTransactionsList.startAnimation(mFadeOut);
        }
    }
    
    public void onEvent(ParentAnimationEvent event) {
        
        if (!event.isOutAnimation() && !event.isFinished()) {
            mWaiting = true;
        }
        
        if (event.isOutAnimation() && event.isFinished()) {

            mWaiting = false;
            configureView();
        }
    }
    
    public void onEvent(FilterEvent event) {

        mQueries = event.getQueries();
        filterChanged(-1);
    }
    
    public void onEvent(DataUpdateEvent event) {
        
        mAdapter.notifyDataSetChanged();
    }

    private void configureView() {
        
        if (mLoaded && !mWaiting && mTransactionsList.getVisibility() != View.VISIBLE) {

            mTransactionsList.postDelayed(new Runnable() {
                
                @Override
                public void run() {

                    mAdapter.applyNewData();
                    mTransactionsList.setVisibility(View.VISIBLE);
                    mTransactionsList.startAnimation(mFadeIn);
                }
            }, 100);
        }
    }
    
    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        if (mParent != null && mParent.getDetailFragment() != null) {
            return mParent.getDetailFragment().onBackPressed();
        }
        
        return false;
    }

    @Override
    public void filterChanged(int direction) {

        if (direction != -1) {
            mDirection = direction == 0;
        }
        
        setupTransactionsList();
    }
    
    /**
     * Add any passed in filters when the fragment
     * was constructed.
     */
    private void addAccountFilter() {
        
        if (mAccountId == null && mTxFilter != null) {
            return;
        }
        
        if (mQueries == null) {
            mQueries = new PowerQuery(false);
        }
        
        if (mAccountId != null) {
            mQueries.and().where(mAccountIdProp, mAccountId);
        }
        
        if (mTxFilter != null && mTxFilter == TxFilter.UNCLEARED) {
            mQueries.and().where(mIsProcessed, "0");
        }
    }
}
