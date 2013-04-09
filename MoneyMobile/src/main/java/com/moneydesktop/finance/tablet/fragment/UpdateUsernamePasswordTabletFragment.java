package main.java.com.moneydesktop.finance.tablet.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import main.java.com.moneydesktop.finance.ApplicationContext;
import main.java.com.moneydesktop.finance.R;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.DataBridge;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.database.Bank;
import main.java.com.moneydesktop.finance.database.BankDao;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.model.EventMessage.GetLogonCredentialsFinished;
import main.java.com.moneydesktop.finance.model.EventMessage.UpdateCredentialsFinished;
import main.java.com.moneydesktop.finance.shared.Services.SyncService;
import main.java.com.moneydesktop.finance.shared.fragment.BaseFragment;
import main.java.com.moneydesktop.finance.tablet.activity.DropDownTabletActivity;
import main.java.com.moneydesktop.finance.util.Fonts;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateUsernamePasswordTabletFragment extends BaseFragment{

    private static Bank mBank;
    private TextView mConnect;
    private List<String> mLoginLabels;
    private HashMap<String, String> mCredentialsHash = new HashMap<String, String>();
    private JSONObject objectToSendToAddInstitution;
    private EditText mEdit1, mEdit2, mEdit3;

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
    public boolean onBackPressed() {
        return false;
    }

    public static UpdateUsernamePasswordTabletFragment newInstance(Intent intent) {

        UpdateUsernamePasswordTabletFragment fragment = new UpdateUsernamePasswordTabletFragment();

        String bankId = intent.getExtras().getString(Constant.KEY_BANK_ACCOUNT_ID);

        BankDao bankDAO = ApplicationContext.getDaoSession().getBankDao();
        mBank = bankDAO.load(Long.valueOf(bankId.hashCode()));

        Bundle args = new Bundle();
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = inflater.inflate(R.layout.tablet_add_bank_connect, null);

        mConnect = (TextView)mRoot.findViewById(R.id.add_account_save_button);

        setupConnectScreen();

        return mRoot;
    }

    private void setupConnectScreen() {
        mLoginLabels = new ArrayList<String>();


        //Gets the Logon Fields needed to populate view for the specific institution
        new Thread(new Runnable() {
            public void run() {
                JSONArray array = DataBridge.sharedInstance().getInstituteLoginFields(mBank.getInstitution().getInstitutionId());
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

                Fonts.applyPrimaryBoldFont(mConnect, 14);
                Fonts.applyPrimaryBoldFont(mEdit1, 14);
                Fonts.applyPrimaryBoldFont(mEdit2, 14);
                Fonts.applyPrimaryBoldFont(mEdit3, 14);

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
                    //Banks like USAA will hit this. UserName/Password/Pin
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


        mConnect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
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
                    objectToSendToAddInstitution.put("guid", mBank.getBankId());
                    objectToSendToAddInstitution.put("name", mBank.getBankName());
                } catch (JSONException e) {
                    //TODO: update this log to something useful
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    public void run() {
                        JSONObject jsonResponse = DataBridge.sharedInstance().updateLoginFields(mBank.getBankId(), objectToSendToAddInstitution);

                        //Notify that request is finished and we are now ready to start populating the view.
                        EventBus.getDefault().post(new EventMessage().new UpdateCredentialsFinished(jsonResponse));

                    }
                }).start();

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

    public void onEvent(final UpdateCredentialsFinished event) {

        Handler updateFields = new Handler(Looper.getMainLooper());
        updateFields.post(new Runnable() {
            public void run()
            {
                ((DropDownTabletActivity)mActivity).dismissDropdown();
            }
        });

        //start the sync
        Intent intent = new Intent(mActivity, SyncService.class);
        mActivity.startService(intent);
    }
    @Override
    public FragmentType getType() {
        return FragmentType.UPDATE_USERNAME_PASSWORD;
    }

}
