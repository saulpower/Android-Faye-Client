package com.moneydesktop.finance.handset.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.database.Bank;
import com.moneydesktop.finance.model.EventMessage.MfaQuestionsRecieved;
import com.moneydesktop.finance.shared.fragment.FixBankFragment;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.LabelEditText;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountOptionMfaQuestionHandsetFragment extends FixBankFragment{

	
	private static Bank mBank;
	private TextView mSave, mTitle, mMessage, mContinue;
	private LabelEditText mMfaQuestion;
	private LabelEditText mLabel1, mLabel2, mLabel3;
	private AccountOptionsCredentialsHandsetFragment mCredentialFragment;
	private JSONObject mUpdateMFA;
	private JSONArray mJsonCredentialsArray; 
	
	@Override
	public FragmentType getType() {
		return null;
	}

	@Override
	public String getFragmentTitle() {
		return null;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        EventBus.getDefault().unregister(this);
    }
    
	@Override
	public boolean onBackPressed() {
		return false;
	}

	public static AccountOptionMfaQuestionHandsetFragment newInstance(Bank bank) {
		
		AccountOptionMfaQuestionHandsetFragment frag = new AccountOptionMfaQuestionHandsetFragment();
		mBank = bank;
		
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
	
		mRoot = inflater.inflate(R.layout.handset_account_option_credentials_view, null);
		
		ImageView logo = (ImageView)mRoot.findViewById(R.id.handset_bank_options_credentials_logo);	

        Bitmap bitmap = BankLogoManager.getBitmapFromMemCache(mBank.getInstitution().getInstitutionId());
        if (bitmap == null) {
            BankLogoManager.getBankImage(logo, mBank.getInstitution().getInstitutionId());
        } else {
            logo.setImageBitmap(bitmap);
        }
		
		TextView bankName = (TextView)mRoot.findViewById(R.id.handset_bank_options_credentials_bank_name);
		bankName.setText(mBank.getBankName());
		
		mSave = (TextView)mRoot.findViewById(R.id.handset_fix_bank_save);
    	mLabel1 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_user_id);
    	mLabel2 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_password);
    	mLabel3 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_pin);
		mLabel1.setVisibility(View.GONE);
		mLabel2.setVisibility(View.GONE);
		mLabel3.setVisibility(View.GONE);

    	mMfaQuestion = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_mfa_question);
    	mMfaQuestion.setVisibility(View.VISIBLE);
    	
    	mSave.setText(getString(R.string.save));
    	
		Fonts.applyPrimaryBoldFont(bankName, 18);
		Fonts.applyPrimaryBoldFont(mSave, 18);
		
		setupView();
		return mRoot;
	}

	private void setupView() {
		getMfaQuestions(mBank);
		
		mSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mUpdateMFA = new JSONObject();				
				mJsonCredentialsArray = new JSONArray();
				try {

					for (int i = 0; i < mQuestionLabels.size(); i++) {
						mUpdateMFA.putOpt("guid", mQuestions.get(mQuestionLabels.get(i).toString()));				
						mUpdateMFA.putOpt("value", mMfaQuestion.getText().toString());
						
						mJsonCredentialsArray.put(mUpdateMFA);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				sendAnsweredMFAQuestion(mBank, mJsonCredentialsArray);
				
				
				mActivity.popFragment();
				mActivity.popFragment();
			}
			
		});		

	}
	
	public void onEvent(final MfaQuestionsRecieved event) {

    	Handler updateUI = new Handler(Looper.getMainLooper());
    	updateUI.post(new Runnable() {
    	    public void run()
    	    {
    	    	for (int i = 0; i < event.getQuestions().size(); i++) {    	    		
    	    		mMfaQuestion.setLabelText(event.getQuestions().get(i).toString());
    	    	}
    	    }
    	    
    	});
    }
	
	
}
