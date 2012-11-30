package com.moneydesktop.finance.handset.fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.WordUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.moneydesktop.finance.BaseActivity.AppearanceListener;
import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.data.BankLogoManager;
import com.moneydesktop.finance.data.Constant;
import com.moneydesktop.finance.data.DataController;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.database.TransactionsDao;
import com.moneydesktop.finance.util.Fonts;

public class TransactionDetailFragment extends BaseFragment implements AppearanceListener {
	
	public final String TAG = this.getClass().getSimpleName();

	private NumberFormat formatter = NumberFormat.getCurrencyInstance();
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("MM.dd.yyyy");
	
	private TransactionsDao dao;
	private Transactions transaction;
	
	private TextView accountName, bankName, category, tags;
	private EditText payee, amount, date, memo, statement;
	private ImageView bankIcon;
	private ToggleButton business, personal, cleared, flagged;
	
	public static TransactionDetailFragment newInstance(long guid) {
		
		TransactionDetailFragment frag = new TransactionDetailFragment();
		
        Bundle args = new Bundle();
        args.putLong(Constant.KEY_GUID, guid);
        frag.setArguments(args);
        
        return frag;
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        this.activity.onFragmentAttached(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        this.activity.updateNavBar(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		root = inflater.inflate(R.layout.handset_transaction_detail_view, null);
		setupViews();
		
		getTransaction();
		loadTransaction();
		Log.i(TAG, "Loaded Transaction: " + transaction.getTitle());
		
		return root;
	}
	
	private void setupViews() {
		
		accountName = (TextView) root.findViewById(R.id.account_name);
		bankName = (TextView) root.findViewById(R.id.bank_name);
		category = (TextView) root.findViewById(R.id.category_name);
		tags = (TextView) root.findViewById(R.id.tags);
		
		payee = (EditText) root.findViewById(R.id.payee_name);
		amount = (EditText) root.findViewById(R.id.amount);
		date = (EditText) root.findViewById(R.id.date);
		memo = (EditText) root.findViewById(R.id.memo);
		statement = (EditText) root.findViewById(R.id.stmt);
		
		bankIcon = (ImageView) root.findViewById(R.id.bank_image);
		
		business = (ToggleButton) root.findViewById(R.id.flag_b);
		personal = (ToggleButton) root.findViewById(R.id.flag_p);
		cleared = (ToggleButton) root.findViewById(R.id.flag_c);
		flagged = (ToggleButton) root.findViewById(R.id.flag);
		
		// Currently we are read-only, disable all input fields
		payee.setEnabled(false);
		amount.setEnabled(false);
		date.setEnabled(false);
		memo.setEnabled(false);
		statement.setEnabled(false);
		
		business.setEnabled(false);
		personal.setEnabled(false);
		cleared.setEnabled(false);
		flagged.setEnabled(false);
		
		fixDottedLine();
		applyFonts();
		configureListeners();
	}
	
	@TargetApi(11)
	private void fixDottedLine() {

		LinearLayout container = (LinearLayout) root.findViewById(R.id.root);
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			
			for (int i = 0; i < container.getChildCount(); i++) {
				
				View v = container.getChildAt(i);
				
				if (v.getTag() != null && v.getTag().equals("dotted"))
					v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}	
		}
	}
	
	private void applyFonts() {
		
		Fonts.applyPrimaryFont(accountName, 12);
		Fonts.applySecondaryItalicFont(bankName, 10);
		Fonts.applyPrimarySemiBoldFont(payee, 24);
		Fonts.applyPrimaryBoldFont(amount, 48);
		Fonts.applyPrimarySemiBoldFont(date, 24);
		Fonts.applyPrimarySemiBoldFont(category, 24);
		Fonts.applyPrimarySemiBoldFont(tags, 24);
		Fonts.applyPrimarySemiBoldFont(memo, 24);
		Fonts.applyPrimaryBoldFont(statement, 12);
		
		// labels
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.payee), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.amount_label), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.date_label), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.category), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.tags_label), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.markers_label), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.memo_label), 12);
		Fonts.applySecondaryItalicFont((TextView) root.findViewById(R.id.stmt_label), 12);
	}
	
	private void configureListeners() {
		
		business.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				personal.setChecked(!business.isChecked());
			}
		});
		
		personal.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				business.setChecked(!personal.isChecked());
			}
		});
	}
	
	private void getTransaction() {
		
		long guid = getArguments().getLong(Constant.KEY_GUID);
		
		if (guid == -1) {
			getFragmentManager().popBackStack();
			return;
		}

		dao = (TransactionsDao) DataController.getDao(Transactions.class);
		transaction = dao.load(guid);
	}
	
	private void loadTransaction() {
		
		if (transaction.getBankAccount() != null)
			BankLogoManager.getBankImage(bankIcon, transaction.getBankAccount().getInstitutionId());
		
		accountName.setText(transaction.getBankAccount().getAccountName());
		bankName.setText(transaction.getBankAccount().getBank().getBankName());
		category.setText(transaction.getCategory().getCategoryName());
		tags.setText(transaction.getTagString());
		
		payee.setText(WordUtils.capitalize(transaction.getTitle().toLowerCase()));
		amount.setText(formatter.format(transaction.getRawAmount()));
		date.setText(dateFormatter.format(transaction.getDate()));
		memo.setText(transaction.getMemo());
		statement.setText(transaction.getOriginalTitle());
		
		business.setChecked(transaction.getIsBusiness());
		personal.setChecked(!transaction.getIsBusiness());
		cleared.setChecked(transaction.getIsCleared());
		flagged.setChecked(transaction.getIsFlagged());
	}

	@Override
	public String getFragmentTitle() {
		return getString(R.string.title_activity_transaction);
	}

	public void onViewDidAppear() {
		
	}
}
