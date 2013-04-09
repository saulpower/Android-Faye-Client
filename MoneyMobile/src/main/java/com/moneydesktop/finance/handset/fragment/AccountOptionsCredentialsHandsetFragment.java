package main.java.com.moneydesktop.finance.handset.fragment;

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
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.BankLogoManager;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.DataBridge;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.database.Bank;
import main.java.com.moneydesktop.finance.database.Institution;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.model.EventMessage.GetLogonCredentialsFinished;
import main.java.com.moneydesktop.finance.model.User;
import main.java.com.moneydesktop.finance.shared.fragment.FixBankFragment;
import main.java.com.moneydesktop.finance.util.Fonts;
import main.java.com.moneydesktop.finance.util.UiUtils;
import main.java.com.moneydesktop.finance.views.LabelEditText;
import main.java.com.moneydesktop.finance.views.LineView;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AccountOptionsCredentialsHandsetFragment extends FixBankFragment{

    private Bank mBank;
    private TextView mSave;
    private LabelEditText mLabel1, mLabel2, mLabel3;

    private Institution mInstitution;

    private String mBankName;
    private String mInstitutionID;

    public void setBank(Bank mBank) {
        this.mBank = mBank;
    }

    public void setInstitution(Institution mInstitution) {
        this.mInstitution = mInstitution;
    }

    @Override
    public FragmentType getType() {
        return null;
    }

    @Override
    public String getFragmentTitle() {
        return String.format(getString(R.string.add_account_institution_connect), mBank.getBankName());
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


    public static AccountOptionsCredentialsHandsetFragment newInstance(Bank bank) {

        AccountOptionsCredentialsHandsetFragment frag = new AccountOptionsCredentialsHandsetFragment();
        frag.setBank(bank);

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    public static AccountOptionsCredentialsHandsetFragment newInstance(Institution institution) {

        AccountOptionsCredentialsHandsetFragment frag = new AccountOptionsCredentialsHandsetFragment();
        frag.setInstitution(institution);

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.handset_account_option_credentials_view, null);


        if (mBank == null) {
            mBankName = mInstitution.getName();
            mInstitutionID = mInstitution.getInstitutionId();
        } else {
            mBankName = mBank.getBankName();
            mInstitutionID = mBank.getInstitution().getInstitutionId();
        }

        ImageView logo = (ImageView)mRoot.findViewById(R.id.handset_bank_options_credentials_logo);

        Bitmap bitmap = BankLogoManager.getBitmapFromMemCache(mBank.getInstitution().getInstitutionId());
        if (bitmap == null) {
            BankLogoManager.getBankImage(logo, mInstitutionID);
        } else {
            logo.setImageBitmap(bitmap);
        }


        TextView bankNameView = (TextView)mRoot.findViewById(R.id.handset_bank_options_credentials_bank_name);
        bankNameView.setText(mBankName);

        mSave = (TextView)mRoot.findViewById(R.id.handset_fix_bank_save);
        mLabel1 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_user_id);
        mLabel2 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_password);
        mLabel3 = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_pin);

        LabelEditText mfaQuestion = (LabelEditText)mRoot.findViewById(R.id.handset_bank_options_mfa_question);
        mfaQuestion.setVisibility(View.GONE);

        Fonts.applyPrimaryBoldFont(bankNameView, 18);
        Fonts.applyPrimaryBoldFont(mSave, 18);

        setupView();

        return mRoot;
    }

    private void setupView() {

        mSave.setText(getString(R.string.save));

        getLoginQuestions(mInstitutionID);
    }

    public void onEvent(final GetLogonCredentialsFinished event) {
        if (event.isAddedForFistTime()) {

            Handler updateFields = new Handler(Looper.getMainLooper());
            updateFields.post(new Runnable() {
                public void run()
                {
                    updateLogonLabels(event);
                }
            });


            mSave.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    saveBank(event, v);
                }
            });
        }
    }

    private void updateLogonLabels(final GetLogonCredentialsFinished event) {
        List<String> logonLabels = event.getLogonLabels();

        LineView line1 = (LineView)mRoot.findViewById(R.id.view_breaker1);
        LineView line2 = (LineView)mRoot.findViewById(R.id.view_breaker2);
        LineView line3 = (LineView)mRoot.findViewById(R.id.view_breaker3);


        switch (logonLabels.size()) {
        case 1:
            //Don't think this will ever happen....
            mLabel1.setVisibility(View.VISIBLE);
            mLabel2.setVisibility(View.GONE);
            mLabel3.setVisibility(View.GONE);

            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.GONE);
            line3.setVisibility(View.GONE);

            break;
        case 2:
            //Most common...just UserName/password
            mLabel1.setVisibility(View.VISIBLE);
            mLabel2.setVisibility(View.VISIBLE);
            mLabel3.setVisibility(View.GONE);

            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.VISIBLE);
            line3.setVisibility(View.GONE);

            break;
        case 3:
            //Banks like USSA will hit this. UserName/Password/Pin
            mLabel1.setVisibility(View.VISIBLE);
            mLabel2.setVisibility(View.VISIBLE);
            mLabel3.setVisibility(View.VISIBLE);

            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.VISIBLE);
            line3.setVisibility(View.VISIBLE);

            break;
        default:
            break;
        }

        for (int i = 0; i < logonLabels.size(); i++) {
            switch (i) {
            case 0:
                mLabel1.setLabelText(logonLabels.get(i));
                break;
            case 1:
                mLabel2.setLabelText(logonLabels.get(i));
                break;
            case 2:
                mLabel3.setLabelText(logonLabels.get(i));
                break;
            default:
                break;
            }

        }
    }

    private void saveBank(final GetLogonCredentialsFinished event, View v) {
        UiUtils.hideKeyboard(mActivity, v);

        final JSONObject objectToSendToAddInstitution = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            for (int i = 0; i < event.getLogonLabels().size(); i++) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("guid", mCredentialsHash.get(event.getLogonLabels().get(i).toString()));

                switch (i) {
                case 0:
                    jsonObject.put("value", mLabel1.getText().toString());
                    break;
                case 1:
                    jsonObject.put("value", mLabel2.getText().toString());
                    break;
                case 2:
                    jsonObject.put("value", mLabel3.getText().toString());
                    break;
                default:
                    break;
                }

                jsonArray.put(jsonObject);
            }

            objectToSendToAddInstitution.put(Constant.KEY_CREDENTIALS, jsonArray);
            objectToSendToAddInstitution.put("institution_guid", mInstitutionID);
            objectToSendToAddInstitution.put("user_guid", User.getCurrentUser().getUserId());
        } catch (JSONException e) {
            //TODO: update this log to something useful
            e.printStackTrace();
        }

        if (mBank != null){

            sendUpdatedCredentials(objectToSendToAddInstitution, mBank);
        } else {
            new Thread(new Runnable() {
                public void run() {
                    JSONObject jsonResponse = DataBridge.sharedInstance().saveFinancialInstitute(objectToSendToAddInstitution);

                    //Notify that request is finished and we are now ready to start populating the view.
                    EventBus.getDefault().post(new EventMessage().new SaveInstitutionFinished(jsonResponse));
                }
            }).start();
        }

        mActivity.clearBackStack();
    }
}