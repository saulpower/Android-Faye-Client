package main.java.com.moneydesktop.finance.shared.fragment;


import android.content.Intent;
import main.java.com.moneydesktop.finance.data.Constant;
import main.java.com.moneydesktop.finance.data.DataBridge;
import main.java.com.moneydesktop.finance.data.DataController;
import main.java.com.moneydesktop.finance.data.Enums.FragmentType;
import main.java.com.moneydesktop.finance.data.SyncEngine;
import main.java.com.moneydesktop.finance.database.Bank;
import main.java.com.moneydesktop.finance.model.EventMessage;
import main.java.com.moneydesktop.finance.shared.Services.SyncService;
import de.greenrobot.event.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class FixBankFragment extends BaseFragment{

    public final String TAG = this.getClass().getSimpleName();
    private List<String> mLoginLabels;
    public HashMap<String, String> mCredentialsHash = new HashMap<String, String>();
    public HashMap<String, String> mQuestions;
    public List<String> mQuestionLabels;

    @Override
    public FragmentType getType() {
        return null;
    }

    protected void getLoginQuestions(final String institutionID) {
        mLoginLabels = new ArrayList<String>();

        new Thread(new Runnable() {
            public void run() {
                JSONArray array = DataBridge.sharedInstance().getInstituteLoginFields(institutionID);
                for (int i = 0; i< array.length(); i++) {
                    try {
                        String object = ((JSONObject)array.get(i)).getString("label");
                        mLoginLabels.add(object);
                        mCredentialsHash.put(object, ((JSONObject)array.get(i)).getString("guid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //Notify that request is finished and we are now ready to start populating the view.
                EventBus.getDefault().post(new EventMessage().new GetLogonCredentialsFinished(true, mLoginLabels));
            }
        }).start();
    }

    protected void getMfaQuestions(final Bank bank) {
        mQuestions = new HashMap<String, String>();
        mQuestionLabels = new ArrayList<String>();

        new Thread(new Runnable() {
            public void run() {
                JSONArray jsonArray = DataBridge.sharedInstance().getMfaQuestions(bank.getBankId());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonCredentials = jsonArray.optJSONObject(i);
                    mQuestions.put(jsonCredentials.optString(Constant.KEY_LABEL), jsonCredentials.optString(Constant.KEY_GUID));
                    mQuestionLabels.add(jsonCredentials.optString(Constant.KEY_LABEL));
                }
                EventBus.getDefault().post(new EventMessage().new MfaQuestionsRecieved(mQuestionLabels));

            }
        }).start();
    }

    public void sendUpdatedCredentials(final JSONObject objectToSendToAddInstitution, final Bank bank) {
        new Thread(new Runnable() {
            public void run() {

                JSONObject jsonResponse = DataBridge.sharedInstance().updateLoginFields(bank.getBankId(), objectToSendToAddInstitution);
                Intent intent = new Intent(mActivity, SyncService.class);
                mActivity.startService(intent);
            }
        }).start();
    }


    public void sendAnsweredMFAQuestion(final Bank bank, final JSONArray jsonCredentialsArray) {
        new Thread(new Runnable() {
            public void run() {
                JSONObject jsonObject = DataBridge.sharedInstance().updateMfaQuestions(bank.getBankId(), jsonCredentialsArray);

                JSONObject memberObject = jsonObject.optJSONObject(Constant.KEY_MEMBER);
                Bank.saveIncomingBank(memberObject, false);

                DataController.save();
                SyncEngine.sharedInstance().beginSync();

                EventBus.getDefault().post(new EventMessage().new UpdateSpecificBankStatus(bank));

            }
        }).start();
    }
}
