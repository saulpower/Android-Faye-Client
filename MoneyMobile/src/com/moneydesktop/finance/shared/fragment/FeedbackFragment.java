package com.moneydesktop.finance.shared.fragment;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneydesktop.communication.HttpRequest;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.Enums.FragmentType;
import com.moneydesktop.finance.handset.activity.PopupHandsetActivity;
import com.moneydesktop.finance.model.User;
import com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import com.moneydesktop.finance.util.DialogUtils;
import com.moneydesktop.finance.util.EmailUtils;
import com.moneydesktop.finance.util.Fonts;
import com.moneydesktop.finance.views.LabelEditText;

public class FeedbackFragment extends BaseFragment {
	
	public final String TAG = this.getClass().getSimpleName();
	
	public static FeedbackFragment newInstance() {
        
	    FeedbackFragment fragment = new FeedbackFragment();
    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }
	
	private PopupHandsetActivity mPopupActivity;
	private DropDownTabletActivity mDropDownActivity;
	
	private TextView mTitle, mMessage, mCancel;
	private LabelEditText mName, mEmail, mFeedback;
	private Button mSubmit;
	private ImageView mLogo;
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
        
	    if (activity instanceof PopupHandsetActivity) {
	        mPopupActivity = (PopupHandsetActivity) activity;
	    } else if (activity instanceof DropDownTabletActivity) {
	    	mDropDownActivity = (DropDownTabletActivity) activity;
	    }
	}

	@Override
	public FragmentType getType() {
		return null;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.feedback_view, null);
        
        setupViews();
        setupListeners();
        
        return mRoot;
	}
	
	private void setupViews() {

		mLogo = (ImageView) mRoot.findViewById(R.id.logo2);
		mTitle = (TextView) mRoot.findViewById(R.id.title);
		mMessage = (TextView) mRoot.findViewById(R.id.message);
		mCancel = (TextView) mRoot.findViewById(R.id.cancel);
		mName = (LabelEditText) mRoot.findViewById(R.id.feedback_name);
		mEmail = (LabelEditText) mRoot.findViewById(R.id.feedback_email);
		mFeedback = (LabelEditText) mRoot.findViewById(R.id.feedback);
		mSubmit = (Button) mRoot.findViewById(R.id.submit);
		
		if (mPopupActivity == null) {
			mLogo.setVisibility(View.GONE);
		}
		
		applyFonts();
	}
	
	private void applyFonts() {
		
		Fonts.applyPrimarySemiBoldFont(mTitle, 12);
		Fonts.applyPrimaryFont(mMessage, 10);

        Fonts.applyPrimarySemiBoldFont(mName, 16);
        Fonts.applyPrimarySemiBoldFont(mEmail, 16);
        Fonts.applyPrimarySemiBoldFont(mFeedback, 10);
        
        Fonts.applyPrimaryFont(mCancel, 10);
        Fonts.applyPrimarySemiBoldFont(mSubmit, 10);
	}
	
	private void setupListeners() {
		
		mSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitFeedback(mName.getText().toString(), mEmail.getText().toString(), mFeedback.getText().toString());
			}
		});
		
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissModal();
			}
		});
	}
	
	private void submitFeedback(String name, String email, String feedback) {

		if (!EmailUtils.validateEmail(email)) {
            DialogUtils.alertDialog(getString(R.string.error_title), getString(R.string.error_email_invalid), getActivity(), null);
            return;
        }
		
		DialogUtils.showProgress(getActivity(), getString(R.string.feedback_sending));
		
		new AsyncTask<String, Void, String>() {
    		
			@Override
			protected String doInBackground(String... params) {

				String response = "";
				
				try {
					
					JSONObject json = new JSONObject();
					JSONObject ticket = new JSONObject();
					JSONObject comment = new JSONObject();
					JSONObject requester = new JSONObject();

				    String subjectLine = "Mobile Support Request";
					String emailString = String.format("name:%s, email:%s, user name:%s, user GUID:%s, feedback:%s", params[0], params[1], User.getCurrentUser().getUserName(), User.getCurrentUser().getUserId(), params[2]);
					
					comment.put("value", emailString);

				    requester.put("locale_id", 1);
				    requester.put("name", params[0]);
				    requester.put("email", User.getCurrentUser().getUserName());
				    
					json.put("subject", subjectLine);
					json.put("comment", comment);
					json.put("requester", requester);
					
					ticket.put("ticket", json);
					
					String credentials = "guru@moneydesktop.com:Gh@ndi11";
					String encoding = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);

					HashMap<String, String> headers = new HashMap<String, String>();
					headers.put("Content-Type", "application/json");
					headers.put("Authorization", "Basic " + encoding);
					
					response = HttpRequest.sendPost("https://moneydesktop.zendesk.com/api/v2/tickets.json", headers, null, ticket.toString());

				} catch (Exception ex) {
					Log.e(TAG, "Failed to send feedback!", ex);
				}

				return response;
			}
    		
    		@Override
    		protected void onPostExecute(String response) {

    			DialogUtils.hideProgress();
    			DialogUtils.alertDialog(getString(R.string.feedback_title), getString(R.string.feedback_message), getActivity(), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissModal();
					}
				});
    			Log.i(TAG, "Response: " + response);
    		}
			
		}.execute(name, email, feedback);
	}
	
	private void dismissModal() {
        
	    if (mPopupActivity != null) {
	        mPopupActivity.dismissModal();
	    } else if (mDropDownActivity != null) {
	    	mDropDownActivity.dismissDropdown();
	    }
	}

    @Override
    public String getFragmentTitle() {
        return getString(R.string.title_feedback).toUpperCase();
    }

	@Override
	public boolean onBackPressed() {
		return false;
	}
}
