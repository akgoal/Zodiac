package com.deakishin.zodiac.controller.mainscreen;

import com.deakishin.zodiac.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class DialogCheckpointName extends DialogFragment {
	/*
	 * Диалога ввода имени чекпоинта.
	 */

	/*
	 * Интерфейс слушателя результата диалога. Вызывающая диалог активность
	 * должна реализовывать этот интерфейс.
	 */
	public static interface OnCheckpointNamedListener {
		public void onCheckpointNamed(String name);
	}

	private OnCheckpointNamedListener mOnCheckpointNamedListener;

	public DialogCheckpointName() {
		super();
	}

	/* Проверка того, что активность реализовала интерфейс слушателя. */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnCheckpointNamedListener = (OnCheckpointNamedListener) activity;
		} catch (Exception e) {
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//builder.setTitle(R.string.checkpointname_custom_dialog_title);
		final EditText input = new EditText(getActivity());
		//input.setHint(R.string.checkpointname_custom_noname);
		input.setHint(R.string.checkpointname_custom_dialog_title);
		input.setId(R.id.checkpoint_name_editText);
		builder.setView(input);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String result = input.getText().toString();
				if (result.length() == 0)
					result = getActivity().getString(R.string.checkpointname_custom_noname);
				if (mOnCheckpointNamedListener != null)
					mOnCheckpointNamedListener.onCheckpointNamed(result);
			}
		});
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return dialog;
	}
}
