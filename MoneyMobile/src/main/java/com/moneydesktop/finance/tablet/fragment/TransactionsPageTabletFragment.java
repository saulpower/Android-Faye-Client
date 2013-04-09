package main.java.com.moneydesktop.finance.tablet.fragment;


import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.data.Enums.TxFilter;
import main.java.com.moneydesktop.finance.database.CategoryDao;
import main.java.com.moneydesktop.finance.database.QueryProperty;
import main.java.com.moneydesktop.finance.database.Transactions;
import main.java.com.moneydesktop.finance.database.TransactionsDao;
import main.java.com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import main.java.com.moneydesktop.finance.shared.TransactionViewHolder;
import main.java.com.moneydesktop.finance.shared.fragment.TransactionsFragment;
import main.java.com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import main.java.com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import main.java.com.moneydesktop.finance.util.DateRange;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.PerformanceUtils;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TargetApi(11)
public class TransactionsPageTabletFragment extends TransactionsFragment implements OnItemClickListener, OnItemLongClickListener {

    public final String TAG = this.getClass().getSimpleName();

    private DateRangeView mDateRange;
    private HorizontalScroller mScroller;
    private ParentTransactionInterface mParent;

    private boolean mFixLine = false;
    private LineView mLine;

    private int[] mLocation = new int[2];

    private HeaderView mDate, mPayee, mCategory, mAmount;
    private LinearLayout mButtons, mHeaders;
    private ImageView mSum, mEmail, mAdd;

    private boolean mShowButtons = true;
    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");

    private Animation mFadeIn, mFadeOut;

    private DateRange mRange;

    private boolean mAdding = false;

    public void setParent(ParentTransactionInterface mParent) {
        this.mParent = mParent;
    }

    public void setLineFix() {
        mFixLine = true;
    }

    public void setShowButtons(boolean showButtons) {
        mShowButtons = showButtons;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.TRANSACTIONS_PAGE;
    }

    public void setDateRange(long start, long end) {
        this.mRange = new DateRange(start, end);
    }

    @SuppressWarnings("unchecked")
    public static TransactionsPageTabletFragment newInstance(ParentTransactionInterface parent, Intent intent) {

        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
        fragment.setParent(parent);
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setCategories((ArrayList<Long>) intent.getSerializableExtra(Constant.EXTRA_CATEGORY_ID));
        fragment.setCategoryType(intent.getIntExtra(Constant.EXTRA_CATEGORY_TYPE, -1));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
        fragment.setShowButtons(false);

        if (intent.hasExtra(Constant.EXTRA_START_DATE) && intent.hasExtra(Constant.EXTRA_END_DATE)) {
            long start = intent.getLongExtra(Constant.EXTRA_START_DATE, 0);
            long end = intent.getLongExtra(Constant.EXTRA_END_DATE, 0);
            fragment.setDateRange(start, end);
        }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_transaction_page_view, null);

        setupAnimations();

        mRoot.post(new Runnable() {

            @Override
            public void run() {
                mRoot.getLocationOnScreen(mLocation);
            }
        });

