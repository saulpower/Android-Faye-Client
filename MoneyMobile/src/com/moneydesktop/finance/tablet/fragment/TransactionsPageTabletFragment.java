package com.moneydesktop.finance.tablet.fragment;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
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

import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.Enums.TxFilter;
import com.moneydesktop.finance.database.CategoryDao;
import com.moneydesktop.finance.database.QueryProperty;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.shared.TransactionDetailController.ParentTransactionInterface;
import com.moneydesktop.finance.shared.TransactionViewHolder;
import com.moneydesktop.finance.shared.fragment.TransactionsFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.activity.PopupTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.util.FileIO;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.DateRangeView;
import com.moneydesktop.finance.views.HeaderView;
import com.moneydesktop.finance.views.HorizontalScroller;
import com.moneydesktop.finance.views.LineView;

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
    private ImageView mSum, mEmail, mPrint, mReport, mAdd;
    
    private boolean mShowButtons = true;
    private DecimalFormat mFormatter = new DecimalFormat("$#,##0.00;-$#,##0.00");
    
    private Animation mFadeIn, mFadeOut;
    
    public ParentTransactionInterface getParent() {
        return mParent;
    }

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

    @SuppressWarnings("unchecked")
	public static TransactionsPageTabletFragment newInstance(ParentTransactionInterface parent, Intent intent) {
            
        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
        fragment.setParent(parent);
        fragment.setAccountId(intent.getStringExtra(Constant.EXTRA_ACCOUNT_ID));
        fragment.setCategories((ArrayList<Long>) intent.getSerializableExtra(Constant.EXTRA_CATEGORY_ID));
        fragment.setCategoryType(intent.getIntExtra(Constant.EXTRA_CATEGORY_TYPE, -1));
        fragment.setTxFilter((TxFilter) intent.getSerializableExtra(Constant.EXTRA_TXN_TYPE));
        fragment.setShowButtons(false);
        
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
    protected void setupView() {
    	super.setupView();
    	
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
        
        mHeaders = (LinearLayout) mRoot.findViewById(R.id.list_headers);
        mButtons = (LinearLayout) mRoot.findViewById(R.id.button_container);
        
        if (!mShowButtons) {
            mButtons.setVisibility(View.INVISIBLE);
        }
        
        mSum = (ImageView) mRoot.findViewById(R.id.sum);
        mEmail = (ImageView) mRoot.findViewById(R.id.email);
        mPrint = (ImageView) mRoot.findViewById(R.id.print);
        mReport = (ImageView) mRoot.findViewById(R.id.report);
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
                emailTransactions();
            }
        });
        
        mAdd.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Add");
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
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);
        
        int[] catLocation = new int[2];
        holder.category.getLocationOnScreen(catLocation);
        
        int adjustedX = catLocation[0] + holder.category.getWidth();
        int adjustedY = catLocation[1] + (view.getHeight() / 2);
        
        Intent intent = new Intent(getActivity(), PopupTabletActivity.class);
        intent.putExtra(Constant.EXTRA_POSITION_X, adjustedX);
        intent.putExtra(Constant.EXTRA_POSITION_Y, adjustedY);
        intent.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.POPUP_CATEGORIES);
        intent.putExtra(Constant.EXTRA_ID, transaction.getId());
        
        startActivity(intent);
        
        return true;
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
    
    private void emailTransactions() {
        
        DialogUtils.showProgress(getActivity(), getString(R.string.generate_email));

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                Bitmap image = getWholeListViewItemsToBitmap();
                String path = FileIO.saveBitmap(getActivity(), image, getString(R.string.transactions_list));

                return path;
            }

            @Override
            protected void onPostExecute(String path) {

                DialogUtils.hideProgress();
                EmailUtils.sendEmail(getActivity(), getString(R.string.email_transactions_subject), "", path);
            }

        }.execute();
    }
    
    private  Bitmap getWholeListViewItemsToBitmap() {

        final float divider = UiUtils.getDynamicPixels(getActivity(), 1);
        int allitemsheight = mHeaders.getHeight();
        Bitmap header = UiUtils.convertViewToBitmap(mHeaders);
        
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        bitmaps.add(header);

        for (int i = 0; i < mAdapter.getCount(); i++) {

            View childView = mAdapter.getView(i, null, mTransactionsList);
            childView.measure(
                    MeasureSpec.makeMeasureSpec(mTransactionsList.getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
            childView.setDrawingCacheEnabled(true);
            childView.buildDrawingCache();
            
            bitmaps.add(childView.getDrawingCache());
            allitemsheight += childView.getMeasuredHeight();
        }

        Bitmap listBitmap = Bitmap.createBitmap(mTransactionsList.getMeasuredWidth(), (int) (allitemsheight + mAdapter.getCount() * divider), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(listBitmap);
        canvas.drawColor(getResources().getColor(R.color.gray1));
        
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int currentTop = 0;

        for (int i = 0; i < bitmaps.size(); i++) {
            
            Bitmap bmp = bitmaps.get(i);
            canvas.drawBitmap(bmp, 0, currentTop, paint);
            currentTop += (bmp.getHeight() + divider);

            bmp.recycle();
            bmp = null;
        }

        return listBitmap;
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
}
