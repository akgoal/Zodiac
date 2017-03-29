package com.deakishin.zodiac.controller.mainscreen.dialogs;

import com.deakishin.zodiac.R;
import com.deakishin.zodiac.controller.CustomDialogFragment;
import com.deakishin.zodiac.model.ciphermanager.CipherManager;
import com.deakishin.zodiac.model.ciphermodel.Model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/** Dialog for entering a character that will be mapped symbol in the cipher. */
public class SymbolDialogFragment extends CustomDialogFragment {

	/** Key for the id of the symbol in the cipher. */
	public static final String EXTRA_SYMBOL_ID = "com.example.zodiac.controller.dialogsymbolid";
	/** Key for the entered character. */
	public static final String EXTRA_SYMBOL_CHAR = "com.example.zodiac.controller.dialogsymbolchar";

	/* Cipher's model. */
	private Model mModel;

	/* Id of the symbol that is being mapped. */
	private int mSymbId;

	/* Widgets. */
	private EditText mSymbolEditText;
	private Button mConfirmButton;

	/* Displayed dialog. */
	private Dialog mDialog;

	/**
	 * Creates and returns configured instance.
	 * 
	 * @param symbolId
	 *            Id of the symbol.
	 * @param chr
	 *            Character that was originally mapped to the symbol.
	 * @return Configured dialog.
	 */
	public static SymbolDialogFragment newInstance(int symbolId, Character chr) {
		Bundle args = new Bundle();
		args.putInt(EXTRA_SYMBOL_ID, symbolId);
		if (chr != null)
			args.putChar(EXTRA_SYMBOL_CHAR, chr);

		SymbolDialogFragment dialog = new SymbolDialogFragment();
		dialog.setArguments(args);
		return dialog;
	}

	public SymbolDialogFragment() {
		super();
		mModel = CipherManager.getInstance(getActivity()).getCipherModel();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mSymbId = getArguments().getInt(EXTRA_SYMBOL_ID);
		Character symbChr = null;
		if (getArguments().containsKey(EXTRA_SYMBOL_CHAR)) {
			symbChr = getArguments().getChar(EXTRA_SYMBOL_CHAR);
		}
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_symbol, null);

		ImageView mainImageView = (ImageView) v.findViewById(R.id.symbol_imageview);
		mainImageView.setImageBitmap(mModel.getImageBySymbId(mSymbId));

		mSymbolEditText = (EditText) v.findViewById(R.id.symbol_edittext);
		if (symbChr != null) {
			mSymbolEditText.setText(symbChr.toString());
		}
		mSymbolEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					returnResult();
					return true;
				}
				return false;
			}
		});

		mConfirmButton = (Button) v.findViewById(R.id.symbol_button);
		mConfirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnResult();
			}
		});

		mDialog = new AlertDialog.Builder(getActivity()).setView(v).create();
		mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/* Return entered character. */
	private void returnResult() {
		Character chr = null;
		if (mSymbolEditText.getText().length() > 0)
			chr = mSymbolEditText.getText().charAt(0);
		sendResult(Activity.RESULT_OK, chr);
	}

	/* Send result to the target fragment. */
	private void sendResult(int resultCode, Character chr) {
		if (getTargetFragment() == null)
			return;

		Intent i = new Intent();
		i.putExtra(EXTRA_SYMBOL_ID, mSymbId);
		if (chr != null)
			i.putExtra(EXTRA_SYMBOL_CHAR, chr);

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
		dismiss();
	}
}