        return mRoot;
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdding = false;
    }

    @Override
    protected void setupView() {
        super.setupView();

        mTransactionsList.setOnItemClickListener(this);
        mTransactionsList.setOnItemLongClickListener(this);

        mScroller = (HorizontalScroller) mRoot.findViewById(R.id.date_scroller);

        mDateRange = (DateRangeView) mRoot.findViewById(R.id.date_range);
        mDateRange.setScroller(mScroller);
        mDateRange.setDateRangeChangeListener(this);

        if (mRange != null) {
            mDateRange.setDateRange(mRange);
        }

        mDate = (HeaderView) mRoot.findViewById(R.id.date_header);
        mDate.setOnFilterChangeListener(this);
        mPayee = (HeaderView) mRoot.findViewById(R.id.payee_header);
        mPayee.setOnFilterChangeListener(this);
        mCategory = (HeaderView) mRoot.findViewById(R.id.category_header);
        mCategory.setOnFilterChangeListener(this);
        mAmount = (HeaderView) mRoot.findViewById(R.id.amount_header);
        mAmount.setOnFilterChangeListener(this);

        mHeaders = (LinearLayout) mRoot.findViewById(R.id.list_headers);
        mButtons = (LinearLayout) mRoot.findViewById(R.id.button_container);

        if (!mShowButtons) {
            mButtons.setVisibility(View.INVISIBLE);
        }

        mSum = (ImageView) mRoot.findViewById(R.id.sum);
        mEmail = (ImageView) mRoot.findViewById(R.id.email);
        mAdd = (ImageView) mRoot.findViewById(R.id.add);

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

        mSum.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                displaySum();
            }
        });

        mEmail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                emailTransactions(mHeaders);
            }
        });

        mAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mAdding) return;

                mAdding = true;

                Intent i = new Intent(mActivity, DropDownTabletActivity.class);
                i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.MANUAL_BANK_LIST);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        });

        mDate.performClick();
    }

    private void applyFonts() {

        Fonts.applyPrimaryBoldFont(mDate, 12);
        Fonts.applyPrimaryBoldFont(mPayee, 12);
        Fonts.applyPrimaryBoldFont(mCategory, 12);
        Fonts.applyPrimaryBoldFont(mAmount, 12);
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
                mTransactionsList.setSelection(0);
                mAdapter.applyNewData();
                mTransactionsList.setVisibility(View.VISIBLE);
                mTransactionsList.startAnimation(mFadeIn);
            }
        });
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

        PerformanceUtils.start("details");

        Transactions transaction = (Transactions) parent.getItemAtPosition(position);

        if (transaction != null && mParent != null) {

            mParent.showTransactionDetails(view, mLocation[1], transaction);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        TransactionViewHolder holder = (TransactionViewHolder) view.getTag();
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);

        if (holder == null || transaction == null) return false;

        int[] catLocation = new int[2];
        holder.category.getLocationOnScreen(catLocation);

        int adjustedX = catLocation[0] + holder.category.getWidth();
        int adjustedY = catLocation[1] + (view.getHeight() / 2);

        Intent intent = new Intent(getActivity(), PopupTabletActivity.class);
        intent.putExtra(Constant.EXTRA_POSITION_X, adjustedX);
        intent.putExtra(Constant.EXTRA_POSITION_Y, adjustedY);
        intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.CATEGORIES);
        intent.putExtra(Constant.EXTRA_ID, transaction.getId());

        startActivity(intent);

        return true;
    }

    @Override
    public void isShowing() {

        mWaiting = false;
        configureView();
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

        } else if (mLoaded && !mWaiting) {

            mTransactionsList.setSelection(0);
            mAdapter.refreshCurrentSelection();
        }
    }

    @Override
    public void dataLoaded(boolean invalidate) {

        mLoaded = true;

        if (!invalidate) {
            mAdapter.applyNewData();
            return;
        }

        if (mTransactionsList.getVisibility() == View.VISIBLE) {
            mTransactionsList.startAnimation(mFadeOut);
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

    private void displaySum() {

        Intent i = new Intent(mActivity, DropDownTabletActivity.class);
        i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.TRANSACTION_SUMMARY);
        i.putExtra(Constant.EXTRA_VALUES, calculateStats());
        startActivity(i);
    }

    private String[] calculateStats() {

        List<Transactions> transactions = mAdapter.getTransactions();

        String[] values = new String[3];
        double[] amounts = new double[2];

        for (Transactions t : transactions) {

            amounts[0] += t.getAmount();
            amounts[1] += t.getRawAmount();
        }

        values[0] = Integer.toString(transactions.size());
        values[1] = mFormatter.format(Math.abs(amounts[0] / transactions.size()));
        values[2] = mFormatter.format(Math.abs(amounts[1]));

        return values;
    }

    @Override
    protected Date getStartDate() {
        return mDateRange.getStartDate();
    }

    @Override
    protected Date getEndDate() {
        return mDateRange.getEndDate();
    }

    @Override
    protected Object getChildInstance() {
        return this;
    }

    public void addHelpView(HelpView helpView) {

        int distance = 40;
        int width = 300;

        View cell1 = mTransactionsList.getChildAt(0);

        if (cell1 == null) return;

        helpView.addHelp(cell1, new Point(cell1.getWidth() / 5, cell1.getHeight() * 1), HelpView.Direction.UP, HelpView.TextSide.LEFT, distance, R.string.help_transactions_1, width);
        helpView.addHelp(cell1, new Point(-cell1.getWidth() / 4, cell1.getHeight() * 3), HelpView.Direction.DOWN, distance, R.string.help_transactions_2, width);
        helpView.addHelp(mScroller, new Point(-mScroller.getWidth() / 4, 0), HelpView.Direction.UP, distance, R.string.help_transactions_4, width);
    }
}
