package com.example.zodiac.controller.dialogs;

import com.example.zodiac.R;
import com.example.zodiac.model.Model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;

public class SymbolDialogFragment extends DialogFragment {
	/*
	 * Диалог изменения привязки буквы к символу.
	 */
	
	/* Ключи для дополнений интентов. */
	public static final String EXTRA_SYMBOL_ID = "com.example.zodiac.controller.dialogsymbolid";
	public static final String EXTRA_SYMBOL_CHAR = "com.example.zodiac.controller.dialogsymbolchar";

	/* Модель. */
	private Model mModel;

	/* Идентификатор изменяемого символа. */
	private int mSymbId;

	/* Виджет редактирования буквы, привязываемой к символу. */
	private EditText mSymbolEditText;

	/* Отображаемый диалог. */
	private Dialog mDialog;

	/* Создание экземпляра фрагмента и упаковка его вместе с аргументами. */
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
		mModel = Model.getInstance(getActivity());
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
		
		mDialog = new AlertDialog.Builder(getActivity()).setView(v)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Character chr = null;
						if (mSymbolEditText.getText().length() > 0)
							chr = mSymbolEditText.getText().charAt(0);

						sendResult(Activity.RESULT_OK, chr);
					}
				}).create();
		mDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return mDialog;
	}

	/* Отправка родительскому фрагменту результата - буквы. */
	private void sendResult(int resultCode, Character chr) {
		if (getTargetFragment() == null)
			return;

		Intent i = new Intent();
		i.putExtra(EXTRA_SYMBOL_ID, mSymbId);
		if (chr != null)
			i.putExtra(EXTRA_SYMBOL_CHAR, chr);

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
	}
}
