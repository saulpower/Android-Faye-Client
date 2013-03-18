package com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.moneydesktop.finance.ApplicationContext;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.Enums.BankRefreshStatus;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.database.BankDao;
import com.moneydesktop.finance.model.EventMessage.GetLogonCredentialsFinished;
import com.moneydesktop.finance.model.EventMessage.MfaQuestionsRecieved;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.shared.fragment.FixBankFragment;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.Fonts;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FixBankTabletFragment extends FixBankFragment{

	private static String mAccountName;
	private static String mAccountId;
	private Bank mBank;
	private TextView mNotificationDescription, mNotificationTitle, mNotificationContinue, mFixCredentialsMessage, mConnect, mCredential1Question;
	private ViewFlipper mFlipper;
	private JSONObject mUpdateMFA;
	private JSONArray mJsonCredentialsArray; 
	private EditText mAnswer1;
	private EditText mEdit1, mEdit2, mEdit3;
	private Button mSaveInstitution;
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.fix_bank_notification_label);
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}
		
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
	}
	
    @Override
	public void onPause() {
    	super.onPause();
    	EventBus.getDefault().unregister(this);
	}

	public static FixBankTabletFragment newInstance(Intent intent) {
		
		FixBankTabletFragment fragment = new FixBankTabletFragment();
        
		mAccountName = intent.getExtras().getString(Constant.KEY_ACCOUNT_NAME);
		mAccountId = intent.getExtras().getString(Constant.KEY_BANK_ACCOUNT_ID);
			
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_fix_bank, null);
        
        mFlipper = (ViewFlipper)mRoot.findViewById(R.id.fix_bank_flipper); 
        

        BankDao bankDAO = ApplicationContext.getDaoSession().getBankDao();
        mBank = bankDAO.load(Long.valueOf(mAccountId.hashCode()));
        
        mNotificationDescription = (TextView) mRoot.findViewById(R.id.tablet_bank_broken_notification_description);
        mNotificationTitle = (TextView) mRoot.findViewById(R.id.tablet_bank_broken_notification_title);
        mNotificationContinue = (TextView) mRoot.findViewById(R.id.tablet_bank_broken_notification_continue);
        mFixCredentialsMessage = (TextView) mRoot.findViewById(R.id.tablet_bank_broken_connect_message);
        mConnect = (TextView)mRoot.findViewById(R.id.tablet_bank_broken_notification_connect);
        mCredential1Question = (TextView)mRoot.findViewById(R.id.fix_account_question_title);
        
        mAnswer1 = (EditText)mRoot.findViewById(R.id.fix_account_connect_option1_edittxt);
        mSaveInstitution = (Button)mRoot.findViewById(R.id.add_account_save_button);
        
    	Fonts.applyPrimaryBoldFont(mSaveInstitution, 14);        
        Fonts.applyPrimaryBoldFont(mNotificationTitle, 20);
        Fonts.applySecondaryBoldFont(mNotificationDescription, 12);
        Fonts.applyPrimaryBoldFont(mNotificationContinue, 14);
        Fonts.applyPrimaryBoldFont(mConnect, 14);
        Fonts.applySecondaryItalicFont(mFixCredentialsMessage, 12);
        Fonts.applyPrimaryFont(mCredential1Question, 14);
        
        setupOnClickListeners();
     
        setupView();
        
        return mRoot;
    }

	private void setupView() {
		if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_MFA.index())) {
			mNotificationDescription.setText(getString(R.string.fix_bank_notification_mfa_message));
			getMfaQuestions(mBank);
		} else if (mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_LOGIN_FAILED.index())) {
			getLoginQuestions(mBank.getInstitution().getInstitutionId());
		}
	}
       
	public void onEvent(final GetLogonCredentialsFinished event) {        
	    if (event.isAddedForFistTime()) {
	    	
	    	Handler updateFields = new Handler(Looper.getMainLooper());
	    	updateFields.post(new Runnable() {
	    	    public void run()
	    	    {
	    	    	
	    	    	List<String> logonLabels = event.getLogonLabels();
	            	TextView label1 = (TextView)mRoot.findViewById(R.id.add_account_connect_option1_title_txt);
	            	TextView label2 = (TextView)mRoot.findViewById(R.id.add_account_connect_option2_title_txt);
	            	TextView label3 = (TextView)mRoot.findViewById(R.id.add_account_connect_option3_title_txt);
	            	
	            	mEdit1 = (EditText)mRoot.findViewById(R.id.add_account_connect_option1_edittxt);
	            	mEdit2 = (EditText)mRoot.findViewById(R.id.add_account_connect_option2_edittxt);
	            	mEdit3 = (EditText)mRoot.findViewById(R.id.add_account_connect_option3_edittxt);
	            	
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
					
					((DropDownTabletActivity)mActivity).dismissDropdown();
					JSONObject objectToSendToAddInstitution = new JSONObject();
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
						objectToSendToAddInstitution.put("institution_guid", mBank.getInstitution().getInstitutionId());
						objectToSendToAddInstitution.put("user_guid", User.getCurrentUser().getUserId());
					} catch (JSONException e) {
						//TODO: update this log to something useful
						e.printStackTrace(); 
					}
					
					sendUpdatedCredentials(objectToSendToAddInstitution, mBank);
					
				}

			});
	    }
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
     
	public void onEvent(final MfaQuestionsRecieved event) {

    	Handler updateUI = new Handler(Looper.getMainLooper());
    	updateUI.post(new Runnable() {
    	    public void run()
    	    {
    	    	for (int i = 0; i < event.getQuestions().size(); i++) {
    	    		mCredential1Question.setText(event.getQuestions().get(i).toString());    	    		
    	    	}
    	    }
    	    
    	});
    }
        
	private void setupOnClickListeners() {
		
		mNotificationContinue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!mBank.getProcessStatus().equals(BankRefreshStatus.STATUS_LOGIN_FAILED.index())) {
					Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
					Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
					mFlipper.setInAnimation(in);
					mFlipper.setOutAnimation(out);
					mFlipper.showNext();					
				} else {
					Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.in_right);
					Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.out_left);
					mFlipper.setInAnimation(in);
					mFlipper.setOutAnimation(out);
					mFlipper.setDisplayedChild(mFlipper.indexOfChild(mRoot.findViewById(R.id.tablet_fix_bank_login_failed)));
				}
			}
		});
		
		mConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				((DropDownTabletActivity)mActivity).dismissDropdown();
				
				mUpdateMFA = new JSONObject();				
				mJsonCredentialsArray = new JSONArray();
				try {

					for (int i = 0; i < mQuestionLabels.size(); i++) {
						mUpdateMFA.putOpt("guid", mQuestions.get(mQuestionLabels.get(i).toString()));				
						mUpdateMFA.putOpt("value", mAnswer1.getText().toString());
						
						mJsonCredentialsArray.put(mUpdateMFA);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				sendAnsweredMFAQuestion(mBank, mJsonCredentialsArray);
			}
			
		});		
	}

	@Override
	public FragmentType getType() {
		return FragmentType.FIX_BANK;
	}

}