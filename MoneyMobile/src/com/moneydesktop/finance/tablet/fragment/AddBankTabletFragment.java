package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataBridge;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.data.SyncEngine;
import com.moneydesktop.finance.database.*;
import com.moneydesktop.finance.model.EventMessage;
import com.moneydesktop.finance.model.EventMessage.GetLogonCredentialsFinished;
import com.moneydesktop.finance.model.EventMessage.SaveInstitutionFinished;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.adapter.SelectAccountTypesAdapter;
import com.moneydesktop.finance.shared.adapter.SelectPropertyTypesAdapter;
import com.moneydesktop.finance.shared.fragment.BaseFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.tablet.adapter.AddNewInstitutionAdapter;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.navigation.AnimatedNavView;
import com.moneydesktop.finance.views.navigation.AnimatedNavView.NavigationListener;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddBankTabletFragment extends BaseFragment implements NavigationListener {

    public final String TAG = this.getClass().getSimpleName();

	private NumberFormat mFormatter = NumberFormat.getCurrencyInstance();
    private AnimatedNavView mNavView;
	private LinearLayout mAutomaticContainer;
	private LinearLayout mManualContainer;
	private static ViewFlipper mFlipper;
	private RelativeLayout mSelectImportMethodScreen;
	private RelativeLayout mSelectInstitutionScreen;
	private RelativeLayout mConnectScreen;
	private List<Institution> mInstitutions;
	private AddNewInstitutionAdapter mAdapter;
	private ListView mAddInstitutionList;
	private Institution mSelectedInstitution;
	
	private CheckBox mPersonal, mBusiness;
	private EditText mEdit1, mEdit2, mEdit3;
	private Button mSaveInstitution;
	private JSONObject objectToSendToAddInstitution;
	private TextView mCurrentBalance, mAccountName;
	
	private List<String> mLoginLabels;
	private HashMap<String, String> mCredentialsHash = new HashMap<String, String>();

    private boolean mForNewTransaction = false;
	
	private QueryProperty mWherePopularity = new QueryProperty(InstitutionDao.TABLENAME, InstitutionDao.Properties.Popularity, "!= ?");
	private QueryProperty mAccountTypeWhereID = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	private QueryProperty mAccountTypeWhereName = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "= ?");
	private QueryProperty mAccountTypeNotWhere = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName, "!= ?");
	private QueryProperty mAccountTypeAnd = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.ParentAccountTypeId, "= ?");
	private QueryProperty mOrderBy = new QueryProperty(AccountTypeDao.TABLENAME, AccountTypeDao.Properties.AccountTypeName);
	
	private QueryProperty mWhereBankId = new QueryProperty(BankDao.TABLENAME, BankDao.Properties.BankId, "= ?");
	private int viewPostion = 0;

    public void setForNewTransaction(boolean forNewTransaction) {
        mForNewTransaction = forNewTransaction;
    }
	
	@Override
	public String getFragmentTitle() {

        int title = mForNewTransaction ? R.string.add_account_type_to_add : R.string.add_account_label;

		return mActivity.getString(title);
	}

	@Override
	public boolean onBackPressed() {
		if (viewPostion == 1 || viewPostion == 0) {
			((DropDownTabletActivity)mActivity).dismissDropdown();
		} else {
			((DropDownTabletActivity)mActivity).getAnimatedNavView().popNav();
		}
		return true;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
	}

	public static AddBankTabletFragment newInstance() {

        AddBankTabletFragment mFragment = new AddBankTabletFragment();
			
        Bundle args = new Bundle();
        mFragment.setArguments(args);
        
        return mFragment;
	}

    public static AddBankTabletFragment newInstance(boolean isManual) {

        AddBankTabletFragment mFragment = new AddBankTabletFragment();
        mFragment.setForNewTransaction(isManual);

        Bundle args = new Bundle();
        mFragment.setArguments(args);

        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mNavView = ((DropDownTabletActivity)mActivity).getAnimatedNavView();
        mNavView.setNavigationListener(this);

        mRoot = inflater.inflate(R.layout.tablet_add_bank, null);

        mFlipper = (ViewFlipper)mRoot.findViewById(R.id.add_bank_flipper);
        mSelectImportMethodScreen = (RelativeLayout)mRoot.findViewById(R.id.view1);
        mSelectInstitutionScreen = (RelativeLayout)mRoot.findViewById(R.id.view2);
        mConnectScreen = (RelativeLayout)mRoot.findViewById(R.id.view3);
        
        setupImportMethodScreen();
       
        setupOnClickListeners();  

        return mRoot;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mForNewTransaction) {
            showManualAccounts();
        }
    }

	private void setupImportMethodScreen() {

		 TextView automaticDownloadTitle = (TextView) mRoot.findViewById(R.id.add_transactions_automatically_title);
		 TextView automaticDownloadDesc = (TextView) mRoot.findViewById(R.id.add_transactions_automatically_description);
		 TextView manualDownloadTitle = (TextView) mRoot.findViewById(R.id.add_transactions_manually_title);
		 TextView manualDownloadDesc = (TextView) mRoot.findViewById(R.id.add_transactions_manually_description);
        
		 mAutomaticContainer = (LinearLayout) mSelectImportMethodScreen.findViewById(R.id.add_bank_automatically_container);
		 mManualContainer = (LinearLayout) mSelectImportMethodScreen.findViewById(R.id.add_bank_manually_container);
        
		 automaticDownloadTitle.setText(getActivity().getString(R.string.add_account_automatically_title));
		 automaticDownloadDesc.setText(getActivity().getString(R.string.add_account_automatically_desc));
		 manualDownloadTitle.setText(getActivity().getString(R.string.add_account_manually_title));
		 manualDownloadDesc.setText(getActivity().getString(R.string.add_account_manually_desc));
		 
		 viewPostion = 1;
		 
         mSaveInstitution = (Button)mConnectScreen.findViewById(R.id.add_account_save_button);
         TextView title = (TextView)mConnectScreen.findViewById(R.id.add_account_title);
         TextView checkBoxHeader = (TextView)mConnectScreen.findViewById(R.id.add_account_connect_checkbox_header);
         
     	 Fonts.applyPrimaryBoldFont(mSaveInstitution, 14);
     	 Fonts.applySecondaryItalicFont(title, 12);
     	 Fonts.applySecondaryItalicFont(checkBoxHeader, 12);
		 Fonts.applyPrimaryBoldFont(automaticDownloadTitle, 14);
		 Fonts.applyPrimaryBoldFont(automaticDownloadDesc, 10);
		 Fonts.applyPrimaryBoldFont(manualDownloadTitle, 14);
		 Fonts.applyPrimaryBoldFont(manualDownloadDesc, 10);
		
	}
	
	private void setupInstitutionScreen() {
		mAddInstitutionList = (ListView) mSelectInstitutionScreen.findViewById(R.id.add_bank_institution_list);
        EditText filter = (EditText) mSelectInstitutionScreen.findViewById(R.id.add_bank_filter_institution);      
		final InstitutionDao dao = ApplicationContext.getDaoSession().getInstitutionDao();
		
		PowerQuery powerQuery = new PowerQuery(dao);
		powerQuery.where(mWherePopularity, "0");
		mInstitutions = dao.queryRaw(powerQuery.toString(), powerQuery.getSelectionArgs());
        
		mAddInstitutionList.setVisibility(View.VISIBLE);
		mAdapter = new AddNewInstitutionAdapter(mActivity, R.layout.tablet_add_bank_institution_list_item, mInstitutions, filter, mAddInstitutionList);
		mAddInstitutionList.setAdapter(mAdapter);
		mAdapter.initializeData();
		
		mAddInstitutionList.setOnItemClickListener(new OnItemClickListener() 
		{
		    @Override
		    public void onItemClick(AdapterView<?> a, View v,int position, long id) 
		    {
				((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(String.format(getString(R.string.add_account_institution_connect), ((Institution)a.getItemAtPosition(position)).getName()));
				viewPostion = 3;
				
				
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				mSelectedInstitution = (Institution)a.getItemAtPosition(position);
				mFlipper.showNext();
				
				setupConnectScreen();
		    }
		});
		
	}
	
	private void setupAccountTypeListScreen () {
		ListView accountTypesList = (ListView)mRoot.findViewById(R.id.tablet_add_bank_manually_account_tyles_list);

		//get List of account types
		AccountTypeDao accountTypeDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		
	    List<AccountType> allAccountTypes = new ArrayList<AccountType>();
	    		
	    //Get all account types in alphabetical order. Removing the "Unknown" Type. 
		PowerQuery query = new PowerQuery(accountTypeDao);	
		query.where(mAccountTypeNotWhere, "Unknown")
		.and().where(mAccountTypeAnd, "0")
		.orderBy(mOrderBy, false);
		
		allAccountTypes = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
		
		accountTypesList.setAdapter(new SelectAccountTypesAdapter(mActivity, R.layout.select_account_types_item, allAccountTypes)); 
		
		accountTypesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				AccountType selectedAccountType= (AccountType)listView.getItemAtPosition(position);
				
				if (!selectedAccountType.getAccountTypeName().toUpperCase().equals("PROPERTY")) {
					((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(String.format(getString(R.string.add_account_new_account), selectedAccountType.getAccountTypeName().toUpperCase()));
					
					viewPostion = 5;
					setupSaveBankManuallyScreen(selectedAccountType);
					Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
					Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
					mFlipper.setInAnimation(in);
					mFlipper.setOutAnimation(out);
					mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view5)));
				} else {
					((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(getString(R.string.add_account_type_of_property));
					
					viewPostion = 6;
					setupProptertyTypeListScreen();
					mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view6)));
				}
			}
		});
	}
		
	private void setupProptertyTypeListScreen () {
		ListView propertyTypesList = (ListView)mRoot.findViewById(R.id.tablet_add_bank_manually_property_type);

		//get List of account types
		AccountTypeDao accountTypeDao = ApplicationContext.getDaoSession().getAccountTypeDao();
		
	    List<AccountType> allPropertyTypes = new ArrayList<AccountType>();
	    List<AccountType> propertyType = new ArrayList<AccountType>();
	    
	    PowerQuery propertyQuery = new PowerQuery(accountTypeDao);
	    propertyQuery.where(mAccountTypeWhereName, "Property");
	    
	    propertyType = accountTypeDao.queryRaw(propertyQuery.toString(), propertyQuery.getSelectionArgs());
	    		
		PowerQuery query = new PowerQuery(accountTypeDao);	
		query.where(mAccountTypeWhereID, propertyType.get(0).getId().toString())
		.orderBy(mOrderBy, false);
		
		allPropertyTypes = accountTypeDao.queryRaw(query.toString(), query.getSelectionArgs());
		
		propertyTypesList.setAdapter(new SelectPropertyTypesAdapter(mActivity, R.layout.select_property_types_item, allPropertyTypes)); 
		
		propertyTypesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				AccountType selectedAccountType= (AccountType)listView.getItemAtPosition(position);

				((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(String.format(getString(R.string.add_account_new_account), selectedAccountType.getAccountTypeName().toUpperCase()));
				
				setupSaveBankManuallyScreen(selectedAccountType);
				mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view5)));
			}
		});
	}

	private void setupSaveBankManuallyScreen(final AccountType selectedAccountType) {

		TextView accountNameLabel = (TextView)mRoot.findViewById(R.id.tablet_add_bank_manually_account_name_title_txt);
		TextView currentBalanceLabel = (TextView)mRoot.findViewById(R.id.tablet_add_bank_manually_current_balance_title_txt);
		mAccountName = (EditText)mRoot.findViewById(R.id.tablet_add_bank_manually_account_name);
		mCurrentBalance = (EditText)mRoot.findViewById(R.id.tablet_add_bank_manually_current_balance_edittext);
		TextView save = (TextView)mRoot.findViewById(R.id.tablet_add_bank_manually_save);

		Fonts.applyPrimaryBoldFont(accountNameLabel, 14);
		Fonts.applyPrimaryBoldFont(currentBalanceLabel, 14);
		Fonts.applyPrimaryBoldFont(save, 14);

		mCurrentBalance.setText(mFormatter.format(0));

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

                createManualBankAccount(selectedAccountType);
			}
		});
	}

    private void createManualBankAccount(AccountType selectedAccountType) {

        double balance = Double.parseDouble(mCurrentBalance.getText().toString());
        String name = mAccountName.getText().toString();

        BankAccount bankAccount = BankAccount.createBankAccount(selectedAccountType, balance, name);
        bankAccount.insertSingle();

        SyncEngine.sharedInstance().syncBankAccount(bankAccount, mForNewTransaction);

        ((DropDownTabletActivity) mActivity).dismissDropdown();
    }
	
	private void setupConnectScreen() {
		mLoginLabels = new ArrayList<String>();
		
		
		//Gets the Logon Fields needed to populate view for the specific institution
		new Thread(new Runnable() {			
			public void run() {				
				JSONArray array = DataBridge.sharedInstance().getInstituteLoginFields(mSelectedInstitution.getInstitutionId());
				for (int i = 0; i< array.length(); i++) {
					try {
						String object = ((JSONObject)array.get(i)).getString("label");
						mLoginLabels.add(object);
						mCredentialsHash.put(object, ((JSONObject)array.get(i)).getString("guid"));
					} catch (JSONException e) {
						//TODO: update this log to something useful
						e.printStackTrace();
					}	
				}	
				
				//Notify that request is finished and we are now ready to start populating the view.
				EventBus.getDefault().post(new EventMessage().new GetLogonCredentialsFinished(true, mLoginLabels));
			}
		}).start();
		
	}

    public void onEvent(final GetLogonCredentialsFinished event) {

        if (event.isAddedForFistTime()) {
        	
	    	Handler updateFields = new Handler(Looper.getMainLooper());
	    	updateFields.post(new Runnable() {
	    	    public void run()
	    	    {
	    	    	
	    	    	List<String> logonLabels = event.getLogonLabels();
	            	TextView label1 = (TextView)mConnectScreen.findViewById(R.id.add_account_connect_option1_title_txt);
	            	TextView label2 = (TextView)mConnectScreen.findViewById(R.id.add_account_connect_option2_title_txt);
	            	TextView label3 = (TextView)mConnectScreen.findViewById(R.id.add_account_connect_option3_title_txt);
	            	
	            	mEdit1 = (EditText)mConnectScreen.findViewById(R.id.add_account_connect_option1_edittxt);
	            	mEdit2 = (EditText)mConnectScreen.findViewById(R.id.add_account_connect_option2_edittxt);
	            	mEdit3 = (EditText)mConnectScreen.findViewById(R.id.add_account_connect_option3_edittxt);
	            	
	            	mPersonal = (CheckBox)mConnectScreen.findViewById(R.id.add_account_connect_checkbox_personal);
	            	mBusiness = (CheckBox)mConnectScreen.findViewById(R.id.add_account_connect_checkbox_business);
	            
	            	mBusiness.setVisibility(View.VISIBLE);
	            	mPersonal.setVisibility(View.VISIBLE);
	            	

	            	TextView checkBoxTextPersonal = (TextView) mConnectScreen.findViewById(R.id.add_account_connect_checkbox_txt1);
	            	TextView checkBoxTextBusiness = (TextView) mConnectScreen.findViewById(R.id.add_account_connect_checkbox_txt2);
	            	TextView checkboxTitleTxt = (TextView)mConnectScreen.findViewById(R.id.add_account_connect_checkbox_header);

	            	checkBoxTextPersonal.setVisibility(View.VISIBLE);
	            	checkBoxTextBusiness.setVisibility(View.VISIBLE);
	            	checkboxTitleTxt.setVisibility(View.VISIBLE);
	            	
	            	
	            	onClickListeners();
	            	
	            	offsetKeyboardOnConnectScreen();
	            	
	    	    	switch (logonLabels.size()) {
	    			case 1:
	    				//Don't think this will ever happen....
	    				label1.setVisibility(View.VISIBLE);
	    				label2.setVisibility(View.GONE);
	    				label3.setVisibility(View.GONE);
	    				
	    				mEdit1.setVisibility(View.VISIBLE);
	    				mEdit2.setVisibility(View.GONE);
	    				mEdit3.setVisibility(View.GONE);
	    				break;
	    			case 2:
	    				//Most common...just UserName/password
	    				label1.setVisibility(View.VISIBLE);
	    				label2.setVisibility(View.VISIBLE);
	    				label3.setVisibility(View.GONE);
	    				
	    				mEdit1.setVisibility(View.VISIBLE);
	    				mEdit2.setVisibility(View.VISIBLE);
	    				mEdit3.setVisibility(View.GONE);
	    				break;
	    			case 3:
	    				//Banks like USSA will hit this. UserName/Password/Pin
	    				label1.setVisibility(View.VISIBLE);
	    				label2.setVisibility(View.VISIBLE);
	    				label3.setVisibility(View.VISIBLE);
	    				
	    				mEdit1.setVisibility(View.VISIBLE);
	    				mEdit2.setVisibility(View.VISIBLE);
	    				mEdit3.setVisibility(View.VISIBLE);
	    				break;
	    			default:
	    				break;
	    			}
	            	
	            	for (int i = 0; i < logonLabels.size(); i++) {        		
	            		switch (i) {
	    				case 0:
	    					label1.setText(logonLabels.get(i));
	    					break;
	    				case 1:
	    					label2.setText(logonLabels.get(i));
	    					break;
	    				case 2:
	    					label3.setText(logonLabels.get(i));
	    					break;					
	    				default:
	    					break;
	    				}
	            	
	            	}
	    	    }
	    	});
	    	
	    	
	    	mSaveInstitution.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
                v.setClickable(false);
                objectToSendToAddInstitution = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                try {
                    for (int i = 0; i < event.getLogonLabels().size(); i++) {
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("guid", mCredentialsHash.get(event.getLogonLabels().get(i).toString()));

                        switch (i) {
                        case 0:
                            jsonObject.put("value", mEdit1.getText().toString());
                            break;
                        case 1:
                            jsonObject.put("value", mEdit2.getText().toString());
                            break;
                        case 2:
                            jsonObject.put("value", mEdit3.getText().toString());
                            break;
                        default:
                            break;
                        }

                        jsonArray.put(jsonObject);
                    }

                    objectToSendToAddInstitution.put(Constant.KEY_CREDENTIALS, jsonArray);
                    objectToSendToAddInstitution.put("institution_guid", mSelectedInstitution.getInstitutionId());
                    objectToSendToAddInstitution.put("user_guid", User.getCurrentUser().getUserId());
                } catch (JSONException e) {
                    //TODO: update this log to something useful
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    public void run() {
                        JSONObject jsonResponse = DataBridge.sharedInstance().saveFinancialInstitute(objectToSendToAddInstitution);

                        //Notify that request is finished and we are now ready to start populating the view.
                        EventBus.getDefault().post(new EventMessage().new SaveInstitutionFinished(jsonResponse));
                 }
                }).start();
					
				}
			});
       	
        }
    }
        
    public void onEvent(final SaveInstitutionFinished event) {   
    	
    	Handler updateFields = new Handler(Looper.getMainLooper());
    	updateFields.post(new Runnable() {
    	    public void run()
    	    {
		    	((DropDownTabletActivity)mActivity).dismissDropdown();
		    }
    	});
    	
    	JSONObject json = event.getJsonResponse();
    	if (json != null) {
    		
    		JSONObject memberObject = json.optJSONObject(Constant.KEY_MEMBER);
    		
    		//save to database
    		Bank.saveIncomingBank(memberObject, false);
    		DataController.save();
    		
    		BankDao bankDAO = ApplicationContext.getDaoSession().getBankDao();
    		
    		PowerQuery query = new PowerQuery(bankDAO);
    	    query.where(mWhereBankId, memberObject.optString(Constant.KEY_GUID));
    	    	    
    	    final List<Bank> bankList = bankDAO.queryRaw(query.toString(), query.getSelectionArgs());
    		  		
    	    Handler sync = new Handler(Looper.getMainLooper());
    	    sync.post(new Runnable() {
        	    public void run()
        	    {
        	    	SyncEngine.sharedInstance().beginBankStatusUpdate(bankList.get(0));
        	    }
        	});
			
    	}
    }
    
	@Override
	public void onPause() {		
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	private void onClickListeners() {
    	//personal is selected by default.
    	mPersonal.setChecked(true);
		
		mPersonal.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mBusiness.setChecked(false);
				}
				
				//prevents both checkboxes from being unselected
				if (!isChecked && !mBusiness.isChecked()) {
					mPersonal.setChecked(true);
				}
			}
		});
    	
    	mBusiness.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mPersonal.setChecked(false);
				}
				
				//prevents both checkboxes from being unselected
				if (!isChecked && !mPersonal.isChecked()) {
					mBusiness.setChecked(true);
				}
			}
		});
    	
	}
    
	private void offsetKeyboardOnConnectScreen() {
		mEdit1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mEdit1);
		        }	
			}
		});
    	
    	mEdit2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mEdit2);
		        }	
			}
		});
    	
    	mEdit3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
		        if (mActivity instanceof DropDownTabletActivity && hasFocus) {
		            ((DropDownTabletActivity) mActivity).setEditText(mEdit3);
		        }	
			}
		});
	}
	
	private void setupOnClickListeners() {
				
		mAutomaticContainer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(getString(R.string.add_account_institution));
				
				viewPostion = 2;
				setupInstitutionScreen();
				
				Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
				Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
				mFlipper.setInAnimation(in);
				mFlipper.setOutAnimation(out);
				mFlipper.showNext();
			}
		});
		
		mManualContainer.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {

                ((DropDownTabletActivity)mActivity).getAnimatedNavView().pushNav(getString(R.string.add_account_type_to_add));

				showManualAccounts();
			}
		});
	}

    private void showManualAccounts() {

        viewPostion = 4;
        setupAccountTypeListScreen();

        Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
        Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
        mFlipper.setInAnimation(in);
        mFlipper.setOutAnimation(out);
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view4)));
    }

	@Override
	public FragmentType getType() {
		return null;
	}

	
	@Override
	public void onNavigationPopped() {

        if (mForNewTransaction && mNavView.getStackSize() == 1) {
            getFragmentManager().popBackStack();
            return;
        }

		animateBackToPrevious();
	}

	private void animateBackToPrevious() {
		Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_left);
		Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_right);
		mFlipper.setInAnimation(in);
		mFlipper.setOutAnimation(out);
		
		if (viewPostion <= 3) {
			viewPostion--;
			mFlipper.showPrevious();
		} else if (viewPostion == 4) {
			mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view1)));
			viewPostion = 1;
		} else if (viewPostion == 5 ) {
			viewPostion--;
			mFlipper.showPrevious();
		} else if (viewPostion == 6) {
			viewPostion = 4;
			mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.view4)));	
		}
	}
}