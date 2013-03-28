package com.moneydesktop.finance.tablet.fragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.util.UiUtils;
import com.moneydesktop.finance.views.SlidingDrawerRightSide;
import de.greenrobot.event.EventBus;

public class AccountSummaryTabletFragment extends SummaryTabletFragment {

	public final String TAG = this.getClass().getSimpleName();
	private TextView mTitle, mAssetsLabel, mLiabilitiesLabel, mAssetsValue, mLiabilitiesValue, mNetWorthLabel, mNetWorthValue;
	private List<AccountType> mBaseAccountTypes;
	private double mAssets, mLiabilities, mNetWorth;
    private LinearLayout mListContainer;
	
	protected QueryProperty mAccountTypeWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	protected QueryProperty mAccountTypeWhereID = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	protected QueryProperty mOrderByType = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.FinancialAccountType);
	protected QueryProperty mOrderByName = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);

    private ImageView mBankImage;
    private TextView mPropertyLogo;
    private TextView mLastUpdatedLabel;
    private TextView mLastUpdatedValue;
    private TextView mBankLinked;
    private TextView mAccountName;
    private TextView mAccountSum;

	public static AccountSummaryTabletFragment newInstance(int position) {

		AccountSummaryTabletFragment frag = new AccountSummaryTabletFragment();

		Bundle args = new Bundle();
		args.putInt("position", position);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public FragmentType getType() {
		return FragmentType.ACCOUNT_SUMMARY;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBus.getDefault().register(this);
        mActivity.onFragmentAttached(this);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mRoot = inflater.inflate(R.layout.tablet_account_summarry_fragment, null);
		
		setupView();
		
		return mRoot;
	}

	private void setupView() {		
		mTitle = (TextView)mRoot.findViewById(R.id.title);
		mAssetsLabel = (TextView)mRoot.findViewById(R.id.assets_label);
		mAssetsValue = (TextView)mRoot.findViewById(R.id.assets_value);
		mLiabilitiesLabel = (TextView)mRoot.findViewById(R.id.liabilities_label);
		mLiabilitiesValue = (TextView)mRoot.findViewById(R.id.liabilities_value);
        mListContainer = (LinearLayout)mRoot.findViewById(R.id.accounts_summary_expandable_list_view);
		
		mNetWorthLabel = (TextView)mRoot.findViewById(R.id.networth_label);
		mNetWorthValue = (TextView)mRoot.findViewById(R.id.networth_value);

        setupFonts();
		getBaseAccountTypes();		
		getAssetsAndLiabilitiesValue();
        populateAccountTypeRows();
    }

    private void populateAccountTypeRows() {
        for (AccountType accountType : mBaseAccountTypes) {

            final LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.account_summary_group, null);

            final TextView accountTypeName = (TextView)view.findViewById(R.id.tablet_account_summary_type_name);
            TextView accountTypeSum = (TextView)view.findViewById(R.id.tablet_account_summary_account_sum);
            TextView accountTypeCount = (TextView)view.findViewById(R.id.tablet_account_summary_account_count);
            TextView icon = (TextView)view.findViewById(R.id.tablet_account_summary_type_icon);
            LinearLayout bankAccountContainer = (LinearLayout)view.findViewById(R.id.tablet_account_summary_bank_accounts_container);
            final SlidingDrawerRightSide draggableView = (SlidingDrawerRightSide) view.findViewById(R.id.slider);
            LinearLayout sliderContent = (LinearLayout) view.findViewById(R.id.tablet_account_summary_drag_view_container);
            RelativeLayout handle = (RelativeLayout) view.findViewById(R.id.handle);
            LinearLayout container = (LinearLayout) view.findViewById(R.id.tablet_account_summary_drag_view_container);
            LinearLayout iconContainer = (LinearLayout)view.findViewById(R.id.tablet_account_summary_type_group_container);

            setupBankAccounts(accountType.getBankAccounts(), bankAccountContainer);

            setAccountTypeRowBackgroundColor(accountType, sliderContent, handle, container, iconContainer);

            accountTypeName.setText(accountType.getAccountTypeName());


            //SET ACCOUNT TYPE SUM
            double accountTypeBalance = 0;
            for (BankAccount bankAccount : accountType.getBankAccounts()) {
                accountTypeBalance = accountTypeBalance + bankAccount.getBalance();
            }

            String formattedSum = NumberFormat.getCurrencyInstance().format(accountTypeBalance);
            accountTypeSum.setText(formattedSum);


            //SET BANK ACCOUNT COUNT
            int bankCount = accountType.getBankAccounts().size();
            if (bankCount > 1 || bankCount == 0) {
                accountTypeCount.setText(bankCount + " " + mActivity.getString(R.string.account_types_title));
            } else {
                accountTypeCount.setText(bankCount + " " + mActivity.getString(R.string.account));
            }

            draggableView.open();

            //This allows you to grab the panel and close it by touching and dragging on any part of the panel instead of just the handle
            sliderContent.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        draggableView.animateToggle();
                    }
                    return true;
                }
            });

            //SET ACCOUNT TYPE ICON
            setIconText(accountType, icon);

            Fonts.applyPrimaryBoldFont(accountTypeSum, 16);
            Fonts.applySecondaryItalicFont(accountTypeCount, 14);
            Fonts.applyPrimaryBoldFont(accountTypeName, 16);
            Fonts.applyGlyphFont(icon, 20);

            mRoot.post(new Runnable() {
                @Override
                public void run() {
                    setupDrawer(draggableView);
                }
            });

            mListContainer.addView(view);
        }
    }

    private void setAccountTypeRowBackgroundColor(AccountType accountType, LinearLayout sliderContent, RelativeLayout handle, LinearLayout container, LinearLayout iconContainer) {
        if (accountType.getFinancialAccountType() == 1) {
            iconContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.secondaryColor));
            container.setBackgroundColor(mActivity.getResources().getColor(R.color.secondaryColor));
            sliderContent.setBackgroundColor(mActivity.getResources().getColor(R.color.secondaryColor));
            handle.setBackgroundColor(mActivity.getResources().getColor(R.color.secondaryColor));
        } else {
            iconContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.primaryColor));
            container.setBackgroundColor(mActivity.getResources().getColor(R.color.primaryColor));
            sliderContent.setBackgroundColor(mActivity.getResources().getColor(R.color.primaryColor));
            handle.setBackgroundColor(mActivity.getResources().getColor(R.color.primaryColor));
        }
    }

    private void setupBankAccounts(List<BankAccount> bankAccounts, LinearLayout bankAccountContainer) {

        for (final BankAccount bankAccount : bankAccounts) {

            LayoutInflater layoutInflater = mActivity.getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.tablet_account_summary_bank_account_item, null);

            mBankImage = (ImageView)view.findViewById(R.id.tablet_account_summary_bank_account_logo);
            mPropertyLogo = (TextView)view.findViewById(R.id.tablet_account_summary_property_logo);
            mLastUpdatedLabel = (TextView)view.findViewById(R.id.tablet_account_summary_last_updated_label);
            mLastUpdatedValue = (TextView)view.findViewById(R.id.tablet_account_summary_last_updated_value);
            mBankLinked = (TextView)view.findViewById(R.id.tablet_account_summary_linked);
            mAccountName = (TextView)view.findViewById(R.id.tablet_account_summary_type_name);
            mAccountSum = (TextView)view.findViewById(R.id.tablet_account_summary_account_sum);

            populateBankAccountContainer(bankAccount);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mActivity, DropDownTabletActivity.class);
                    i.putExtra(Constant.EXTRA_FRAGMENT, FragmentType.BANK_ACCOUNT_BALANCE_HISTORY);
                    i.putExtra(Constant.EXTRA_ACCOUNT_ID, bankAccount.getAccountId());
                    mActivity.startActivity(i);
                }
            });

            bankAccountContainer.addView(view);
        }
    }

    private void setupFonts() {
        Fonts.applySecondaryItalicFont(mTitle, 14);
        Fonts.applySecondaryItalicFont(mAssetsLabel, 12);
        Fonts.applySecondaryItalicFont(mAssetsValue, 12);
        Fonts.applySecondaryItalicFont(mLiabilitiesLabel, 12);
        Fonts.applySecondaryItalicFont(mLiabilitiesValue, 12);
        Fonts.applySecondaryItalicFont(mNetWorthLabel, 12);
        Fonts.applySecondaryItalicFont(mNetWorthValue, 12);
    }

    private void getAssetsAndLiabilitiesValue() {
		for (AccountType accountType : mBaseAccountTypes) {
			if (accountType.getFinancialAccountType() == 1) {
				for (BankAccount account : accountType.getBankAccounts())  {
					mLiabilities = mLiabilities + account.getBalance();					
				}
			} else {
				for (BankAccount account : accountType.getBankAccounts())  {
					mAssets = mAssets + account.getBalance();					
				}
			}
		}	
		
		mNetWorth = (mAssets - mLiabilities);

        mAssetsValue.setText(NumberFormat.getCurrencyInstance().format(mAssets));
        mLiabilitiesValue.setText(NumberFormat.getCurrencyInstance().format(mLiabilities));
        mNetWorthValue.setText(NumberFormat.getCurrencyInstance().format(mNetWorth));
	}

	private void getBaseAccountTypes() {
		mBaseAccountTypes = new ArrayList<AccountType>();
		ArrayList<QueryProperty> orderBy = new ArrayList<QueryProperty>();
		orderBy.add(mOrderByType);
		orderBy.add(mOrderByName);
		
		ApplicationContext.getDaoSession().clear();
		AccountTypeDao accountTypeDAO = ApplicationContext.getDaoSession().getAccountTypeDao();
		
		//Get the accounts for the account summary dashboard in their proper order by list 
		PowerQuery query = new PowerQuery(accountTypeDAO);	
		query.where(mAccountTypeWhere, "Unknown").and()
		.where(mAccountTypeWhereID, "0")
		.orderBy(orderBy);

        mBaseAccountTypes = accountTypeDAO.queryRaw(query.toString(), query.getSelectionArgs());

	}

    @Override
	public String getTitleText() {
		return getString(R.string.title_fragment_account_summary);
	}

    public void onEvent(final EventMessage.DatabaseSaveEvent event) {
        if ((event.getChangedClassesList().contains(Bank.class) || event.getChangedClassesList().contains(BankAccount.class))
                && (event.didDatabaseChange())) {
            Handler updateSummary = new Handler(Looper.getMainLooper());
            updateSummary.post(new Runnable() {
                public void run()
                {
                  //  mListAdapter.setGroupExpandPosition(RESET);
                  //  mListAdapter.notifyDataSetChanged();
                  //  getAssetsAndLiabilitiesValue();
                }
            });
        }
    }

    private void setIconText(AccountType accountType, TextView icon) {

        Enums.AccountTypesEnum type = Enums.AccountTypesEnum.fromString(accountType.getAccountTypeName().toUpperCase());

        switch (type) {
            case CASH:
                icon.setText(getString(R.string.icon_cash));
                break;

            case CHECKING:
                icon.setText(getString(R.string.icon_checking));
                break;

            case INVESTMENTS:
                icon.setText(getString(R.string.icon_inv));
                break;

            case PROPERTY:
                icon.setText(getString(R.string.icon_prop));
                break;

            case SAVINGS:
                icon.setText(getString(R.string.icon_saving));
                break;

            case CREDIT_CARD:
                icon.setText(getString(R.string.icon_cc));
                break;

            case LINE_OF_CREDIT:
                icon.setText(getString(R.string.icon_loc));
                break;

            case LOANS:
                icon.setText(getString(R.string.icon_loans));
                break;

            case MORTGAGE:
                icon.setText(getString(R.string.icon_mort));
                break;

            default:
                break;
        }

    }

    public void setupDrawer(SlidingDrawerRightSide draggableView) {
        final ViewGroup.LayoutParams drawerLayoutParams = draggableView.getLayoutParams();

        //this is here so we can adjust for the handle on the panel...without it, sizing is a little off.
        drawerLayoutParams.width = mRoot.getWidth() + (int)UiUtils.convertDpToPixel(30, mActivity);
        drawerLayoutParams.height = (int) UiUtils.convertDpToPixel(90, mActivity);
        draggableView.setLayoutParams(drawerLayoutParams);
    }

    private void populateBankAccountContainer(BankAccount bankAccount) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)UiUtils.getScaledPixels(mActivity, 50), (int)UiUtils.getScaledPixels(mActivity, 50));
        mBankImage.setLayoutParams(layoutParams);
        mAccountName.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        mAccountName.setText(bankAccount.getAccountName());
        mAccountSum.setText(NumberFormat.getCurrencyInstance().format(bankAccount.getBalance()));

        if (!bankAccount.getAccountType().getAccountTypeName().toLowerCase().equals(Constant.KEY_PROPERTY)) {
            mPropertyLogo.setVisibility(View.GONE);
            setBankLogo(mBankImage, bankAccount);
        } else {
            setPropertyLogo(mPropertyLogo, bankAccount);
        }

        mBankLinked.setText(setupLastupdatedInfo(mLastUpdatedLabel, mLastUpdatedValue, bankAccount));

        Fonts.applyPrimaryBoldFont(mAccountName, 12);
        Fonts.applyPrimaryFont(mAccountSum, 12);
        Fonts.applySecondaryItalicFont(mBankLinked, 10);
        Fonts.applySecondaryItalicFont(mLastUpdatedLabel, 10);
        Fonts.applySecondaryItalicFont(mLastUpdatedValue, 10);
    }

    private TextView addBankSymbolToContainer() {

        LayoutInflater layoutInflater = mActivity.getLayoutInflater();
        final View addBankView = layoutInflater.inflate(R.layout.handset_account_types_add_bank_item, null);
        TextView addBank = (TextView)addBankView.findViewById(R.id.handset_account_types_add_bank_icon);

        Fonts.applyGlyphFont(addBank, 35);

        int tenDIP = (int) UiUtils.convertDpToPixel(10, mActivity);

        addBank.setPadding(tenDIP, tenDIP*(int)(1.5), 0, tenDIP);

        return addBank;
    }

    private void setBankLogo(final ImageView bankLogo, final BankAccount bankAccount) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!bankAccount.getBank().getBankName().toLowerCase().equals("manual institution")) {
                    BankLogoManager.getBankImage(bankLogo, bankAccount.getInstitutionId());
                }
            }
        });
    }

    private void setPropertyLogo(TextView propertyLogo, BankAccount bankAccount) {

        propertyLogo.setVisibility(View.VISIBLE);
        if (bankAccount.getSubAccountType() == null) {
            propertyLogo.setText(mActivity.getString(R.string.icon_prop));
        } else {
            Enums.PropertyTypesEnum type = Enums.PropertyTypesEnum.fromString(bankAccount.getSubAccountType().getAccountTypeName().toUpperCase());
            switch (type) {
                case REAL_ESTATE:
                    propertyLogo.setText(mActivity.getString(R.string.icon_real_estate));
                    break;

                case VEHICLE:
                    propertyLogo.setText(mActivity.getString(R.string.icon_vehicle));
                    break;

                case ART:
                    propertyLogo.setText(mActivity.getString(R.string.icon_art));
                    break;

                case JEWELRY:
                    propertyLogo.setText(mActivity.getString(R.string.icon_jewelry));
                    break;

                case FURNITURE:
                    propertyLogo.setText(mActivity.getString(R.string.icon_furniture));
                    break;

                case APPLIANCES:
                    propertyLogo.setText(mActivity.getString(R.string.icon_appliances));
                    break;

                case COMPUTER:
                    propertyLogo.setText(mActivity.getString(R.string.icon_computer));
                    break;

                case ELECTRONICS:
                    propertyLogo.setText(mActivity.getString(R.string.icon_electronics));
                    break;

                case SPORTS_EQUIPMENT:
                    propertyLogo.setText(mActivity.getString(R.string.icon_sports_equipment));
                    break;

                case MISCELLANEOUS:
                    propertyLogo.setText(mActivity.getString(R.string.icon_miscellaneous));
                    break;

                default:
                    break;
            }
        }
        Fonts.applyGlyphFont(propertyLogo, 30);
    }

    private String setupLastupdatedInfo(TextView lastUpdatedLabel, TextView lastUpdatedValue, BankAccount bankAccount) {
        String linkedTxt = "";
        if (!bankAccount.getBank().getBankName().toLowerCase().equals("manual institution")) {
            linkedTxt = mActivity.getString(R.string.label_linked);
            lastUpdatedLabel.setText(mActivity.getString(R.string.label_last_updated));

            if (bankAccount.getBank().getLastRefreshDate() != null) {
                setRefreshDate(bankAccount, lastUpdatedValue);

            } else {
                lastUpdatedValue.setText(mActivity.getString(R.string.label_never_refreshed));
            }

        } else {
            linkedTxt = mActivity.getString(R.string.label_manual);
        }
        return linkedTxt;
    }

    private void setRefreshDate(BankAccount bankAccount, TextView lastUpdatedValue) {
        Date date = bankAccount.getBank().getLastRefreshDate();

        long todayMiliseconds = new Date().getTime();
        long timestampMiliseconds = date.getTime();

        long timeDifferenceInMiliseconds = (todayMiliseconds - timestampMiliseconds);

        TimeUnit days = TimeUnit.DAYS;
        TimeUnit hours = TimeUnit.HOURS;
        TimeUnit minutes = TimeUnit.MINUTES;
        TimeUnit seconds = TimeUnit.SECONDS;

        long numOfDays = days.convert(timeDifferenceInMiliseconds, TimeUnit.MILLISECONDS);
        long numOfHours = hours.convert(timeDifferenceInMiliseconds, TimeUnit.MILLISECONDS);
        long numOfMinutes = minutes.convert(timeDifferenceInMiliseconds, TimeUnit.MILLISECONDS);
        long numOfSeconds = seconds.convert(timeDifferenceInMiliseconds, TimeUnit.MILLISECONDS);

        if (numOfHours > 23) {
            String labelAddendum = " " + ((numOfMinutes > 1) ? mActivity.getString(R.string.label_days_ago) : mActivity.getString(R.string.label_day_ago));
            lastUpdatedValue.setText(String.valueOf(numOfDays) + labelAddendum);

        } else if (numOfMinutes > 60) {
            String labelAddendum = " " + ((numOfMinutes > 1) ? mActivity.getString(R.string.label_hours_ago) : mActivity.getString(R.string.label_hour_ago));
            lastUpdatedValue.setText(String.valueOf(numOfHours) + labelAddendum);

        } else if (numOfSeconds > 60) {
            String labelAddendum = " " + ((numOfMinutes > 1) ? mActivity.getString(R.string.label_minutes_ago) : mActivity.getString(R.string.label_minutes_ago));
            lastUpdatedValue.setText(String.valueOf(numOfMinutes) + labelAddendum);

        } else {
            lastUpdatedValue.setText(mActivity.getString(R.string.label_less_than_a_minute_ago));
        }
    }

}